package me.bazhenov.shotassistant;

import me.bazhenov.shotassistant.target.IpscClassicalTarget;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.RotatedRect;

import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.collect.Lists.newArrayList;
import static org.opencv.core.Core.countNonZero;
import static org.opencv.core.Core.findNonZero;
import static org.opencv.core.CvType.CV_32FC2;
import static org.opencv.imgproc.Imgproc.minAreaRect;

public class ProcessingChain<T> {

	private final List<Named<Function<Mat, Mat>>> stages = newArrayList();
	private final Function<Mat, T> frameProcessor;
	private RestorePerspective restorePerspective;

	public ProcessingChain(Function<Mat, T> frameProcessor) {
		this.frameProcessor = frameProcessor;
	}

	static ProcessingChain createProcessingChain(List<Point> perspectivePoints, IpscClassicalTarget target,
																							 Consumer<Optional<Point>> pointConsumer) {
		AtomicInteger frameNo = new AtomicInteger();
		RestorePerspective perspectiveComponent = new RestorePerspective(target.getSize(), perspectivePoints);
		ProcessingChain<FrameFeatures> chain = new ProcessingChain<>(f -> {
			FrameFeatures features = extractFeatures(frameNo.incrementAndGet(), f);
			pointConsumer.accept(features.getCentroid().map(perspectiveComponent::scaleToTarget));
			return features;
		});
		chain.restorePerspective = perspectiveComponent;
		chain.addStage("gray", ImageOperations.grayscale());
		chain.addStage("perspective", chain.restorePerspective);
		chain.addStage("backgroundDiff", ImageOperations.differenceWithBackground());
		chain.addStage("blur", ImageOperations.medianBlur(5));
		chain.addStage("threshold", ImageOperations.threshold(120));
		chain.addStage("erode", ImageOperations.erode(3));
		return chain;
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

	public T apply(Mat frame, BiConsumer<String, Mat> listener) {
		for (Named<Function<Mat, Mat>> stage : stages) {
			frame = stage.get().apply(frame);
			if (listener != null) {
				listener.accept(stage.getName(), frame);
			}
		}

		return frameProcessor.apply(frame);
	}

	private static FrameFeatures extractFeatures(int frameNo, Mat mat) {
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
