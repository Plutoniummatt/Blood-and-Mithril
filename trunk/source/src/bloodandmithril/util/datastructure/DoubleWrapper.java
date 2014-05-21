package bloodandmithril.util.datastructure;

import java.io.Serializable;

/**
 * Wraps two things
 *
 * @author Matt
 */
public class DoubleWrapper<T extends Serializable, S extends Serializable> implements Serializable {
	private static final long serialVersionUID = -3051706517345260879L;

	/** Wrappee? */
	public T t;
	public S s;

	/**
	 * Constructor
	 */
	public DoubleWrapper(T t, S s) {
		this.t = t;
		this.s = s;
	}
}
