package me.bazhenov.shotassistant.drills;

import me.bazhenov.shotassistant.target.IpscScore;

import java.io.IOException;

public class FirstShotDrill implements Drill {

	private volatile long startTime;
	private volatile long finishTime;

	@Override
	public void start() {
		startTime = System.currentTimeMillis();
	}

	@Override
	public Status onShot(IpscScore shot) {
		if (startTime <= 0)
			return Status.Continue;
		/*if (finishTime > 0)
			return Status.Continue;*/
		finishTime = System.currentTimeMillis();
		double time = (finishTime - startTime) / 1000.0;
		String tm = String.format("%s in %.1f", shot, time);
		try {
			Runtime.getRuntime().exec("say -r 220 '" + tm + "'");
		} catch (IOException e) {
			e.printStackTrace(System.err);
			throw new RuntimeException(e);
		}
		startTime = 0;
		return Status.Finished;
	}

	@Override
	public void reset() {
		startTime = 0;
	}
}
