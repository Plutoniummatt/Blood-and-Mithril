package bloodandmithril.util;

import bloodandmithril.core.Copyright;

/**
 * A parameterized {@link Task}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public interface ParameterizedTask<T> {

	/**
	 * Execute this {@link ParameterizedTask}
	 */
	public void execute(T t);
}
