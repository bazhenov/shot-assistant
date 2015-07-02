package me.bazhenov.shotassistant;

import java.io.IOException;

public class AnnounceShotListener implements ShotListener {

	private int shot = 0;

	@Override
	public void onShot(int level) {
		try {
			shot++;
			System.out.println("Shot #" + shot);
			Runtime.getRuntime().exec("say -r 280 'hit'");
		} catch (IOException e) {
			e.printStackTrace(System.err);
			throw new RuntimeException(e);
		}
	}
}
