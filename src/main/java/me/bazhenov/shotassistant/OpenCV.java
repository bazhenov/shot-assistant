package me.bazhenov.shotassistant;

import me.bazhenov.shotassistant.drills.Drill;
import me.bazhenov.shotassistant.drills.FirstShotDrill;
import me.bazhenov.shotassistant.target.IpscClassicalTarget;
import me.bazhenov.shotassistant.ui.StagesComboBoxModel;
import me.bazhenov.shotassistant.ui.StartDrillAction;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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

		/*String fileName = "./examples/laser7.mov";
		VideoCapture c = new VideoCapture(fileName);
		MovieInfo info = new MovieInfo(new File(fileName + ".info"));
		List<Point> perspectivePoints = info.getPerspectivePoints();
		runDrill(target, c, perspectivePoints, new FirstShotDrill());*/

		VideoCapture c = new VideoCapture(0);
		c.set(CV_CAP_PROP_FRAME_WIDTH, 640);
		c.set(CV_CAP_PROP_FRAME_HEIGHT, 480);
		List<Point> points = newArrayList(new Point(0, 0), new Point(640, 0), new Point(640, 480), new Point(0, 480));
		runDrill(target, c, points, new FirstShotDrill());
	}

	private static void runDrill(IpscClassicalTarget target, VideoCapture c, List<Point> perspectivePoints, Drill drill) {
		//DrillLifecycle lifecycle = new DrillLifecycle(drill);
		ShotDetector shotDetector = new ShotDetector(target, drill::onShot);
		ProcessingChain chain = createProcessingChain(perspectivePoints, target, shotDetector);
		//TargetFrameComponent targetFrameComponent = new TargetFrameComponent(target);
		//chain.setTargetFrameListener(targetFrameComponent);

		MainFrameComponent comp = new MainFrameComponent();

		JFrame jframe = new JFrame();

		jframe.setDefaultCloseOperation(EXIT_ON_CLOSE);
		jframe.add(comp);

		jframe.add(new JButton(new StartDrillAction(drill)));

		StagesComboBoxModel stagesModel = new StagesComboBoxModel();
		jframe.add(new JComboBox<>(stagesModel));

		jframe.setLayout(new FlowLayout());
		jframe.pack();
		jframe.setVisible(true);

		VideoProcessor processor = new VideoProcessor();

		processor.setListener(new ProcessingListener() {
			@Override
			public void onStage(ProcessingChain<?> c, String name, Mat processingResult) {
				stagesModel.addIfNeeded(c, name);
				if (stagesModel.isValid(c, name))
					comp.update(processingResult);
			}

			@Override
			public void onFrame(Mat mat) {

			}

			@Override
			public void onFrameComplete(Map<ProcessingChain<?>, Object> features) {

			}
		});

		processor.addChain(chain);
		processor.run(c);
	}
}