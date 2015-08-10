package me.bazhenov.shotassistant;

import me.bazhenov.shotassistant.target.IpscClassicalTarget;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Sets.intersection;
import static com.google.common.collect.Sets.newHashSet;
import static java.nio.charset.StandardCharsets.UTF_8;
import static me.bazhenov.shotassistant.ExportDiagnosticFrames.parseIntervals;

/**
 * ffmpeg -r 15 -i export-result/export-%05d.png -c:v libx264 -r 30 -pix_fmt yuv420p out.mp4
 */
public class ComputeAccuracy implements OldListener {

	private Set<Integer> framesWithCentroids = newHashSet();

	public ComputeAccuracy() {
	}

	public static void main(String[] args) throws IOException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		String fileName = "./examples/laser5.mov";


		List<String> lines = Files.readAllLines(new File(fileName + ".confirmed").toPath(), UTF_8);
		Set<Integer> confirmedFrames = parseIntervals(lines.get(0));

		Sensitivity s = new Sensitivity(15, 9, 20, 1, 0);
		VideoCapture c = new VideoCapture(fileName);
		calculateAccuracy(confirmedFrames, s, c);

		/*for (int threshold = 4; threshold <= 20; threshold += 4) {
			for (int blurLevel = 1; blurLevel <= 5; blurLevel += 2) {
				for (int dilateLevel = 0; dilateLevel <= 8; dilateLevel += 1) {
					Sensitivity s = new Sensitivity(threshold, blurLevel, dilateLevel, 1, 0);
					VideoCapture c = new VideoCapture(fileName);
					calculateAccuracy(confirmedFrames, s, c);
				}
			}
		}*/

	}

	private static void calculateAccuracy(Set<Integer> confirmedFrames, Sensitivity s, VideoCapture c) {
		FrameProcessingLoop loop = new FrameProcessingLoop(c, new IpscClassicalTarget());
		loop.setSensitivity(s);

		ComputeAccuracy listener = new ComputeAccuracy();
		loop.addFrameListener(listener);
		loop.run(l -> {
			//System.out.println("Shot detected");
		});

		double intersection = intersection(confirmedFrames, listener.framesWithCentroids).size();

		double precision = intersection / listener.framesWithCentroids.size();
		double recall = intersection / confirmedFrames.size();

		System.out.printf("%d\t%d\t%d\t%.2f\t%.2f\n", s.getThresholdValue(), s.getBlurLevel(),
			s.getDilateErodeLevel(), precision, recall);
	}

	@Override
	public void onFrame(Mat mat, Mat background, Mat frame, FrameFeatures features, Optional<?> shot, ShotDetectingAutomata.State automataState) {
		if (features.getCentroid().isPresent())
			framesWithCentroids.add(features.getFrameNo());
	}
}
