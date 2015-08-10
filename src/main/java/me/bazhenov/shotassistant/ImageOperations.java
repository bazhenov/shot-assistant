package me.bazhenov.shotassistant;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.function.Consumer;

import static org.opencv.core.Core.subtract;
import static org.opencv.imgproc.Imgproc.*;

public class ImageOperations {


	public static Consumer<Mat> grayscale() {
		return f -> {
			cvtColor(f, f, Imgproc.COLOR_RGB2GRAY);
		};
	}

	public static Consumer<Mat> medianBlur(int blurLevel) {
		return f -> {
			Imgproc.medianBlur(f, f, blurLevel);
		};
	}

	public static Consumer<Mat> threshold(int threshold) {
		return f -> {
			Imgproc.threshold(f, f, threshold, 255, THRESH_BINARY);
		};
	}

	public static Consumer<Mat> erode(int erode) {
		return f -> {
			Imgproc.erode(f, f, getStructuringElement(MORPH_ELLIPSE, new Size(erode, erode)));
		};
	}

	public static Consumer<Mat> differenceWithBackground() {
		return new DifferenceWithBackground();
	}

	public static class DifferenceWithBackground implements Consumer<Mat> {

		private Mat background;

		@Override
		public void accept(Mat f) {
			if (background == null) {
				background = new Mat();
				f.copyTo(background);
			}
			subtract(f, background, f);
		}
	}
}
