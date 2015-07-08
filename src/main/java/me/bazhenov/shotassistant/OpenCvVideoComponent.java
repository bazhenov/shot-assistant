package me.bazhenov.shotassistant;

import org.opencv.core.Mat;

import java.awt.*;
import java.awt.image.BufferedImage;

import static me.bazhenov.shotassistant.VisualizingListener.toBufferedImage;

public class OpenCvVideoComponent extends Component {

	private volatile BufferedImage image;

	public void update(Mat i) {
		image = toBufferedImage(i);
		if ((getWidth() != i.width() || getHeight() != i.height())) {
			Dimension dimension = new Dimension(i.width(), i.height());
			setSize(dimension);
			setPreferredSize(dimension);
			setMinimumSize(dimension);
		}
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (image != null) {
			g.drawImage(image, 0, 0, null, null);
		}
	}
}
