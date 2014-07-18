package bloodandmithril.util;

import java.io.Serializable;

import bloodandmithril.core.Copyright;

/**
 * A {@link Serializable} version of {@link Function}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public interface SerializableFunction<T extends Serializable> extends Serializable {

	public T call();
}