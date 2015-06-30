package me.bazhenov.shotassistant;

import org.opencv.core.*;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.opencv.core.Core.*;
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

		List<Integer> markedFrames;
		try {
			markedFrames = Files.lines(new File("/Users/bazhenov/Desktop/shot-assistant/frames-with-laser.csv").toPath())
				.map(Integer::parseInt)
				.collect(toList());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

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
			int nonZero = countNonZero(frame);

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
				//Core.putText(frame, Double.toString(rect.size.area()), new Point(80, frame.rows() - 10), FONT_HERSHEY_COMPLEX, 0.6, new Scalar(255));
				//Core.rectangle(frame, p[0], p[2], new Scalar(150), 4);
			}

			if (markedFrames.contains(frameNo)) {
				//Core.rectangle(r, new Point(30, 30), new Point(60, 60), new Scalar(255));
				Core.putText(frame, "Match", new Point(10, frame.rows() - 10), FONT_HERSHEY_COMPLEX, 0.6, new Scalar(255));
			}

			for (FrameListener listener : listeners) {
				listener.onFrame(frame, background, frameResult, point);
			}

			System.out.println(frameNo + "," + nonZero + "," + area + "," + (markedFrames.contains(frameNo + 2) ? "yes" : "no"));

		}
	}
}
