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

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import static org.opencv.imgproc.Imgproc.cvtColor;

public class VisualizingListener implements FrameListener {

	private JFrame jframe = null;
	private Mat privateFrame = new Mat();

	@Override
	public void onFrame(Mat mat, Mat background, Mat frame, Point point) {
		int width = mat.width();
		int height = mat.height();

		if (jframe == null) {
			jframe = new JFrame();

			jframe.setDefaultCloseOperation(EXIT_ON_CLOSE);
			jframe.setSize(width * 2, height * 2);
			jframe.setVisible(true);
		}

		cvtColor(frame, privateFrame, Imgproc.COLOR_GRAY2BGR);

		if (point != null) {
			Core.circle(privateFrame, point, 10, new Scalar(0, 0, 255), 1);
		}

		Graphics g = jframe.getGraphics();
		g.drawImage(toBufferedImage(mat), 0, 0, null, null);
		g.drawImage(toBufferedImage(background), width, 0, null, null);
		g.drawImage(toBufferedImage(privateFrame), 0, height, null, null);
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
