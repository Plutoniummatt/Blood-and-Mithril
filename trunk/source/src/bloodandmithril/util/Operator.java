package bloodandmithril.util;

/**
 * A function that operates on an object
 *
 * @author Matt
 */
public interface Operator<T> {

	public void operate(T t);
}