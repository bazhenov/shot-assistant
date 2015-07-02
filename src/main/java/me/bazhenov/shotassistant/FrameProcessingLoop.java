package me.bazhenov.shotassistant;

import org.opencv.core.*;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static me.bazhenov.shotassistant.Sensitivity.MEDIUM;
import static me.bazhenov.shotassistant.ShotDetectingAutomata.State.Shot;
import static org.opencv.core.Core.countNonZero;
import static org.opencv.core.Core.findNonZero;
import static org.opencv.core.CvType.*;
import static org.opencv.highgui.Highgui.CV_CAP_PROP_FRAME_HEIGHT;
import static org.opencv.highgui.Highgui.CV_CAP_PROP_FRAME_WIDTH;
import static org.opencv.imgproc.Imgproc.*;

public class FrameProcessingLoop {

	private final VideoCapture videoCapture;
	private final List<FrameListener> listeners = new ArrayList<>();
	private Sensitivity sensitivity = MEDIUM;
	private int frameRate = 0;

	public FrameProcessingLoop(VideoCapture videoCapture) {
		this.videoCapture = requireNonNull(videoCapture);
	}

	public void addFrameListener(FrameListener listener) {
		listeners.add(listener);
	}

	public void run(ShotListener shotListener) {
		int width = (int) videoCapture.get(CV_CAP_PROP_FRAME_WIDTH);
		int height = (int) videoCapture.get(CV_CAP_PROP_FRAME_HEIGHT);

		Mat frame = new Mat(width, height, CV_32FC3);
		Mat frameResult = new Mat(width, height, CV_8UC1);
		Mat background = null;
		Features f = new Features();
		ShotDetector detector = new ShotDetector(sensitivity);
		long start = System.nanoTime();

		MatOfPoint2f destination = new MatOfPoint2f(new Point(0, 0), new Point(width, 0), new Point(width, height), new Point(0, height));
		MatOfPoint2f source = new MatOfPoint2f(new Point(180, 70), new Point(314, 134), new Point(315, 254), new Point(173, 214));
		Mat transformation = getPerspectiveTransform(source, destination);

		int frameNo = 0;
		int lastRebalancingFrameNo = 0;
		while (videoCapture.read(frame)) {
			frameNo++;
			System.out.println("Frame #" + (frameNo++));
			//warpPerspective(frame, frame, transformation, new Size(width, height));
			cvtColor(frame, frame, Imgproc.COLOR_RGB2GRAY);
			//normalize(frame, frame, 10, 245, NORM_MINMAX);
			//equalizeHist(frame, frame);

			if (sensitivity.getBlurLevel() > 0)
				Imgproc.medianBlur(frame, frame, sensitivity.getBlurLevel());

			if (background == null) {
				background = new Mat(width, height, CV_8UC1);
				frame.copyTo(background);
			}
			Core.subtract(frame, background, frameResult);

			if (sensitivity.getThresholdValue() > 0)
				threshold(frameResult, frameResult, sensitivity.getThresholdValue(), 255, THRESH_BINARY);

			if (sensitivity.getDilateErodeLevel() > 0)
				erode(frameResult, frameResult, getStructuringElement(MORPH_ELLIPSE,
					new Size(sensitivity.getDilateErodeLevel(), sensitivity.getDilateErodeLevel())));

			FrameFeatures features = f.extract(frameResult);

			boolean shotDetected = false;
			if (features.getCoveringArea() > 10000) {
				// rebalancing
				frame.copyTo(background);
				lastRebalancingFrameNo = frameNo;
			} else {
				shotDetected = detector.detectShot(features);
				if (shotDetected) {
					shotListener.onShot(1);
				}
			}

			/*if (frameNo - lastRebalancingFrameNo < 20) {
				continue;
			}*/

			for (FrameListener listener : listeners) {
				try {
					listener.onFrame(frameResult, background, frame, features, shotDetected);
				} catch (RuntimeException e) {
					e.printStackTrace();
				}
			}

			long timeSpent = NANOSECONDS.toMillis(System.nanoTime() - start);
			double expectedDelay = 1000.0 / frameRate;
			if (frameRate > 0 && timeSpent < expectedDelay) {
//				System.out.println((int) expectedDelay - timeSpent);
				sleepUninterruptibly(((int) expectedDelay - timeSpent), MILLISECONDS);
			}
			start = System.nanoTime();
		}
	}

	public void setSensitivity(Sensitivity sensitivity) {
		this.sensitivity = requireNonNull(sensitivity);
	}

	public void setSlowDownForFrameRate(int frameRate) {
		this.frameRate = frameRate;
	}

	static class ShotDetector {

		private final ShotDetectingAutomata automata;

		public ShotDetector(Sensitivity sensitivity) {
			automata = new ShotDetectingAutomata(sensitivity.getCandidateDelay(), sensitivity.getCoolDownDelay());
		}


		public boolean detectShot(FrameFeatures f) {
			return automata.feed(f.getCentroid().isPresent()) == Shot;
		}
	}

	static class Features {

		MatOfPoint coordinates = new MatOfPoint();
		int frameNo = 0;

		public FrameFeatures extract(Mat frame) {
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
			return new FrameFeatures(frameNo++, frame.size(), nonZero, (int) area, Optional.ofNullable(point));
		}
	}
}
