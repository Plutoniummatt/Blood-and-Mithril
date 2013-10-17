package spritestar.util.datastructure;

import java.io.Serializable;

import com.badlogic.gdx.math.Vector2;

/**
 * Stores a line in the form of 2 coordinates.
 *
 * @author Sam
 */
public class Line implements Serializable {
	private static final long serialVersionUID = -2775197848532953106L;

	/**	Coordinates */
	public float x1, y1, x2, y2;

	/**
	 * @param x1 - x of the first coordinate.
	 * @param y1 - y of the first coordinate.
	 * @param x2 - x of the second coordinate.
	 * @param y2 - x of the second coordinate.
	 */
	public Line(float x1, float y1, float x2, float y2) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}


	/**
	 * Get the point of intersection between this line and a given line.
	 * This will always find an intersection if the lines aren't parallel.
	 *
	 * @param line - the given line.
	 * @return - the point of intersection.
	 */
	public Vector2 getIntersection(Line line) {
		float m1 = (this.y2 - this.y1) / (this.x2 - this.x1);
		float m2 = (line.y2 - line.y1) / (line.x2 - line.x1);
		float x = (line.y1 - this.y1 - m2 * line.x1 + m1 * this.x1) / (m1 - m2);
		float y = m1 * x + this.y1 - this.x1 * ((this.y2 - this.y1) / (this.x2 - this.x1));
		return new Vector2(x, y);
	}


	/**
	 * Checks if a point is within the minimum rectangular boundaries which could contain this line.
	 */
	public boolean isWithinBoxContainingLine(Vector2 vector) {
		if (vector.x < Math.min(x1, x2) || vector.x > Math.max(x1, x2) || vector.y < Math.min(y1, y2) || vector.y > Math.max(y1, y2)) {
			return false;
		} else {
			return true;
		}
	}
	

	/**
	 * Put the two coordinates left first, or if they're on top of each other, bottom first.
	 */
	public void organiseLine() {
		float tempX1;
		float tempY1;
		float tempX2;
		float tempY2;
		if (x1 == x2) {
			if (y1 < y2) {
				tempY1 = y1;
				tempY2 = y2;
				tempX1 = x1;
				tempX2 = y2;
			} else {
				tempY1 = y2;
				tempY2 = y1;
				tempX1 = x2;
				tempX2 = x1;
			}
		} else {
			if (x1 < x2) {
				tempX1 = x1;
				tempX2 = x2;
				tempY1 = y1;
				tempY2 = y2;
			} else {
				tempX1 = x2;
				tempX2 = x1;
				tempY1 = y2;
				tempY2 = y1;
			}
		}
		x1 = tempX1;
		x2 = tempX2;
		y1 = tempY1;
		y2 = tempY2;
	}


	/**
	 * @return the gradient of this line.
	 */
	public float getGradient() {
		organiseLine();
		if (x1 == x2) {
			return Float.POSITIVE_INFINITY;
		}
		return (y2 - y1)/(x2 - x1);
	}
}
