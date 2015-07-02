package me.bazhenov.shotassistant;

import javax.swing.*;
import java.awt.*;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class SwingTest {

	public static void main(String[] args) {
		JFrame jframe = new JFrame();

		jframe.setDefaultCloseOperation(EXIT_ON_CLOSE);
		jframe.add(new MyPanel());
		//jframe.setSize(width, height);
		jframe.pack();
		jframe.setVisible(true);
	}
}

class MyPanel extends JPanel {

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(300, 400);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		//g.translate(10, 5);
		g.drawString("Hello", 0, 0);
	}
}
