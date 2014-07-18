package bloodandmithril.util;

import bloodandmithril.core.Copyright;

/**
 * A function that returns a generic type
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public interface Function<T> {

	/** Execute some code and return a generic type */
	public T call();
}