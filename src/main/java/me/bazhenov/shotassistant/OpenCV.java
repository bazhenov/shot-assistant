package me.bazhenov.shotassistant;

import org.opencv.core.Core;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

import java.io.IOException;

import static me.bazhenov.shotassistant.Sensitivity.HIGH;
import static me.bazhenov.shotassistant.Sensitivity.LOW;
import static me.bazhenov.shotassistant.Sensitivity.MEDIUM;
import static org.opencv.highgui.Highgui.CV_CAP_PROP_FRAME_HEIGHT;
import static org.opencv.highgui.Highgui.CV_CAP_PROP_FRAME_WIDTH;

public class OpenCV {

	public static void main(String[] args) throws IOException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		VideoCapture c = new VideoCapture(0);
		/*c.set(CV_CAP_PROP_FRAME_WIDTH, 320);
		c.set(CV_CAP_PROP_FRAME_HEIGHT, 240);*/
		c.set(CV_CAP_PROP_FRAME_WIDTH, 1024);
		c.set(CV_CAP_PROP_FRAME_HEIGHT, 768);
		//VideoCapture c = new VideoCapture("./laser3.mov");

		FrameProcessingLoop loop = new FrameProcessingLoop(c);
		loop.addFrameListener(new VisualizingListener());
		//loop.setSlowDownForFrameRate(17);
		loop.setSensitivity(MEDIUM);
		loop.run(new PrintShotListener());
		//loop.run(new AnnounceShotListener());
	}
}