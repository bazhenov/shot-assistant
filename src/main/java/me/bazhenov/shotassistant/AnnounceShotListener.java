package me.bazhenov.shotassistant;

import me.bazhenov.shotassistant.target.IpscScore;

import java.io.IOException;
import java.util.Optional;

public class AnnounceShotListener implements ShotListener<IpscScore> {

	private int shot = 0;

	@Override
	public void onShot(Optional<IpscScore> level) {
		try {
			shot++;
			Runtime.getRuntime().exec("say -r 220 '" + level.map(IpscScore::toString).orElse("miss") + "'");
		} catch (IOException e) {
			e.printStackTrace(System.err);
			throw new RuntimeException(e);
		}
	}
}
