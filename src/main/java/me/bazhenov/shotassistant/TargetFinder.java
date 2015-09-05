package me.bazhenov.shotassistant;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static java.lang.Math.*;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static me.bazhenov.shotassistant.ImageOperations.grayscale;
import static org.opencv.imgproc.Imgproc.*;


public class TargetFinder {

	private static final Logger log = LoggerFactory.getLogger(TargetFinder.class);
	private Point[] lastDetected = null;
	private int framesWithoutTarget = 0;

	public Optional<Point[]> find(Mat frame) {
		List<Point[]> candidates = detectCandidates(frame);

		if (candidates.isEmpty()) {
			framesWithoutTarget++;
			if (lastDetected != null && framesWithoutTarget < 3) {
				return Optional.of(lastDetected);
			} else {
				return Optional.empty();
			}
		} else {
			framesWithoutTarget = 0;
			if (lastDetected != null) {
				candidates.sort(comparing(distanceTo(lastDetected)));
			}
			lastDetected = candidates.get(0);
			return Optional.of(candidates.get(0));
		}
	}

	public List<Point[]> detectCandidates(Mat frame) {

		grayscale().accept(frame);
		Imgproc.medianBlur(frame, frame, 1);
		//Imgproc.equalizeHist(frame, frame);
		//Imgproc.Canny(frame, frame, 60, 60 * 3);

		ImageOperations.adaptiveThreshold().accept(frame);
		ImageOperations.erode(3).accept(frame);
		ImageOperations.dilate(1).accept(frame);

		List<MatOfPoint> contours = new ArrayList<>();
		//return Optional.empty();
		findContours(frame, contours, new Mat(), RETR_LIST, CHAIN_APPROX_SIMPLE);

		final List<RotatedRect> perspectivePoints = newArrayList();
		final List<RotatedRect> bigEllipses = newArrayList();
		for (MatOfPoint c : contours) {
			if (c.height() < 5)
				continue;

			MatOfPoint2f pts = new MatOfPoint2f(c.toArray());
			RotatedRect rotatedRect = fitEllipse(pts);

			double aspectRation = rotatedRect.size.width / rotatedRect.size.height;
			if (aspectRation > 6 || aspectRation < 1.0 / 6) {
				continue;
			}

			double area = boundingRect(c).area();
			double cArea = contourArea(c);
			int frameArea = frame.height() * frame.width();
			double areaProportion = cArea / frameArea;
			if (area / cArea >= 2.5)
				continue;

			if (areaProportion > 0.001) {
				bigEllipses.add(rotatedRect);
				Core.ellipse(frame, rotatedRect, new Scalar(150));
			} else {
				perspectivePoints.add(rotatedRect);
				Core.ellipse(frame, rotatedRect, new Scalar(255), 1);
			}
		}
		log.debug("Big/Small: {}/{}", bigEllipses.size(), perspectivePoints.size());

		if (perspectivePoints.size() < 4)
			return Collections.emptyList();

		List<Point[]> result = newArrayList();
		for (RotatedRect targetCandidate : bigEllipses) {

			List<RotatedRect> perPointsCopy = newArrayList(perspectivePoints);
			Point center = targetCandidate.center;
			perPointsCopy.sort(comparing(distanceFrom((int) center.x, (int) center.y)));
			perPointsCopy = orderTopologically(perPointsCopy.subList(0, 4));

			Point[] pts = perPointsCopy.stream().map(r -> r.center).toArray(Point[]::new);

			MatOfPoint matOfPoint = new MatOfPoint(pts);
			Core.polylines(frame, newArrayList(matOfPoint), true, new Scalar(255));
			for (RotatedRect c : perPointsCopy) {
				Core.ellipse(frame, c, new Scalar(255), 2);
			}
			Core.putText(frame, "H", center, 1, 1, new Scalar(255));

			if (abs(angle(pts[0], pts[1]) - angle(pts[2], pts[3])) > 10)
				continue;
			if (abs(angle(pts[1], pts[2]) - angle(pts[0], pts[3])) > 10)
				continue;
			if (abs(angle(pts[0], pts[1]) - angle(pts[1], pts[2])) < 30 || abs(angle(pts[0], pts[1]) - angle(pts[1], pts[2])) > 150)
				continue;
			double aspect = distance(pts[1], pts[2]) / distance(pts[0], pts[1]);
			if (aspect > 4 || aspect < 0.25)
				continue;
			result.add(pts);
		}
		return result;
	}

	public static boolean alreadyExist(List<RotatedRect> alreadyVisited, RotatedRect rotatedRect) {
		for (RotatedRect i : alreadyVisited) {
			if (distance(i.center, rotatedRect.center) < 1) {
				return true;
			}
		}
		return false;
	}

	private static List<RotatedRect> orderTopologically(List<RotatedRect> i) {
		List<RotatedRect> sortedY = i.stream().sorted(comparing(r -> r.center.y)).collect(toList());

		List<RotatedRect> upperPoints = sortedY.subList(0, 2);
		List<RotatedRect> lowerPoints = sortedY.subList(2, 4);

		upperPoints.sort(comparing(p -> p.center.x));
		lowerPoints.sort(comparing(p -> -p.center.x));

		List<RotatedRect> result = newArrayListWithCapacity(4);
		result.addAll(upperPoints);
		result.addAll(lowerPoints);
		return result;
	}

	private Function<? super RotatedRect, Double> distanceFrom(int x, int y) {
		return r -> sqrt(pow(r.center.x - x, 2) + pow(r.center.y - y, 2));
	}

	private Function<? super Point[], Double> distanceTo(Point[] p) {
		return a -> {
			double result = 0;
			for (int i = 0; i < a.length; i++)
				result += sqrt(pow(a[i].x - p[i].x, 2) + pow(a[i].y - p[i].y, 2));
			return result;
		};
	}

	public static double angle(Point a1, Point a2) {
		return Math.atan((a2.y - a1.y) / (a2.x - a1.x)) * 180 / Math.PI;
	}

	public static double distance(Point a, Point b) {
		return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
	}
}
