package bloodandmithril.util;

/**
 * A function that returns a generic type
 *
 * @author Matt
 */
public interface Function<T> {

	/** Execute some code and return a generic type */
	public T call();
}