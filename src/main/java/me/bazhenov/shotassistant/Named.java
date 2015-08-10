package me.bazhenov.shotassistant;

import static java.util.Objects.requireNonNull;

public final class Named<T> {

	private final String name;
	private final T ref;

	public Named(String name, T ref) {
		this.name = requireNonNull(name);
		this.ref = requireNonNull(ref);
	}

	public String getName() {
		return name;
	}

	public T get() {
		return ref;
	}
}
