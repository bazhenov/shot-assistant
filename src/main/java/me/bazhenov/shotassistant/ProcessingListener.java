package me.bazhenov.shotassistant;

import org.opencv.core.Mat;

import java.util.Map;

public interface ProcessingListener {

	public void onStage(ProcessingChain<?> chain, String name, Mat processingResult);

	void onFrame(Mat mat);

	void onFrameComplete(Map<ProcessingChain<?>, Object> features);
}
