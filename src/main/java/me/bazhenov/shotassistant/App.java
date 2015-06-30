package me.bazhenov.shotassistant;

import boofcv.struct.image.ImageSInt16;
import com.github.sarxos.webcam.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;

import static boofcv.core.image.ConvertBufferedImage.convertFrom;

/**
 * Hello world!
 */
public class App {
/*
	public static void main(String[] args) throws IOException, InterruptedException {
		Webcam wc = Webcam.getDefault();
		wc.setViewSize(WebcamResolution.VGA.getSize());
		wc.setImageTransformer(new WebcamImageTransformer() {
			@Override
			public BufferedImage transform(BufferedImage image) {
				BufferedImage gray = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

				Graphics2D g = gray.createGraphics();
				g.drawImage(image, 0, 0, null);
				return gray;
			}
		});

		wc.addWebcamListener(new Listener());
		wc.open(true);
		Thread.sleep(60000);
	}

	private static class Listener implements WebcamListener {

		private PointDetector detector;
		private ImageSInt16 frame;

		@Override
		public void webcamOpen(WebcamEvent we) {
			System.out.println("Webcam open");
		}

		@Override
		public void webcamClosed(WebcamEvent we) {
			System.out.println("Webcam closed");
		}

		@Override
		public void webcamDisposed(WebcamEvent we) {
			System.out.println("Webcam disposed");
		}

		@Override
		public void webcamImageObtained(WebcamEvent we) {
			BufferedImage img = we.getImage();
			if (detector == null) {
				System.out.println("Calibrating");
				ImageSInt16 base = convertFrom(img, null, ImageSInt16.class);
				frame = new ImageSInt16(img.getWidth(), img.getHeight());
				detector = new PointDetector(base);
			} else {
				try {
					convertFrom(img, frame, ImageSInt16.class);
					Point point = detector.detect(frame);
					System.out.println(new Date().toString() + " Image obtained: " + point);
				} catch (Exception e) {
					e.printStackTrace(System.out);
				}
			}
		}
	}
	*/
}
