package me.bazhenov.shotassistant;

import me.bazhenov.shotassistant.target.IpscScore;

import java.util.Optional;

public class PrintShotListener implements ShotListener<IpscScore> {

	@Override
	public void onShot(Optional<IpscScore> level) {
		System.out.println("Shot registered: " + level);
	}
}
