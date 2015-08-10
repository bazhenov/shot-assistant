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
	private final Mat perspectiveTransform;
	private final Mat targetFrame = new Mat();
	private final Size targetSize;

	public RestorePerspective(Dimension size, List<Point> points) {
		this.size = size;
		this.points = points;

		double targetRatio = Math.min(300d / size.getWidth(), 300d / size.getHeight());

		MatOfPoint2f destination = new MatOfPoint2f(
			new org.opencv.core.Point(0, 0),
			new org.opencv.core.Point(size.getWidth() * targetRatio, 0),
			new org.opencv.core.Point(size.getWidth() * targetRatio, size.getHeight() * targetRatio),
			new org.opencv.core.Point(0, size.getHeight() * targetRatio));

		targetSize = new Size(size.getWidth() * targetRatio, size.getHeight() * targetRatio);

		MatOfPoint2f source = new MatOfPoint2f(
			toOpenCvPoint(points.get(0)),
			toOpenCvPoint(points.get(1)),
			toOpenCvPoint(points.get(2)),
			toOpenCvPoint(points.get(3)));

		perspectiveTransform = getPerspectiveTransform(source, destination);
	}

	@Override
	public Mat apply(Mat mat) {
		warpPerspective(mat, targetFrame, perspectiveTransform, targetSize);
		return targetFrame;
	}
}
