package me.bazhenov.shotassistant;

import org.opencv.core.Mat;

import java.util.function.Consumer;

public interface ProcessingListener {

	public void onStage(int i, String name, Mat processingResult);

	void onFrame(Mat mat, int size);

	void onFrameComplete();
}
