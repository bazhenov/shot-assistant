package me.bazhenov.shotassistant;

import org.opencv.core.Mat;

import java.awt.*;
import java.awt.image.BufferedImage;

import static me.bazhenov.shotassistant.VisualizingListener.toBufferedImage;

public class OpenCvVideoComponent extends Component {

	private volatile BufferedImage image;
	private volatile Dimension dimension;

	public void update(Mat i) {
		image = toBufferedImage(i);
		if (dimension == null || (dimension.getWidth() != i.width() || dimension.getHeight() != i.height()))
			dimension = new Dimension(i.width(), i.height());
	}

	@Override
	public Dimension getSize() {
		return dimension;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (image != null) {
			g.drawImage(image, 0, 0, null, null);
		}
	}
}
