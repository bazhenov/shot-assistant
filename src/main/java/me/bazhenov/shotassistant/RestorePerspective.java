package me.bazhenov.shotassistant;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Size;

import java.awt.*;
import java.util.List;
import java.util.function.Function;

import static me.bazhenov.shotassistant.VisualizingListener.toOpenCvPoint;
import static org.opencv.imgproc.Imgproc.getPerspectiveTransform;
import static org.opencv.imgproc.Imgproc.warpPerspective;

public class RestorePerspective implements Function<Mat, Mat> {

	private final Dimension size;
	private final List<Point> points;
	private final Mat targetFrame = new Mat();
	private final Size targetSize;
	private final double scaleRatio;
	private volatile Mat perspectiveTransform;

	public RestorePerspective(Dimension size, List<Point> points) {
		this.size = size;
		this.points = points;

		scaleRatio = Math.min(300d / size.getWidth(), 300d / size.getHeight());

		targetSize = new Size(size.getWidth() * scaleRatio, size.getHeight() * scaleRatio);
		updatePerspective(points);
	}

	public void updatePerspective(List<Point> points) {
		MatOfPoint2f destination = new MatOfPoint2f(
			new org.opencv.core.Point(0, 0),
			new org.opencv.core.Point(size.getWidth() * scaleRatio, 0),
			new org.opencv.core.Point(size.getWidth() * scaleRatio, size.getHeight() * scaleRatio),
			new org.opencv.core.Point(0, size.getHeight() * scaleRatio));


		MatOfPoint2f source = new MatOfPoint2f(
			toOpenCvPoint(points.get(0)),
			toOpenCvPoint(points.get(1)),
			toOpenCvPoint(points.get(2)),
			toOpenCvPoint(points.get(3)));

		perspectiveTransform = getPerspectiveTransform(source, destination);
	}

	public double getScaleRatio() {
		return scaleRatio;
	}

	@Override
	public Mat apply(Mat mat) {
		warpPerspective(mat, targetFrame, perspectiveTransform, targetSize);
		return targetFrame;
	}
}
