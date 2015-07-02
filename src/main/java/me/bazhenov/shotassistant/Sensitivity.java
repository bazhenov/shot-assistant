package me.bazhenov.shotassistant;

public enum Sensitivity {
	LOW(40, 7, 8, 0, 0),
	MEDIUM(20, 1, 3, 1, 1),
	HIGH(10, 1, 1, 3, 3);

	private final int threshold;
	private final int blurLevel;
	private final double dilateLevel;
	private final int candidateDelay;
	private final int coolDownDelay;

	Sensitivity(int threshold, int blurLevel, double dilateLevel, int candidateDelay, int coolDownDelay) {
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

	public double getDilateErodeLevel() {
		return dilateLevel;
	}

	public int getCandidateDelay() {
		return candidateDelay;
	}

	public int getCoolDownDelay() {
		return coolDownDelay;
	}
}
