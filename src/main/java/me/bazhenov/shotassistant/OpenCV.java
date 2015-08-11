package me.bazhenov.shotassistant;

import me.bazhenov.shotassistant.drills.Drill;
import me.bazhenov.shotassistant.drills.FirstShotDrill;
import me.bazhenov.shotassistant.target.IpscClassicalTarget;
import org.opencv.core.Core;
import org.opencv.highgui.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import static me.bazhenov.shotassistant.Sensitivity.MEDIUM;

public class OpenCV {

	public static void loadOpenCv() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static void main(String[] args) throws IOException {
		loadOpenCv();
		IpscClassicalTarget target = new IpscClassicalTarget();

		int width = 640;
		int height = 480;
		/*VideoCapture c = new VideoCapture(0);
		c.set(CV_CAP_PROP_FRAME_WIDTH, width);
		c.set(CV_CAP_PROP_FRAME_HEIGHT, height);*/
		VideoCapture c = new VideoCapture("./examples/laser5.mov");

		FrameProcessingLoop loop = new FrameProcessingLoop(c, target);

		TargetFrameComponent targetFrameComponent = new TargetFrameComponent(target);
		MainFrameComponent currentFrameComponent = new MainFrameComponent();
		currentFrameComponent.setPreferredSize(new Dimension(width, height));
		PerspectiveComponent perspectiveComponent = new PerspectiveComponent(currentFrameComponent, loop::setPerspective);

		JFrame jframe = new JFrame();

		Drill drill = new FirstShotDrill();

		jframe.setDefaultCloseOperation(EXIT_ON_CLOSE);
		jframe.add(targetFrameComponent);
		jframe.add(perspectiveComponent);

		jframe.setLayout(new FlowLayout());
		jframe.pack();
		jframe.setVisible(true);

		loop.addFrameListener(new VisualizingListener(jframe, currentFrameComponent, targetFrameComponent));
		//loop.setSlowDownForFrameRate(17);
		loop.setSensitivity(MEDIUM);
		loop.run(new AnnounceShotListener());
	}
}