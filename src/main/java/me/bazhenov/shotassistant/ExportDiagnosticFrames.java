package me.bazhenov.shotassistant;

import me.bazhenov.shotassistant.target.IpscClassicalTarget;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.awt.Color.*;
import static java.awt.geom.AffineTransform.getTranslateInstance;
import static java.awt.image.BufferedImage.TYPE_3BYTE_BGR;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptySet;
import static me.bazhenov.shotassistant.ShotDetectingAutomata.State.*;
import static me.bazhenov.shotassistant.VisualizingListener.toBufferedImage;

/**
 * ffmpeg -r 15 -i export-result/export-%05d.png -c:v libx264 -r 30 -pix_fmt yuv420p out.mp4
 */
public class ExportDiagnosticFrames implements ProcessingListener {

	private final File location;
	private final boolean shotOnly;
	int frameNo = 0;
	private Set<Integer> requiredFrames = emptySet();

	private BufferedImage currentImage;
	private int currentY = 0;
	private int currentX = 0;
	private int currentLineMaxHeight = 0;

	public ExportDiagnosticFrames(File location, boolean shotOnly) {
		this.location = location;
		this.shotOnly = shotOnly;
		if (!location.isDirectory())
			checkState(location.mkdirs(), "Unable to create directories");
	}

	public static void main(String[] args) throws IOException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		String fileName = "./examples/laser5.mov";
		List<String> lines = Files.readAllLines(new File(fileName + ".info").toPath(), UTF_8);
		VideoCapture c = new VideoCapture(fileName);

		ProcessingChain chain = new ProcessingChain();
		List<Point> perspectivePoints = parsePerspectivePoints(lines.get(1));

		chain.addStage("perspective", new RestorePerspective(new IpscClassicalTarget().getSize(), perspectivePoints));
		chain.addStage("gray", ImageOperations.grayscale());
		chain.addStage("blur", ImageOperations.medianBlur(1));
		chain.addStage("backgroundDiff", ImageOperations.differenceWithBackground());
		chain.addStage("threshold", ImageOperations.threshold(30));
		chain.addStage("erode", ImageOperations.erode(5));

		chain.setListener(new ExportDiagnosticFrames(new File("./export-result"), false));
		VideoProcessor.run(c, chain);
	}

	private static List<Point> parsePerspectivePoints(String s) {
		List<Point> result = newArrayList();
		for (String p : s.split(";")) {
			String[] parts = p.split(",", 2);
			result.add(new Point(parseInt(parts[0]), parseInt(parts[1])));
		}
		checkState(result.size() == 4, "4 points expected. %d given", result.size());
		return result;
	}

	public static Set<Integer> parseIntervals(String s) {
		Set<Integer> result = newHashSet();
		for (String interval : s.split(",")) {
			if (interval.contains("-")) {
				String[] parts = interval.split("-", 2);
				int from = parseInt(parts[0]);
				int to = parseInt(parts[1]);
				while (from <= to)
					result.add(from++);
			} else {
				result.add(parseInt(interval));
			}
		}
		return result;
	}

	@Override
	public void onFrame(Mat mat, int stagesCount) {
		if (!requiredFrames.isEmpty() && !requiredFrames.contains(frameNo))
			return;
		currentImage = new BufferedImage(1280, 2000, TYPE_3BYTE_BGR);
		currentY = 0;
		currentX = 0;
		currentLineMaxHeight = 0;
		drawStage(mat, "Original frame");

		frameNo++;
	}

	private void drawStage(Mat mat, String name) {
		if (currentX + mat.cols() > currentImage.getWidth() && currentLineMaxHeight > 0) {
			currentY += currentLineMaxHeight;
			currentX = 0;
			currentLineMaxHeight = 0;
		}
		Graphics2D g = (Graphics2D) currentImage.getGraphics();
		g.translate(currentX, currentY);
		g.drawImage(toBufferedImage(mat), 0, 0, null, null);

		g.setFont(g.getFont().deriveFont(16f));
		g.setColor(red);
		g.drawString(name, 10, 20);
		g.drawRect(0, 0, mat.cols() - 1, mat.rows() - 1);

		currentX += mat.cols();
		currentLineMaxHeight = Math.max(currentLineMaxHeight, mat.rows());
	}

	@Override
	public void onStage(int i, String name, Mat processingResult) {
		if (!requiredFrames.isEmpty() && !requiredFrames.contains(frameNo))
			return;

		drawStage(processingResult, name);
	}

	@Override
	public void onFrameComplete() {
		if (!requiredFrames.isEmpty() && !requiredFrames.contains(frameNo))
			return;
		try {
			int width = currentImage.getWidth();
			int height = currentY + currentLineMaxHeight;
			BufferedImage resultingImage = new BufferedImage(width, height, TYPE_3BYTE_BGR);
			resultingImage.getGraphics().drawImage(currentImage, 0, 0, null, null);
			String fileName = format("export-%05d.jpg", frameNo);
			ImageIO.write(resultingImage, "jpg", new File(location, fileName));
			clear(currentImage);

		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private void clear(BufferedImage i) {
		Graphics2D g = (Graphics2D) i.getGraphics();
		g.setColor(BLACK);
		g.setTransform(getTranslateInstance(0, 0));
		g.fillRect(0, 0, i.getWidth(), i.getHeight());
	}

	private void annotate(Graphics2D g, FrameFeatures features, ShotDetectingAutomata.State state) {
		g.setColor(WHITE);
		g.drawString("Frame#: " + features.getFrameNo(), 10, 15);
		g.drawString("Covering area: " + features.getCoveringArea(), 10, 30);
		g.drawString("NonZeroPixels#: " + features.getNonZeroPixels(), 10, 45);
		g.drawString("Automata state: " + state, 10, 60);

		if (state == Candidate || state == CoolDown)
			g.setColor(ORANGE);
		else if (state == Shot)
			g.setColor(RED);
		else
			g.setColor(GREEN);
		g.fillRect(10, 65, 10, 10);

		Optional<org.opencv.core.Point> point = features.getCentroid();
		if (point.isPresent()) {
			g.setColor(RED);
			g.drawArc((int) point.get().x - 6, (int) point.get().y - 6, 12, 12, 0, 360);
		}
	}
}
