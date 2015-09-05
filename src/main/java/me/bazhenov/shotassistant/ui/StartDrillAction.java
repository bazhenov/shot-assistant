package me.bazhenov.shotassistant.ui;

import me.bazhenov.shotassistant.drills.Drill;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.sound.sampled.AudioSystem.getAudioInputStream;

public class StartDrillAction extends AbstractAction {

	private final Drill drill;

	public StartDrillAction(Drill drill) {
		super("Start drill");
		putValue(MNEMONIC_KEY, KeyEvent.VK_S);
		this.drill = drill;
	}

	public static synchronized void playSound(final String url) {
		try (InputStream stream = StartDrillAction.class.getResourceAsStream(url)) {
			Clip clip = AudioSystem.getClip();
			clip.open(getAudioInputStream(stream));
			clip.start();
		} catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
			System.err.println(e.getMessage());
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		waitAndPlayStartBeeperSound(drill);
	}

	public static void waitAndPlayStartBeeperSound(Drill drill) {
		new Thread(() -> {
			try {
				int seconds = (int) (Math.random() * 3) + 1;
				SECONDS.sleep(seconds);
			} catch (InterruptedException e1) {
				throw new RuntimeException(e1);
			}
			playSound("/beep.wav");
			drill.start();
		}).start();
	}
}
