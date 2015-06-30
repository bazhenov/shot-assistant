package me.bazhenov.shotassistant;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.awt.*;

import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.threshold;

class PointDetector {

	private final Mat base;

	PointDetector(Mat base) {
		this.base = base;
	}

	public Point detect(Mat frame) {
		Mat tmp = new Mat(frame.rows(), frame.cols(), CV_8UC1);

		Core.absdiff(frame, base, tmp);
		threshold(tmp, tmp, 60, 255, THRESH_BINARY);

		int xSum = 0;
		int ySum = 0;
		int count = 0;
		/*for (int x = 0; x < base.getWidth(); x++)
			for (int y = 0; y < base.getHeight(); y++)
				if (result.get(x, y) > 0) {
					xSum += x;
					ySum += y;
					count++;
				}

		return new Point(xSum / count, ySum / count);*/
		return null;
	}
}
