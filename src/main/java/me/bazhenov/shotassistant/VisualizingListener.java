package me.bazhenov.shotassistant;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.LinkedList;
import java.util.List;

import static java.awt.Color.green;
import static java.awt.Color.white;
import static java.util.Arrays.asList;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import static me.bazhenov.shotassistant.VisualizingListener.toBufferedImage;
import static org.opencv.imgproc.Imgproc.cvtColor;

public class VisualizingListener implements FrameListener {

	private JFrame jframe = null;
	private OpenCvVideoComponent currentFrame;
	private Mat privateFrame = new Mat();
	LinkedList<Point> points = new LinkedList<>();
	private int shots = 0;

	@Override
	public void onFrame(Mat mat, Mat background, Mat frame, FrameFeatures features, boolean shotDetected) {
		int width = mat.width();
		int height = mat.height();

		if (jframe == null) {
			jframe = new JFrame();

			jframe.setDefaultCloseOperation(EXIT_ON_CLOSE);

			currentFrame = new OpenCvVideoComponent();
			//jframe.add(currentFrame);
			PerspectiveComponent comp = new PerspectiveComponent(currentFrame);
			comp.setSize(new Dimension(width, height));
			jframe.add(comp);
			jframe.setSize(new Dimension(width, height));
			jframe.setVisible(true);
		}

		if (shotDetected)
			shots++;

		cvtColor(frame, privateFrame, Imgproc.COLOR_GRAY2BGR);

		Point point = features.getCentroid().orElse(null);
		if (point != null && shotDetected) {
			points.add(point);
			while (points.size() > 5)
				points.removeFirst();
		}
		for (Point p : points) {
			Core.circle(privateFrame, p, 7, new Scalar(0, 0, 255), 2);
		}

		Graphics g = jframe.getGraphics();
		/*Core.putText(privateFrame, "Frame: " + Integer.toString(features.getFrameNo()), new Point(10, 50), FONT_HERSHEY_COMPLEX, 0.4, new Scalar(255, 255, 255));
		Core.putText(privateFrame, "Shots:" + Integer.toString(shots), new Point(10, 35), FONT_HERSHEY_COMPLEX, 0.4, new Scalar(255, 255, 255));*/
		/*g.drawImage(toBufferedImage(privateFrame), 0, 0, null, null);
		g.drawImage(toBufferedImage(background), width, 0, null, null);
		g.drawImage(toBufferedImage(mat), 0, height, null, null);*/
		currentFrame.update(privateFrame, features);
		jframe.repaint();
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

class OpenCvVideoComponent extends Component {

	private volatile BufferedImage image;
	private volatile Dimension dimension;
	private volatile FrameFeatures f;

	public void update(Mat i, FrameFeatures f) {
		image = toBufferedImage(i);
		if (dimension == null || (dimension.getWidth() != i.width() || dimension.getHeight() != i.height()))
			dimension = new Dimension(i.width(), i.height());
		this.f = f;
	}

	@Override
	public Dimension getSize() {
		return dimension;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (image != null) {
			g.drawImage(image, 0, 0, null, null);
			g.setColor(green);
			g.setFont(getFont().deriveFont(10.0f));
			g.drawString("FNo: " + f.getFrameNo(), 10, 20);
			g.drawString(" CA: " + f.getCoveringArea(), 10, 35);
			g.drawString("NZP: " + f.getNonZeroPixels(), 10, 50);
		}
	}
}

class PerspectiveComponent extends Container {

	private final Component delegate;

	PerspectiveComponent(Component delegate) {
		this.delegate = delegate;
		add(delegate);
	}

	@Override
	public Dimension getSize() {
		return delegate.getSize();
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		delegate.paint(g);
		g.setColor(white);

		Point p1 = new Point(10, 20);
		Point p2 = new Point(310, 25);
		Point p3 = new Point(310, 150);
		Point p4 = new Point(32, 210);

		List<Point> pts = asList(p1, p2, p3, p4);
		pts.forEach(p -> g.drawRect((int) p.x, (int) p.y, 10, 10));

		for (int i = 0; i < pts.size() - 1; i++) {
			List<Point> pLine = pts.subList(i, i + 2);
			g.drawLine((int) pLine.get(0).x + 5, (int) pLine.get(0).y + 5, (int) pLine.get(1).x + 5, (int) pLine.get(1).y + 5);
		}
	}
}