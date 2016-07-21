package bloodandmithril.util;

import java.io.Serializable;

import com.google.common.base.Function;

import bloodandmithril.core.Copyright;

/**
 * {@link Serializable} version of {@link Function}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public abstract class SerializableMappingFunction<F, T> implements Function<F, T>, Serializable {
	private static final long serialVersionUID = 3220742596173017770L;
}