package bloodandmithril.util.datastructure;

import java.io.Serializable;

import bloodandmithril.core.Copyright;

/**
 * Stores two integers.
 *
 * @author Sam
 */
@Copyright("Matthew Peck 2014")
public class TwoInts implements Serializable {
	private static final long serialVersionUID = 5733873972689677971L;

	/** Two integers. */
	public int a, b;

	/**
	 * Constructor
	 */
	public TwoInts(int a, int b) {
		this.a = a;
		this.b = b;
	}


	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TwoInts) {
			if(((TwoInts)obj).a == this.a && ((TwoInts)obj).b == this.b) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}
