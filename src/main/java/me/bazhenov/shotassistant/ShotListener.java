package me.bazhenov.shotassistant;

import java.util.Optional;

public interface ShotListener<T> {

	void onShot(Optional<T> level);
}
