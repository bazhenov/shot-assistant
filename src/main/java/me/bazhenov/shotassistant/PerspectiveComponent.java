package me.bazhenov.shotassistant;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;
import java.util.function.Consumer;

import static com.google.common.collect.Lists.newArrayList;
import static java.awt.Color.white;
import static java.lang.Math.abs;
import static java.util.Arrays.asList;

public class PerspectiveComponent extends Container {

	private static final int HALF_WIDTH = 5;
	private final Component delegate;
	private List<Point> points;
	private int activePointIdx = -1;

	public PerspectiveComponent(Component delegate, Consumer<List<Point>> listener) {
		this.points = asList(new Point(10, 20), new Point(310, 25), new Point(310, 150), new Point(32, 210));
		listener.accept(points);

		this.delegate = delegate;
		add(delegate);
		addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (activePointIdx >= 0)
					return;

				for (int i = 0; i < points.size(); i++) {
					Point p = points.get(i);
					if (abs(e.getX() - p.x) < HALF_WIDTH && abs(e.getY() - p.y) < HALF_WIDTH) {
						activePointIdx = i;
						break;
					}
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (activePointIdx >= 0) {
					listener.accept(points);
					activePointIdx = -1;
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}
		});
		addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if (activePointIdx >= 0) {
					points.get(activePointIdx).setLocation(e.getX(), e.getY());
					listener.accept(points);
					repaint();
				}
			}

			@Override
			public void mouseMoved(MouseEvent e) {
			}
		});
	}

	public List<Point> getPoints() {
		return points;
	}

	@Override
	public Dimension getSize() {
		return delegate.getSize();
	}

	@Override
	public Dimension getMinimumSize() {
		return delegate.getMinimumSize();
	}

	@Override
	public Dimension getPreferredSize() {
		return delegate.getPreferredSize();
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		delegate.paint(g);
		g.setColor(white);

		points.forEach(p -> g.drawRect(p.x - HALF_WIDTH, p.y - HALF_WIDTH, HALF_WIDTH * 2, HALF_WIDTH * 2));

		List<Point> pts = newArrayList(points);
		pts.add(points.get(0));
		for (int i = 0; i < pts.size() - 1; i++) {
			List<Point> pLine = pts.subList(i, i + 2);
			g.drawLine(pLine.get(0).x, pLine.get(0).y, pLine.get(1).x, pLine.get(1).y);
		}
	}
}
