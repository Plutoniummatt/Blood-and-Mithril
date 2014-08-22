package bloodandmithril.util;

import java.io.Serializable;

import com.google.common.base.Function;

/**
 * {@link Serializable} version of {@link Function}
 *
 * @author Matt
 */
public abstract class SerializableMappingFunction<F, T> implements Function<F, T>, Serializable {
	private static final long serialVersionUID = 3220742596173017770L;
}