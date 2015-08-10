package me.bazhenov.shotassistant;

import me.bazhenov.shotassistant.target.IpscScore;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.Optional;

public interface OldListener {

	void onFrame(Mat mat, Mat background, Mat frame, FrameFeatures point, Optional<?> shot, ShotDetectingAutomata.State automataState);
}
