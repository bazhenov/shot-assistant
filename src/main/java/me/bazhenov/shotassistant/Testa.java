package me.bazhenov.shotassistant;

import javax.swing.*;
import java.awt.*;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class Testa {

	public static void main(String[] args) {
		JFrame jframe = new JFrame();

		jframe.setDefaultCloseOperation(EXIT_ON_CLOSE);

		Dimension dimension = new Dimension(400, 400);
		Component c = new JPanel();
		c.setSize(dimension);
		c.setBackground(Color.red);
		//jframe.add(currentFrame);
		PerspectiveComponent comp = new PerspectiveComponent(c, p -> System.out.println(p.toString()));
		comp.setSize(dimension);
		jframe.add(comp);
		jframe.setSize(dimension);
		jframe.setVisible(true);
	}
}
