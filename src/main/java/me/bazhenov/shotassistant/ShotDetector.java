package me.bazhenov.shotassistant;

import me.bazhenov.shotassistant.target.IpscScore;
import me.bazhenov.shotassistant.target.Target;

import java.awt.*;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static me.bazhenov.shotassistant.ShotDetectingAutomata.State.Shot;

public class ShotDetector implements Consumer<Optional<Point>> {

	private final Target target;
	private final Consumer<IpscScore> shotListener;
	private final ShotDetectingAutomata automata = new ShotDetectingAutomata(1, 1);

	public ShotDetector(Target target, Consumer<IpscScore> shotListener) {
		this.target = requireNonNull(target);
		this.shotListener = requireNonNull(shotListener);
	}

	@Override
	public void accept(Optional<Point> shot) {
		if (automata.feed(shot.isPresent()) == Shot) {
			shot.flatMap(target::scoreShot)
				.ifPresent(shotListener);
		}
	}
}
