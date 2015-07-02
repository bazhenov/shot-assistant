package me.bazhenov.shotassistant;

import org.opencv.core.Mat;
import org.opencv.core.Point;

public interface FrameListener {

	void onFrame(Mat mat, Mat background, Mat frame, FrameFeatures point, boolean shots);
}
