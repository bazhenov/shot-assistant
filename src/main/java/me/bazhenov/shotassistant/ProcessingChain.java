package me.bazhenov.shotassistant;

import me.bazhenov.shotassistant.target.IpscClassicalTarget;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.RotatedRect;
import org.opencv.highgui.VideoCapture;

import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Math.round;
import static org.opencv.core.Core.countNonZero;
import static org.opencv.core.Core.findNonZero;
import static org.opencv.core.CvType.CV_32FC2;
import static org.opencv.core.CvType.CV_32FC3;
import static org.opencv.highgui.Highgui.CV_CAP_PROP_FRAME_HEIGHT;
import static org.opencv.highgui.Highgui.CV_CAP_PROP_FRAME_WIDTH;
import static org.opencv.imgproc.Imgproc.minAreaRect;

public class ProcessingChain implements Consumer<Mat> {

	private final List<Named<Function<Mat, Mat>>> stages = newArrayList();
	private final Consumer<Optional<Point>> pointConsumer;
	private ProcessingListener listener;
	private int frameNo = 0;
	private static double scaleRatio;

	public ProcessingChain(Consumer<Optional<Point>> pointConsumer) {
		this.pointConsumer = pointConsumer;
	}

	static ProcessingChain createProcessingChain(List<Point> perspectivePoints, IpscClassicalTarget target,
																							 Consumer<Optional<Point>> pointConsumer) {
		ProcessingChain chain = new ProcessingChain(pointConsumer);
		RestorePerspective restorePerspective = new RestorePerspective(target.getSize(), perspectivePoints);
		scaleRatio = restorePerspective.getScaleRatio();
		chain.addStage("perspective", restorePerspective);
		chain.addStage("gray", ImageOperations.grayscale());
		chain.addStage("backgroundDiff", ImageOperations.differenceWithBackground());
		chain.addStage("blur", ImageOperations.medianBlur(5));
		chain.addStage("threshold", ImageOperations.threshold(120));
		chain.addStage("erode", ImageOperations.erode(3));
		return chain;
	}

	public void run(VideoCapture videoCapture) {
		int width = (int) videoCapture.get(CV_CAP_PROP_FRAME_WIDTH);
		int height = (int) videoCapture.get(CV_CAP_PROP_FRAME_HEIGHT);

		Mat frame = new Mat(width, height, CV_32FC3);

		while (videoCapture.read(frame)) {
			accept(frame);
		}
	}

	public void addStage(String name, Consumer<Mat> stage) {
		stages.add(new Named<>(name, i -> {
			stage.accept(i);
			return i;
		}));
	}

	public void addStage(String name, Function<Mat, Mat> stage) {
		stages.add(new Named<>(name, stage));
	}

	public void setListener(ProcessingListener listener) {
		this.listener = listener;
	}

	@Override
	public void accept(Mat mat) {
		frameNo++;

		if (listener != null) {
			listener.onFrame(mat, stages.size());
		}
		for (Named<Function<Mat, Mat>> stage : stages) {
			mat = stage.get().apply(mat);
			if (listener != null) {
				listener.onStage(stage.getName(), mat);
			}
		}

		FrameFeatures features = extractFeatures(frameNo, mat);
		if (listener != null) {
			listener.onFrameComplete(features);
		}

		pointConsumer.accept(features.getCentroid().map(this::scaleToTarget));
	}

	private Point scaleToTarget(Point point) {
		return new Point((int) round(point.getX() / scaleRatio), (int) round(point.getY() / scaleRatio));
	}

	private FrameFeatures extractFeatures(int frameNo, Mat mat) {
		MatOfPoint coordinates = new MatOfPoint();
		findNonZero(mat, coordinates);

		MatOfPoint2f m = new MatOfPoint2f();

		coordinates.convertTo(m, CV_32FC2);
		double area = 0;
		Point point = null;
		if (m.size().area() > 0) {
			org.opencv.core.Point p[] = new org.opencv.core.Point[4];
			RotatedRect rect = minAreaRect(m);
			area = rect.size.area();
			rect.points(p);
			point = new Point((int) rect.center.x, (int) rect.center.y);
		}
		int nonZero = countNonZero(mat);
		return new FrameFeatures(frameNo, mat.size(), nonZero, (int) area, Optional.ofNullable(point));
	}
}
