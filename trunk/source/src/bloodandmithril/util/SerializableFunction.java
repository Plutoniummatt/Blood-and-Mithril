package bloodandmithril.util;

import java.io.Serializable;

/**
 * A {@link Serializable} version of {@link Function}
 *
 * @author Matt
 */
public interface SerializableFunction<T extends Serializable> extends Serializable {

	public T call();
}