package me.bazhenov.shotassistant;

import me.bazhenov.shotassistant.target.IpscClassicalTarget;
import org.opencv.core.*;
import org.opencv.highgui.VideoCapture;

import java.awt.Point;
import java.io.Closeable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Math.round;
import static org.opencv.core.Core.countNonZero;
import static org.opencv.core.Core.findNonZero;
import static org.opencv.core.CvType.CV_32FC2;
import static org.opencv.imgproc.Imgproc.minAreaRect;

public class ProcessingChain implements Closeable {

	private final List<Named<Function<Mat, Mat>>> stages = newArrayList();
	private final Consumer<Optional<Point>> pointConsumer;
	private ProcessingListener listener;
	private int frameNo = 0;
	private static double scaleRatio;
	private Consumer<Mat> targetFrameListener;
	private RestorePerspective restorePerspective;
	private Consumer<Mat> originalFrameListener;
	private boolean isClosed = false;

	public ProcessingChain(Consumer<Optional<Point>> pointConsumer) {
		this.pointConsumer = pointConsumer;
	}

	public void setTargetFrameListener(Consumer<Mat> targetFrameListener) {
		this.targetFrameListener = targetFrameListener;
	}

	public void setOriginalFrameListener(Consumer<Mat> originalFrameListener) {
		this.originalFrameListener = originalFrameListener;
	}

	static ProcessingChain createProcessingChain(List<Point> perspectivePoints, IpscClassicalTarget target,
																							 Consumer<Optional<Point>> pointConsumer) {
		ProcessingChain chain = new ProcessingChain(pointConsumer);
		chain.restorePerspective = new RestorePerspective(target.getSize(), perspectivePoints);
		scaleRatio = chain.restorePerspective.getScaleRatio();
		//chain.addStage("perspective", chain.restorePerspective);
		chain.addStage("gray", ImageOperations.grayscale());
		//chain.addStage("backgroundDiff", ImageOperations.differenceWithBackground());
		//chain.addStage("blur", ImageOperations.medianBlur(5));
		//chain.addStage("threshold", ImageOperations.threshold(120));
		//chain.addStage("erode", ImageOperations.erode(3));
		return chain;
	}

	public void run(VideoCapture videoCapture) {
		try {
			runAsync(videoCapture).get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	public CompletableFuture<?> runAsync(VideoCapture videoCapture) {
		CompletableFuture<?> result = new CompletableFuture<>();
		new Thread(() -> {
			Mat frame = new Mat();
			TargetFinder finder = new TargetFinder();
			Mat p = new Mat();
			while (videoCapture.read(frame) && !isClosed) {
				frame.copyTo(p);
				Optional<org.opencv.core.Point[]> target = finder.find(p);
				if (target.isPresent()) {
					System.out.println("Yep");
					MatOfPoint matOfPoint = new MatOfPoint(target.get());
					Core.polylines(frame, newArrayList(matOfPoint), true, new Scalar(255), 3);
				}
				accept(frame);
			}
			result.complete(null);
		}).start();
		return result;
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

	public void accept(Mat frame) {
		frameNo++;

		if (listener != null) {
			listener.onFrame(frame, stages.size());
		}
		if (originalFrameListener != null)
			originalFrameListener.accept(frame);
		for (Named<Function<Mat, Mat>> stage : stages) {
			frame = stage.get().apply(frame);
			if (listener != null) {
				listener.onStage(stage.getName(), frame);
			}
			if (targetFrameListener != null && "perspective".equals(stage.getName())) {
				targetFrameListener.accept(frame);
			}
		}

		FrameFeatures features = extractFeatures(frameNo, frame);
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

	public void updatePerspective(List<Point> points) {
		restorePerspective.updatePerspective(points);
	}

	@Override
	public void close() {
		isClosed = true;
	}
}
