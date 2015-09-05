package me.bazhenov.shotassistant;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.highgui.Highgui;
import org.testng.annotations.Test;

import java.util.Optional;

import static me.bazhenov.shotassistant.TargetFinder.angle;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

public class TargetFinderTest {

	static {
		OpenCV.loadOpenCv();
	}

	@Test
	public void testAngleBetweenLines() {
		assertThat(angle(new Point(0, 0), new Point(10, 10)), is(45.0));
		assertThat(angle(new Point(10, 10), new Point(0, 0)), is(45.0));
		assertThat(angle(new Point(0, 0), new Point(-10, 10)), is(-45.0));
		assertThat(angle(new Point(0, 0), new Point(10, 0)), is(0.0));
		assertThat(angle(new Point(0, 0), new Point(0, 10)), is(90.0));
		assertThat(angle(new Point(0, 0), new Point(-10, 0)), is(-0.0));
		assertThat(angle(new Point(0, 0), new Point(0, -10)), is(-90.0));
	}

	@Test
	public void findTarget() {
		Mat frame = Highgui.imread("./frames/00018.jpg");

		TargetFinder finder = new TargetFinder();

		Optional<Point[]> target = finder.find(frame);
		Highgui.imwrite("result.png", frame);
		assertThat(target.isPresent(), is(true));
		Point[] pts = target.get();

		assertThat(pts[0].x, closeTo(376.2, 0.1));
		assertThat(pts[0].y, closeTo(442.4, 0.1));

		assertThat(pts[1].x, closeTo(667.8, 0.1));
		assertThat(pts[1].y, closeTo(290.4, 0.1));

		assertThat(pts[2].x, closeTo(1001.8, 0.1));
		assertThat(pts[2].y, closeTo(503.5, 0.1));

		assertThat(pts[3].x, closeTo(710.9, 0.1));
		assertThat(pts[3].y, closeTo(721.2, 0.1));

	}
}