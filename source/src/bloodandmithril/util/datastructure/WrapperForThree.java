package bloodandmithril.util.datastructure;

import java.io.Serializable;

import bloodandmithril.core.Copyright;

@Copyright("Matthew Peck 2014")
public class WrapperForThree<A, B, C> implements Serializable {
	private static final long serialVersionUID = -7895334250552575621L;
	
	public A a;
	public B b;
	public C c;

	public static <A, B, C> WrapperForThree<A, B, C> wrap(A a, B b, C c) {
		return new WrapperForThree<>(a, b, c);
	}


	/**
	 * Constructor
	 */
	private WrapperForThree(A a, B b, C c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}
}
