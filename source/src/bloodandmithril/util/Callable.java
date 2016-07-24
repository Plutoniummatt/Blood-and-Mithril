package bloodandmithril.util;

import bloodandmithril.core.Copyright;

/**
 * Basically the same thing as a {@link java.util.concurrent.Callable} but doesn't throw {@link Exception}s
 * 
 * @author Matt
 *
 * @param <T>
 */
@Copyright("Matthew Peck 2016")
public interface Callable<T> {

	public T call();
}