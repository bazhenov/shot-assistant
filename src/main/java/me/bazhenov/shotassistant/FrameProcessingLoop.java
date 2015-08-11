package me.bazhenov.shotassistant;

import me.bazhenov.shotassistant.target.IpscScore;
import me.bazhenov.shotassistant.target.Target;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static me.bazhenov.shotassistant.Sensitivity.MEDIUM;
import static me.bazhenov.shotassistant.ShotDetectingAutomata.State;
import static me.bazhenov.shotassistant.ShotDetectingAutomata.State.Shot;
import static me.bazhenov.shotassistant.VisualizingListener.toOpenCvPoint;
import static org.opencv.core.Core.*;
import static org.opencv.core.CvType.*;
import static org.opencv.highgui.Highgui.CV_CAP_PROP_FRAME_HEIGHT;
import static org.opencv.highgui.Highgui.CV_CAP_PROP_FRAME_WIDTH;
import static org.opencv.imgproc.Imgproc.*;

public class FrameProcessingLoop {

	private final VideoCapture videoCapture;
	private final List<OldListener> listeners = new ArrayList<>();
	private Sensitivity sensitivity = MEDIUM;
	private int frameRate = 0;
	private Target t;
	private Mat perspectiveTransform;
	private Dimension targetSize;
	private double targetScaleFactor;

	public FrameProcessingLoop(VideoCapture videoCapture, Target t) {
		this.t = t;
		targetSize = t.getSize();
		targetScaleFactor = 1;
		this.videoCapture = requireNonNull(videoCapture);
		setPerspective(newArrayList(
			new java.awt.Point(0, 0),
			new java.awt.Point(100, 0),
			new java.awt.Point(100, 100),
			new java.awt.Point(0, 100)
		));
	}

	public void addFrameListener(OldListener listener) {
		listeners.add(listener);
	}

	public void setPerspective(List<java.awt.Point> p) {
		MatOfPoint2f destination = new MatOfPoint2f(
			new org.opencv.core.Point(0, 0),
			new org.opencv.core.Point(targetSize.getWidth() * targetScaleFactor, 0),
			new org.opencv.core.Point(targetSize.getWidth() * targetScaleFactor, targetSize.getHeight() * targetScaleFactor),
			new org.opencv.core.Point(0, targetSize.getHeight() * targetScaleFactor));

		MatOfPoint2f source = new MatOfPoint2f(
			toOpenCvPoint(p.get(0)),
			toOpenCvPoint(p.get(1)),
			toOpenCvPoint(p.get(2)),
			toOpenCvPoint(p.get(3)));

		perspectiveTransform = getPerspectiveTransform(source, destination);
	}

	public void run(ShotListener<IpscScore> shotListener) {
		int width = (int) videoCapture.get(CV_CAP_PROP_FRAME_WIDTH);
		int height = (int) videoCapture.get(CV_CAP_PROP_FRAME_HEIGHT);

		Mat frame = new Mat(width, height, CV_32FC3);
		Mat frameResult = new Mat(width, height, CV_8UC1);
		Mat resizedFrame = new Mat();
		Mat background = null;
		Features f = new Features();
		long start = System.nanoTime();
		Mat target = new Mat();

		int frameNo = 0;
		int lastRebalancingFrameNo = 0;
		ShotDetectingAutomata automata = new ShotDetectingAutomata(sensitivity.getCandidateDelay(), sensitivity.getCoolDownDelay());
		List<Mat> channels = newArrayList(new Mat(), new Mat(), new Mat());
		while (videoCapture.read(frame)) {
			frameNo++;
			//Core.split(frame, channels);
			//frame = channels.get(2);
			cvtColor(frame, frame, Imgproc.COLOR_RGB2GRAY);

			readTarget(frame, target);

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

			// Shot detecting
			Point point = null;//features.getCentroid().orElse(null);
			Optional<IpscScore> score = Optional.empty();


			State automataState = automata.feed(point != null && correctFeatures(features));
			boolean shotDetected = false;
			if (features.getCoveringArea() > 100 && false) {
				// rebalancing
				frame.copyTo(background);
				lastRebalancingFrameNo = frameNo;
			} else if (point != null) {

				shotDetected = (automataState == Shot);
				if (shotDetected) {
					MatOfPoint2f shotCoordinates = new MatOfPoint2f();
					perspectiveTransform(new MatOfPoint2f(point), shotCoordinates, perspectiveTransform);
					double[] doubles = shotCoordinates.get(0, 0);
					double targetScaleFactor = 1;
					int sx = (int) Math.round(doubles[0] / targetScaleFactor);
					int sy = (int) Math.round(doubles[1] / targetScaleFactor);

					score = t.scoreShot(new java.awt.Point(sx, sy));
					shotListener.onShot(score);
				}
			}

			//resize(frame, resizedFrame, new Size(320, 240));
			for (OldListener listener : listeners) {
				try {
					listener.onFrame(target, frameResult, frame, features, score, automataState);
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

	private boolean correctFeatures(FrameFeatures features) {
		return features.getCoveringArea() <= 100 && features.getCoveringArea() > 0;
	}

	private void readTarget(Mat frame, Mat target) {
		warpPerspective(frame, target, perspectiveTransform, new Size(targetSize.getWidth() * targetScaleFactor,
			targetSize.getHeight() * targetScaleFactor));
	}

	public void setSensitivity(Sensitivity sensitivity) {
		this.sensitivity = requireNonNull(sensitivity);
	}

	public void setSlowDownForFrameRate(int frameRate) {
		this.frameRate = frameRate;
	}

	static class Features {

		MatOfPoint coordinates = new MatOfPoint();
		int frameNo = 1;

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
			return new FrameFeatures(frameNo++, frame.size(), nonZero, (int) area, Optional.ofNullable(null));
		}
	}
}
