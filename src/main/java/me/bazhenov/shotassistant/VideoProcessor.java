package me.bazhenov.shotassistant;

import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static org.opencv.core.CvType.CV_32FC3;
import static org.opencv.highgui.Highgui.CV_CAP_PROP_FRAME_HEIGHT;
import static org.opencv.highgui.Highgui.CV_CAP_PROP_FRAME_WIDTH;

public class VideoProcessor {

	public static void run(VideoCapture videoCapture, Consumer<Mat> consumer) {
		int width = (int) videoCapture.get(CV_CAP_PROP_FRAME_WIDTH);
		int height = (int) videoCapture.get(CV_CAP_PROP_FRAME_HEIGHT);

		Mat frame = new Mat(width, height, CV_32FC3);

		while (videoCapture.read(frame)) {
			consumer.accept(frame);
		}
	}
}
