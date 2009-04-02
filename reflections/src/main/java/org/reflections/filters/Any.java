package org.reflections.filters;

/**
 *
 */
public class Any<T> implements Filter<T> {

	@SuppressWarnings({"RawUseOfParameterizedType"})
	public static final Any ANY = new Any();

	public boolean accept(final T name) {
		return true;
	}
}
