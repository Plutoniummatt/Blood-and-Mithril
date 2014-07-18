package bloodandmithril.util.datastructure;

import bloodandmithril.core.Copyright;

/**
 * I Wanna be a Rapper!
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Wrapper<T> {

	/** Wrappee? */
	public T t;

	/**
	 * Constructor
	 */
	public Wrapper(T t) {
		this.t = t;
	}
}
