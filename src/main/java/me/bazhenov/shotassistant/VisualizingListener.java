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
import java.util.function.Consumer;

import static java.awt.Color.green;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import static me.bazhenov.shotassistant.VisualizingListener.toBufferedImage;
import static org.opencv.imgproc.Imgproc.*;

public class VisualizingListener implements FrameListener {

	private static final int TARGET_SIZE = 240;
	private JFrame jframe = null;
	private MainFrameComponent currentFrameComponent;
	private OpenCvVideoComponent targetFrameComponent;
	private Mat privateFrame = new Mat();
	private Mat targetFrame = new Mat();
	LinkedList<org.opencv.core.Point> points = new LinkedList<>();
	private int shots = 0;
	private volatile List<Point> perspectivePoints;

	private Consumer<List<java.awt.Point>> perspectiveListener = p -> {
		System.out.println(p);
		this.perspectivePoints = p;
	};

	/*public VisualizingListener(Consumer<List<java.awt.Point>> perspectiveListener) {
		this.perspectiveListener = requireNonNull(perspectiveListener);
	}*/

	@Override
	public void onFrame(Mat mat, Mat background, Mat frame, FrameFeatures features, boolean shotDetected) {
		int width = mat.width();
		int height = mat.height();

		if (jframe == null) {
			jframe = new JFrame();

			jframe.setDefaultCloseOperation(EXIT_ON_CLOSE);

			currentFrameComponent = new MainFrameComponent();
			targetFrameComponent = new TargetFrameComponent();
			PerspectiveComponent comp = new PerspectiveComponent(currentFrameComponent, perspectiveListener);
			comp.setSize(new Dimension(width, height));
			targetFrameComponent.setSize(new Dimension(TARGET_SIZE, TARGET_SIZE));
			targetFrameComponent.setLocation(width, 0);
			jframe.add(targetFrameComponent);
			jframe.add(comp);
			jframe.setSize(new Dimension(width + TARGET_SIZE, height));
			jframe.setVisible(true);
		}

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
		for (org.opencv.core.Point p : points) {
			Core.circle(privateFrame, p, 7, new Scalar(0, 0, 255), 2);
		}

		Graphics g = jframe.getGraphics();
		/*Core.putText(privateFrame, "Frame: " + Integer.toString(features.getFrameNo()), new Point(10, 50), FONT_HERSHEY_COMPLEX, 0.4, new Scalar(255, 255, 255));
		Core.putText(privateFrame, "Shots:" + Integer.toString(shots), new Point(10, 35), FONT_HERSHEY_COMPLEX, 0.4, new Scalar(255, 255, 255));*/
		/*g.drawImage(toBufferedImage(privateFrame), 0, 0, null, null);
		g.drawImage(toBufferedImage(background), width, 0, null, null);
		g.drawImage(toBufferedImage(mat), 0, height, null, null);*/
		currentFrameComponent.update(privateFrame, features);
		targetFrameComponent.update(targetFrame);
		jframe.repaint();
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

