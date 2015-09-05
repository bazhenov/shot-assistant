package me.bazhenov.shotassistant;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.List;
import java.util.function.Consumer;

import static com.google.common.collect.Lists.newArrayList;
import static org.opencv.core.Core.*;
import static org.opencv.imgproc.Imgproc.*;

public class ImageOperations {


	public static Consumer<Mat> grayscale() {
		return f -> {
			cvtColor(f, f, Imgproc.COLOR_RGB2GRAY);
		};
	}

	public static Consumer<Mat> takeChannel(int channelNo) {
		List<Mat> channels = newArrayList(new Mat(), new Mat(), new Mat());
		return f -> {
			split(f, channels);
			channels.get(channelNo).copyTo(f);
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

	public static Consumer<Mat> adaptiveThreshold() {
		return f -> {
			Imgproc.adaptiveThreshold(f, f, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, 11, 6);
		};
	}

	public static Consumer<Mat> erode(int erode) {
		return f -> {
			Imgproc.erode(f, f, getStructuringElement(MORPH_ELLIPSE, new Size(erode, erode)));
		};
	}

	public static Consumer<Mat> dilate(int dilate) {
		return f -> {
			Imgproc.dilate(f, f, getStructuringElement(MORPH_ELLIPSE, new Size(dilate, dilate)));
		};
	}

	public static Consumer<Mat> differenceWithBackground() {
		return new DifferenceWithBackground();
	}

	public static class DifferenceWithBackground implements Consumer<Mat> {

		private Mat background;
		private Mat backgroundReversed = new Mat();

		@Override
		public void accept(Mat f) {
			if (background == null) {
				background = new Mat();
				f.copyTo(background);
				absdiff(background, new Scalar(256), backgroundReversed);
			}
			subtract(f, background, f);
			divide(f, backgroundReversed, f, 255);
		}
	}
}
