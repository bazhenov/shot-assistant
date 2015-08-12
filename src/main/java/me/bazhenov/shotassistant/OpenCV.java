package me.bazhenov.shotassistant;

import me.bazhenov.shotassistant.target.IpscClassicalTarget;
import org.opencv.core.Core;
import org.opencv.highgui.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import static me.bazhenov.shotassistant.ProcessingChain.createProcessingChain;

public class OpenCV {

	public static void loadOpenCv() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static void main(String[] args) throws IOException {
		loadOpenCv();
		IpscClassicalTarget target = new IpscClassicalTarget();

		/*VideoCapture c = new VideoCapture(0);
		c.set(CV_CAP_PROP_FRAME_WIDTH, width);
		c.set(CV_CAP_PROP_FRAME_HEIGHT, height);*/
		String fileName = "./examples/laser7.mov";
		VideoCapture c = new VideoCapture(fileName);
		MovieInfo info = new MovieInfo(new File(fileName + ".info"));

		ShotDetector shotDetector = new ShotDetector(target, new AnnounceShotListener());
		TargetFrameComponent targetFrameComponent = new TargetFrameComponent(target);
		ProcessingChain chain = createProcessingChain(info.getPerspectivePoints(), target, shotDetector);
		chain.setTargetFrameListener(targetFrameComponent);

		JFrame jframe = new JFrame();

		jframe.setDefaultCloseOperation(EXIT_ON_CLOSE);
		jframe.add(targetFrameComponent);

		jframe.setLayout(new FlowLayout());
		jframe.pack();
		jframe.setVisible(true);

		chain.run(c);
	}
}