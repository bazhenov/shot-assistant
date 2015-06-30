package me.bazhenov.shotassistant;

import org.opencv.core.Point;
import org.opencv.core.Size;

import java.util.Optional;

public final class FrameFeatures {

	private final Size frameSize;
	private final int nonZeroPixels;
	private final int coveringArea;
	private final Optional<Point> centroid;

	public FrameFeatures(Size frameSize, int nonZeroPixels, int coveringArea, Optional<Point> centroid) {
		this.frameSize = frameSize;
		this.nonZeroPixels = nonZeroPixels;
		this.coveringArea = coveringArea;
		this.centroid = centroid;
	}

	public Optional<Point> getCentroid() {
		return centroid;
	}
}
