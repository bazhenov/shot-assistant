package me.bazhenov.shotassistant;

import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkState;
import static me.bazhenov.shotassistant.VisualizingListener.toBufferedImage;

public class WriteToFsConsumer implements Consumer<Mat> {

	private final File location;
	private int frameNo = 0;

	public WriteToFsConsumer(File location) {
		this.location = location;
		if (!location.isDirectory())
			checkState(location.mkdirs());
	}

	@Override
	public void accept(Mat mat) {
		frameNo++;
		BufferedImage image = toBufferedImage(mat);
		try {
			ImageIO.write(image, "png", new File(location, String.format("frame-%05d.png", frameNo)));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
