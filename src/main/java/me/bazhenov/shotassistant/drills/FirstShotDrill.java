package me.bazhenov.shotassistant.drills;

import me.bazhenov.shotassistant.target.IpscScore;

import java.util.Optional;

public class FirstShotDrill implements Drill {

	private long startTime;
	private long finishTime;

	public void start() {
		startTime = System.currentTimeMillis();
	}

	public Status onShot(Optional<IpscScore> shot) {
		if (shot.isPresent()) {
			finishTime = System.currentTimeMillis();
			return Status.Finished;
		}
		return Status.Continue;
	}

	public long getDrillStatus() {
		return finishTime - startTime;
	}

}
