package bloodandmithril.util;

import java.io.Serializable;

import bloodandmithril.core.Copyright;

/**
 * A function that operates on an object
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public interface Operator<T> extends Serializable {

	public void operate(T t);
}