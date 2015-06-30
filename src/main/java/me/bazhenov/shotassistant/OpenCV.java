package me.bazhenov.shotassistant;

import org.opencv.core.Core;
import org.opencv.highgui.VideoCapture;

import java.io.IOException;

public class OpenCV {

	public static void main(String[] args) throws IOException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		//VideoCapture c = new VideoCapture(0);
		VideoCapture c = new VideoCapture("/Users/bazhenov/Desktop/shot-assistant/laser.mov");

		FrameProcessingLoop loop = new FrameProcessingLoop(c);
		loop.addFrameListener(new VisualizingListener());
		loop.run();
	}
}