package bloodandmithril.util.datastructure;

import java.io.Serializable;

/**
 * Stores 4 ints to be used as boundaries for a rectangle.
 *
 * @author Sam, Matt
 */
public class Boundaries implements Serializable {
	private static final long serialVersionUID = -4985478591963928423L;

	public int top;
	public int bottom;
	public int left;
	public int right;


	/**
	 * Constructor
	 */
	public Boundaries(int top, int bottom, int left, int right) {
		this.top = top;
		this.bottom = bottom;
		this.left = left;
		this.right = right;
	}


	/**
	 * Overloaded constructor
	 */
	public Boundaries(Boundaries boundaries) {
		this.top = boundaries.top;
		this.bottom = boundaries.bottom;
		this.left = boundaries.left;
		this.right = boundaries.right;
	}


	/**
	 * True if specified coordinates are within these boundaries
	 */
	public boolean isWithin(int x, int y) {
		return x >= left && x <= right && y >= bottom && y <= top;
	}


	/**
	 * Calculates the vertical overlap between two instances of {@link Boundaries}
	 */
	public int getVerticalOverlap(Boundaries other) {

		// Test for exact match
		if (top == other.top && bottom == other.bottom) {
			return top - bottom + 1;
		}

		// Top matches
		if (top == other.top) {
			if (bottom > other.bottom) {
				return top - bottom + 1;
			} else {
				return other.top - other.bottom + 1;
			}
		}

		// Bottom matches
		if (bottom == other.bottom) {
			if (top < other.top) {
				return top - bottom + 1;
			} else {
				return other.top - other.bottom + 1;
			}
		}

		Boundaries higher = null;
		Boundaries lower = null;

		if (top > other.top) {
			higher = this;
		} else {
			higher = other;
		}

		if (bottom < other.bottom) {
			lower = this;
		} else {
			lower = other;
		}

		if (higher == lower) {
			if (higher == this) {
				return other.top - other.bottom + 1;
			} else {
				return top - bottom + 1;
			}
		}

		if (higher.bottom > lower.top) {
			return 0;
		} else {
			return lower.top - higher.bottom + 1;
		}
	}


	/**
	 * True if this {@link Boundaries} is within another set of {@link Boundaries}
	 */
	public boolean isWithin(Boundaries other) {
		return left >= other.left && right <= other.right && bottom >= other.bottom && top <= other.top;
	}


	/**
	 * True if this {@link Boundaries} overlaps with another
	 */
	public boolean doesOverlapWith(Boundaries other) {
		return
			!(left >= other.right) &&
			!(right <= other.left) &&
			!(top <= other.bottom) &&
			!(bottom >= other.top);
	}
}