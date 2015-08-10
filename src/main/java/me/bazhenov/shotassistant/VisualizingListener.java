package me.bazhenov.shotassistant;

import org.opencv.core.Mat;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.LinkedList;
import java.util.Optional;

public class VisualizingListener implements OldListener {

	private final JFrame jframe;
	private final MainFrameComponent currentFrameComponent;
	private final OpenCvVideoComponent targetFrameComponent;
	private Mat privateFrame = new Mat();
	private Mat targetFrame = new Mat();
	LinkedList<org.opencv.core.Point> points = new LinkedList<>();

	/*public VisualizingListener(Consumer<List<java.awt.Point>> perspectiveListener) {
		this.perspectiveListener = requireNonNull(perspectiveListener);
	}*/

	public VisualizingListener(JFrame jframe, MainFrameComponent currentFrameComponent, OpenCvVideoComponent targetFrameComponent) {
		this.jframe = jframe;
		this.currentFrameComponent = currentFrameComponent;
		this.targetFrameComponent = targetFrameComponent;
	}

	@Override
	public void onFrame(Mat mat, Mat background, Mat frame, FrameFeatures features, Optional<?> score, ShotDetectingAutomata.State automataState) {
/*
		if (score.isPresent()) {
			points.add(point);
			while (points.size() > 5)
				points.removeFirst();
		}
		for (org.opencv.core.Point p : points)
			Core.circle(privateFrame, p, 7, new Scalar(0, 0, 255), 2);
*/

		// Принудительно снижаем framerate, чтобы CPU меньше елось
		currentFrameComponent.update(frame, features);
		targetFrameComponent.update(mat);

		jframe.repaint();
	}

	public static org.opencv.core.Point toOpenCvPoint(Point p) {
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

