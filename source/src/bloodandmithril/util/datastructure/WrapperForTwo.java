package bloodandmithril.util.datastructure;

public class WrapperForTwo<A, B> {

	/** Wrappees? */
	public A a;
	public B b;


	/**
	 * Constructor
	 */
	public WrapperForTwo(A a, B b) {
		this.a = a;
		this.b = b;
	}
}
