package me.bazhenov.shotassistant;

import boofcv.struct.image.ImageSInt16;
import boofcv.struct.image.ImageUInt8;
import org.testng.annotations.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static boofcv.core.image.ConvertBufferedImage.convertFrom;
import static boofcv.io.image.UtilImageIO.loadImage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

public class PointDetectorTest {

/*
	@Test
	public void foo() {
		BufferedImage img = loadImage(getClass().getResource("/laser2.jpg"));
		ImageSInt16 background = convertFrom(img, null, ImageSInt16.class);
		PointDetector detector = new PointDetector(background);

		ImageSInt16 frame = convertFrom(loadImage(getClass().getResource("/laser2-with.jpg")), null, ImageSInt16.class);
		Point point = detector.detect(frame);
		assertThat(point.getX(), closeTo(490, 5));
		assertThat(point.getY(), closeTo(130, 5));
	}
*/
}