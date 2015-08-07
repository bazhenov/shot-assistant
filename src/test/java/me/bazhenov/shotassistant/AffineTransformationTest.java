package me.bazhenov.shotassistant;

import org.testng.annotations.Test;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import static java.awt.geom.AffineTransform.getTranslateInstance;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class AffineTransformationTest {

	@Test
	public void translateTest() {
		AffineTransform t = getTranslateInstance(10, 20);
		Point2D result = t.transform(new Point(0, 18), null);

		assertThat(result.getX(), is(10d));
		assertThat(result.getY(), is(38d));
	}
}
