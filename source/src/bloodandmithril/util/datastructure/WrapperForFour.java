package bloodandmithril.util.datastructure;

import java.io.Serializable;

import bloodandmithril.core.Copyright;

@Copyright("Matthew Peck 2016")
public class WrapperForFour<A, B, C, D> implements Serializable {
	private static final long serialVersionUID = 9171014626971496388L;
	
	public A a;
	public B b;
	public C c;
	public D d;

	public static <A, B, C, D> WrapperForFour<A, B, C, D> wrap(A a, B b, C c, D d) {
		return new WrapperForFour<>(a, b, c, d);
	}


	/**
	 * Constructor
	 */
	private WrapperForFour(A a, B b, C c, D d) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}
}