package me.bazhenov.shotassistant.drills;

import me.bazhenov.shotassistant.target.IpscScore;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static me.bazhenov.shotassistant.ui.StartDrillAction.waitAndPlayStartBeeperSound;

public class DrillLifecycle implements Consumer<IpscScore> {

	private final Drill drill;

	private volatile int drillStatus = 0;

	public DrillLifecycle(Drill drill) {
		this.drill = requireNonNull(drill);
	}

	@Override
	public synchronized void accept(IpscScore ipscScore) {
		if (drillStatus == 0) {
			waitAndPlayStartBeeperSound(drill);
			startDrill();
			drillStatus = 2;
		} else if (drillStatus == 2) {
			if (drill.onShot(ipscScore) == Drill.Status.Finished) {
				drill.reset();
			}
		}
	}

	private void startDrill() {

	}
}
