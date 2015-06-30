package me.bazhenov.shotassistant;

import org.opencv.core.*;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.opencv.core.Core.countNonZero;
import static org.opencv.core.Core.findNonZero;
import static org.opencv.core.CvType.*;
import static org.opencv.highgui.Highgui.CV_CAP_PROP_FRAME_HEIGHT;
import static org.opencv.highgui.Highgui.CV_CAP_PROP_FRAME_WIDTH;
import static org.opencv.imgproc.Imgproc.*;

public class FrameProcessingLoop {

	private final VideoCapture videoCapture;
	private final List<FrameListener> listeners = new ArrayList<>();

	public FrameProcessingLoop(VideoCapture videoCapture) {
		this.videoCapture = requireNonNull(videoCapture);
	}

	public void addFrameListener(FrameListener listener) {
		listeners.add(listener);
	}

	public void run() {
		int width = (int) videoCapture.get(CV_CAP_PROP_FRAME_WIDTH);
		int height = (int) videoCapture.get(CV_CAP_PROP_FRAME_HEIGHT);

		int frameNo = 0;
		Mat frame = new Mat(width, height, CV_8UC(3));
		Mat frameResult = new Mat(width, height, CV_8UC1);
		Mat background = null;
		while (videoCapture.read(frame)) {
			frameNo++;
			cvtColor(frame, frame, Imgproc.COLOR_RGB2GRAY);
			//normalize(frame, frame, 10, 245, NORM_MINMAX);
			//equalizeHist(frame, frame);
			Imgproc.medianBlur(frame, frame, 3);

			if (background == null) {
				background = new Mat(width, height, CV_8UC1);
				frame.copyTo(background);
			}
			frame.copyTo(frameResult);
			Core.subtract(frame, background, frame);
			threshold(frame, frame, 20, 255, THRESH_BINARY);
			dilate(frame, frame, getStructuringElement(MORPH_ELLIPSE, new Size(5, 5)));

			FrameFeatures features = extractFeatures(frame);

			for (FrameListener listener : listeners) {
				listener.onFrame(frame, background, frameResult, features.getCentroid().orElse(null));
			}
		}
	}

	private FrameFeatures extractFeatures(Mat frame) {
		MatOfPoint coordinates = new MatOfPoint();
		findNonZero(frame, coordinates);

		MatOfPoint2f m = new MatOfPoint2f();

		coordinates.convertTo(m, CV_32FC2);
		double area = 0;
		Point point = null;
		if (m.size().area() > 0) {
			Point p[] = new Point[4];
			RotatedRect rect = minAreaRect(m);
			area = rect.size.area();
			rect.points(p);
			point = rect.center;
		}
		int nonZero = countNonZero(frame);
		return new FrameFeatures(frame.size(), nonZero, (int) area, Optional.ofNullable(point));
	}
}
