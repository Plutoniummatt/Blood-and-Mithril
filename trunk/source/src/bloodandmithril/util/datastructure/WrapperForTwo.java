package bloodandmithril.util.datastructure;

import java.io.Serializable;

import bloodandmithril.core.Copyright;

@Copyright("Matthew Peck 2014")
public class WrapperForTwo<A, B> implements Serializable {
	private static final long serialVersionUID = 6376930545850683753L;

	/** Wrappees? */
	public A a;
	public B b;


	public static <A, B> WrapperForTwo<A, B> wrap(A a, B b) {
		return new WrapperForTwo<>(a, b);
	}


	/**
	 * Constructor
	 */
	public WrapperForTwo(A a, B b) {
		this.a = a;
		this.b = b;
	}
}
