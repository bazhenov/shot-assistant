package me.bazhenov.shotassistant;

import me.bazhenov.shotassistant.drills.FirstShotDrill;
import me.bazhenov.shotassistant.target.IpscClassicalTarget;
import me.bazhenov.shotassistant.ui.StartDrillAction;
import org.opencv.core.Core;
import org.opencv.highgui.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import static com.google.common.collect.Lists.newArrayList;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import static me.bazhenov.shotassistant.ProcessingChain.createProcessingChain;
import static org.opencv.highgui.Highgui.CV_CAP_PROP_FRAME_HEIGHT;
import static org.opencv.highgui.Highgui.CV_CAP_PROP_FRAME_WIDTH;

public class OpenCV {

	public static void loadOpenCv() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static void main(String[] args) throws IOException {
		loadOpenCv();
		IpscClassicalTarget target = new IpscClassicalTarget();

		VideoCapture c = new VideoCapture(0);
		c.set(CV_CAP_PROP_FRAME_WIDTH, 640);
		c.set(CV_CAP_PROP_FRAME_HEIGHT, 480);
		String fileName = "./examples/laser7.mov";
		//VideoCapture c = new VideoCapture(fileName);
		MovieInfo info = new MovieInfo(new File(fileName + ".info"));

		List<Point> perspectivePoints = info.getPerspectivePoints();
		choosePerspective(target, c, pts -> {
			runDrill(target, c, pts);
		});

		//runDrill(target, c, perspectivePoints);

	}

	private static void choosePerspective(IpscClassicalTarget target, VideoCapture c,
																				Consumer<List<Point>> perspectiveConsumer) {
		TargetFrameComponent targetFrameComponent = new TargetFrameComponent(target);
		OpenCvVideoComponent originalFrameComponent = new OpenCvVideoComponent();
		List<Point> perspectivePoints = newArrayList(
			new Point(0, 0),
			new Point(10, 0),
			new Point(10, 10),
			new Point(0, 10)
		);
		ProcessingChain chain = createProcessingChain(perspectivePoints, target, p -> {
		});
		chain.setTargetFrameListener(targetFrameComponent);
		chain.setOriginalFrameListener(originalFrameComponent);

		JFrame jframe = new JFrame();

		jframe.setDefaultCloseOperation(EXIT_ON_CLOSE);
		PerspectiveComponent perspectiveComponent = new PerspectiveComponent(originalFrameComponent, chain::updatePerspective);
		jframe.add(perspectiveComponent);
		jframe.add(targetFrameComponent);
		jframe.add(new JButton(new AbstractAction("Start drill") {
			@Override
			public void actionPerformed(ActionEvent e) {
				List<Point> perspectivePoints = perspectiveComponent.getPoints();
				jframe.dispose();
				chain.close();
				perspectiveConsumer.accept(perspectivePoints);
			}
		}));

		jframe.setLayout(new FlowLayout());
		jframe.pack();
		jframe.setVisible(true);

		chain.run(c);
	}

	private static void runDrill(IpscClassicalTarget target, VideoCapture c, List<Point> perspectivePoints) {
		FirstShotDrill drill = new FirstShotDrill();

		ShotDetector shotDetector = new ShotDetector(target, drill::onShot);
		TargetFrameComponent targetFrameComponent = new TargetFrameComponent(target);
		ProcessingChain chain = createProcessingChain(perspectivePoints, target, shotDetector);
		chain.setTargetFrameListener(targetFrameComponent);

		JFrame jframe = new JFrame();

		jframe.setDefaultCloseOperation(EXIT_ON_CLOSE);
		jframe.add(targetFrameComponent);

		JButton startButton = new JButton(new StartDrillAction(drill));
		jframe.add(startButton);

		jframe.setLayout(new FlowLayout());
		jframe.pack();
		jframe.setVisible(true);

		chain.runAsync(c);
	}
}