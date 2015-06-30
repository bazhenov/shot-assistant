package me.bazhenov.shotassistant;

import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.gui.image.ShowImages;
import boofcv.struct.image.ImageSInt16;
import boofcv.struct.image.ImageUInt8;

import java.awt.*;
import java.awt.image.BufferedImage;

import static boofcv.alg.misc.GPixelMath.subtract;
import static boofcv.core.image.ConvertBufferedImage.convertFrom;
import static boofcv.io.image.UtilImageIO.loadImage;

public class BoofCV {

	public static void main(String[] args) {
		/*BufferedImage img = loadImage("/Users/bazhenov/Desktop/laser2.jpg");
		ImageUInt8 base = convertFrom(img, new ImageUInt8(img.getWidth(), img.getHeight()));

		ImageUInt8 frame = convertFrom(loadImage("/Users/bazhenov/Desktop/laser2-with.jpg"), new ImageUInt8(img.getWidth(), img.getHeight()));

		PointDetector detector = new PointDetector(base);

		Point p = detector.detect(null);*/

		//UtilImageIO.saveImage(img, "/Users/bazhenov/Desktop/foo.jpg");

		//ShowImages.showWindow(img, "foo");
	}
}

