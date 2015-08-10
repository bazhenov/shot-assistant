package me.bazhenov.shotassistant;

import me.bazhenov.shotassistant.target.Target;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.function.Consumer;

import static java.awt.geom.AffineTransform.getScaleInstance;
import static java.util.Objects.requireNonNull;

public class TargetFrameComponent extends OpenCvVideoComponent {

	private final Target target;

	public TargetFrameComponent(Target target) {
		this.target = requireNonNull(target);
		setPreferredSize(target.getSize());
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		/*int centerX = (int) (getSize().getWidth() / 2);
		int centerY = (int) (getSize().getHeight() / 2);


		for (int i = 0; i < 10; i++) {
			int r = (int) (10 + (i * getSize().getWidth() / 10)) / 2;
			g.drawOval(centerX - r, centerY - r, 2 * r, 2 * r);
		}*/
		Graphics2D g2d = (Graphics2D) g;
		g.setColor(new Color(255, 255, 255, 189));
		double scale = getSize().getHeight() / target.getSize().getHeight();
		withTransform(getScaleInstance(scale, scale), g2d, target::draw);
		//target.draw(g);
	}


	private static void withTransform(AffineTransform transform, Graphics2D g, Consumer<Graphics2D> consumer) {
		AffineTransform current = g.getTransform();
		try {
			AffineTransform t = new AffineTransform(current);
			t.concatenate(transform);
			g.setTransform(t);
			consumer.accept(g);
		} finally {
			g.setTransform(current);
		}
	}
}
