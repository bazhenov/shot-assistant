package me.bazhenov.shotassistant.drills;

import me.bazhenov.shotassistant.target.IpscScore;

public interface Drill {

	void start();

	Status onShot(IpscScore shot);

	void reset();

	enum Status {
		Finished, Continue
	}
}
