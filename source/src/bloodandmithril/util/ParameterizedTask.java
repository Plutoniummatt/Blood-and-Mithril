package bloodandmithril.util;

/**
 * A parameterized {@link Task}
 *
 * @author Matt
 */
public interface ParameterizedTask<T> {

	/**
	 * Execute this {@link ParameterizedTask}
	 */
	public void execute(T t);
}
