package me.bazhenov.shotassistant;

import java.awt.*;

public class TargetFrameComponent extends OpenCvVideoComponent {

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		int centerX = (int) (getSize().getWidth() / 2);
		int centerY = (int) (getSize().getHeight() / 2);

		g.setColor(new Color(255, 255, 255, 189));
		for (int i = 0; i < 10; i++) {
			int r = (int) (10 + (i * getSize().getWidth() / 10)) / 2;
			g.drawOval(centerX - r, centerY - r, 2 * r, 2 * r);
		}

	}
}
