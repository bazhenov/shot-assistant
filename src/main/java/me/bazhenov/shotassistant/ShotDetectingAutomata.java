package me.bazhenov.shotassistant;

import static me.bazhenov.shotassistant.ShotDetectingAutomata.State.*;

public final class ShotDetectingAutomata {

	private State state = NoShot;
	private int shotMarkerHistory = 0;
	private int candidateToShotFrames = 2;
	private int coolDownFrames = 5;

	public ShotDetectingAutomata(int candidateToShotFrames, int coolDownFrames) {
		this.candidateToShotFrames = candidateToShotFrames;
		this.coolDownFrames = coolDownFrames;
	}

	public State feed(boolean shotMarkerFound) {
		updateShotMarkerHistory(shotMarkerFound);
		switch (state) {
			case NoShot:
				if (shotMarkerFound) {
					state = candidateToShotFrames > 1 ? Candidate : Shot;
				}
				break;

			case Candidate:
				if (!shotMarkerFound) {
					state = NoShot;
				} else {
					if (shotMarkerHistory > candidateToShotFrames) {
						state = Shot;
					}
				}

				break;

			case Shot:
				state = (coolDownFrames > 0 || shotMarkerFound) ? CoolDown : NoShot;
				break;

			case CoolDown:
				if (shotMarkerHistory < -coolDownFrames) {
					state = NoShot;
				}
				break;

			default:
				throw new UnsupportedOperationException();
		}
		return state;
	}

	private void updateShotMarkerHistory(boolean shotMarkerFound) {
		if (shotMarkerFound)
			shotMarkerHistory = Math.max(1, shotMarkerHistory + 1);
		else
			shotMarkerHistory = Math.min(-1, shotMarkerHistory - 1);
	}

	public static enum State {
		NoShot, Candidate, Shot, CoolDown
	}
}
