package me.bazhenov.shotassistant;

public class PrintShotListener implements ShotListener {

	@Override
	public void onShot(int level) {
		System.out.print("Shot registered: " + level + " ");
	}
}
