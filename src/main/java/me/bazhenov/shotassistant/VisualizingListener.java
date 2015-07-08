package me.bazhenov.shotassistant;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.*;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.LinkedList;
import java.util.List;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import static org.opencv.imgproc.Imgproc.*;

public class VisualizingListener implements FrameListener {

	private static final int TARGET_SIZE = 240;
	private JFrame jframe = null;
	private final MainFrameComponent currentFrameComponent;
	private final OpenCvVideoComponent targetFrameComponent;
	private Mat privateFrame = new Mat();
	private Mat targetFrame = new Mat();
	LinkedList<org.opencv.core.Point> points = new LinkedList<>();
	private int shots = 0;
	private volatile List<Point> perspectivePoints;

	/*public VisualizingListener(Consumer<List<java.awt.Point>> perspectiveListener) {
		this.perspectiveListener = requireNonNull(perspectiveListener);
	}*/

	public VisualizingListener() {
		targetFrameComponent = new TargetFrameComponent();
		currentFrameComponent = new MainFrameComponent();
		PerspectiveComponent perspectiveComponent = new PerspectiveComponent(currentFrameComponent, this::setPerspective);

		jframe = new JFrame();

		jframe.setDefaultCloseOperation(EXIT_ON_CLOSE);
		jframe.add(targetFrameComponent);
		jframe.add(perspectiveComponent);

		jframe.setLayout(new FlowLayout());
	}

	private void setPerspective(List<Point> p) {
		this.perspectivePoints = p;
	}

	@Override
	public void onFrame(Mat mat, Mat background, Mat frame, FrameFeatures features, boolean shotDetected) {
		if (shotDetected)
			shots++;

		cvtColor(frame, privateFrame, Imgproc.COLOR_GRAY2BGR);

		MatOfPoint2f destination = new MatOfPoint2f(
			new org.opencv.core.Point(0, 0),
			new org.opencv.core.Point(TARGET_SIZE, 0),
			new org.opencv.core.Point(TARGET_SIZE, TARGET_SIZE),
			new org.opencv.core.Point(0, TARGET_SIZE));

		MatOfPoint2f source = new MatOfPoint2f(
			toOpenCvPoint(perspectivePoints.get(0)),
			toOpenCvPoint(perspectivePoints.get(1)),
			toOpenCvPoint(perspectivePoints.get(2)),
			toOpenCvPoint(perspectivePoints.get(3)));

		Mat transformation = getPerspectiveTransform(source, destination);

		warpPerspective(frame, targetFrame, transformation, new Size(TARGET_SIZE, TARGET_SIZE));

		org.opencv.core.Point point = features.getCentroid().orElse(null);
		if (point != null && shotDetected) {
			points.add(point);
			while (points.size() > 5)
				points.removeFirst();
		}
		for (org.opencv.core.Point p : points)
			Core.circle(privateFrame, p, 7, new Scalar(0, 0, 255), 2);

		currentFrameComponent.update(privateFrame, features);
		targetFrameComponent.update(targetFrame);

		jframe.repaint();

		if (!jframe.isVisible()) {
			jframe.pack();
			jframe.setVisible(true);
		}
	}

	private static org.opencv.core.Point toOpenCvPoint(Point p) {
		return new org.opencv.core.Point(p.getX(), p.getY());
	}

	public static BufferedImage toBufferedImage(Mat m) {
		int type = BufferedImage.TYPE_BYTE_GRAY;
		if (m.channels() > 1) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		int bufferSize = m.channels() * m.cols() * m.rows();
		byte[] b = new byte[bufferSize];
		m.get(0, 0, b); // get all the pixels
		BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(b, 0, targetPixels, 0, b.length);
		return image;
	}
}

