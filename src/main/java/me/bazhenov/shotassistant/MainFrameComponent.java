package me.bazhenov.shotassistant;

import org.opencv.core.Mat;

import java.awt.*;

import static java.awt.Color.green;

public class MainFrameComponent extends OpenCvVideoComponent {

	private volatile FrameFeatures f;

	public void update(Mat i, FrameFeatures f) {
		super.update(i);
		this.f = f;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (f != null) {
			g.setColor(green);
			g.setFont(getFont().deriveFont(10.0f));
			g.drawString("FNo: " + f.getFrameNo(), 10, 20);
			g.drawString(" CA: " + f.getCoveringArea(), 10, 35);
			g.drawString("NZP: " + f.getNonZeroPixels(), 10, 50);
		}
	}
}
