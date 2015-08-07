package me.bazhenov.shotassistant;

import me.bazhenov.shotassistant.target.IpscClassicalTarget;
import me.bazhenov.shotassistant.target.IpscScore;
import me.bazhenov.shotassistant.target.Target;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.*;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import static org.opencv.core.Core.perspectiveTransform;
import static org.opencv.imgproc.Imgproc.*;

public class VisualizingListener implements FrameListener {

	private static final int TARGET_SIZE = 240;
	private final Target target;
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
		target = new IpscClassicalTarget();
		targetFrameComponent = new TargetFrameComponent(target);
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
		if (jframe != null)
			jframe.repaint();
	}

	@Override
	public void onFrame(Mat mat, Mat background, Mat frame, FrameFeatures features, boolean shotDetected) {
		if (shotDetected)
			shots++;

		cvtColor(frame, privateFrame, Imgproc.COLOR_GRAY2BGR);

		Dimension targetSize = target.getSize();
		double targetScaleFactor = Math.min(300 / targetSize.getWidth(), 300 / targetSize.getHeight());

		MatOfPoint2f destination = new MatOfPoint2f(
			new org.opencv.core.Point(0, 0),
			new org.opencv.core.Point(targetSize.getWidth() * targetScaleFactor, 0),
			new org.opencv.core.Point(targetSize.getWidth() * targetScaleFactor, targetSize.getHeight() * targetScaleFactor),
			new org.opencv.core.Point(0, targetSize.getHeight() * targetScaleFactor));

		MatOfPoint2f source = new MatOfPoint2f(
			toOpenCvPoint(perspectivePoints.get(0)),
			toOpenCvPoint(perspectivePoints.get(1)),
			toOpenCvPoint(perspectivePoints.get(2)),
			toOpenCvPoint(perspectivePoints.get(3)));

		Mat transformation = getPerspectiveTransform(source, destination);

		warpPerspective(frame, targetFrame, transformation, new Size(targetSize.getWidth() * targetScaleFactor, targetSize.getHeight() * targetScaleFactor));

		org.opencv.core.Point point = features.getCentroid().orElse(null);
		if (point != null && shotDetected) {
			points.add(point);
			while (points.size() > 5)
				points.removeFirst();

			MatOfPoint2f shotCoordinates = new MatOfPoint2f();
			perspectiveTransform(new MatOfPoint2f(point), shotCoordinates, transformation);
			double[] doubles = shotCoordinates.get(0, 0);
			int sx = (int) Math.round(doubles[0] / targetScaleFactor);
			int sy = (int) Math.round(doubles[1] / targetScaleFactor);
			System.out.println(sx + "," + sy);
			Optional<IpscScore> score = target.scoreShot(new Point(sx, sy));
			System.out.println(score.toString());
			try {
				Runtime.getRuntime().exec("say -r 280 '" + score.map(Enum::toString).orElse("miss") + "'");
			} catch (IOException e) {
				System.err.println(e.toString());
			}
		}
		for (org.opencv.core.Point p : points)
			Core.circle(privateFrame, p, 7, new Scalar(0, 0, 255), 2);

		// Принудительно снижаем framerate, чтобы CPU меньше елось
		if (Math.random() < 0.1) {
			currentFrameComponent.update(privateFrame, features);
			targetFrameComponent.update(targetFrame);

			jframe.repaint();
		}

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
		byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(b, 0, targetPixels, 0, b.length);
		return image;
	}
}

