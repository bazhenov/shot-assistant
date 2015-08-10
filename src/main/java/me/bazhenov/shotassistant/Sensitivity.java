package me.bazhenov.shotassistant;

public class Sensitivity {

	public static final Sensitivity LOW = new Sensitivity(15, 5, 5, 1, 0);
	public static final Sensitivity MEDIUM = new Sensitivity(10, 3, 3, 1, 1);
	public static final Sensitivity HIGH = new Sensitivity(10, 1, 1, 3, 3);

	private final int threshold;
	private final int blurLevel;
	private final int dilateLevel;
	private final int candidateDelay;
	private final int coolDownDelay;

	public Sensitivity(int threshold, int blurLevel, int dilateLevel, int candidateDelay, int coolDownDelay) {
		this.threshold = threshold;
		this.blurLevel = blurLevel;
		this.dilateLevel = dilateLevel;
		this.candidateDelay = candidateDelay;
		this.coolDownDelay = coolDownDelay;
	}

	public int getThresholdValue() {
		return threshold;
	}

	public int getBlurLevel() {
		return blurLevel;
	}

	public int getDilateErodeLevel() {
		return dilateLevel;
	}

	public int getCandidateDelay() {
		return candidateDelay;
	}

	public int getCoolDownDelay() {
		return coolDownDelay;
	}
}
