package me.bazhenov.shotassistant;

import javax.swing.*;

import java.awt.*;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class TestSwing {

	public static void main(String[] args) {
		JFrame jframe = new JFrame();

		jframe.setDefaultCloseOperation(EXIT_ON_CLOSE);

		jframe.setLayout(new FlowLayout());
		jframe.add(new JButton("Hello"));
		jframe.add(new JButton("Hello 2"));
		jframe.pack();
		jframe.setVisible(true);
	}
}
