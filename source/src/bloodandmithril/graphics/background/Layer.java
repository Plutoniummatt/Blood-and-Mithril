package bloodandmithril.graphics.background;

import java.io.Serializable;
import java.util.TreeMap;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.util.datastructure.WrapperForTwo;

/**
 * A background layer
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public abstract class Layer implements Serializable {
	private static final long serialVersionUID = 5749079115895910672L;

	/**
	 * Maps the position of the x-coordinate of the image, to the image ID and the Y-offset of the image
	 */
	final TreeMap<Integer, WrapperForTwo<Integer, Integer>> images;
	
	/**
	 * Weather this layer is pegged to the horizon (sea horizon level)
	 */
	final boolean peggedToHorizon;

	/**
	 * Constructor
	 */
	protected Layer(final TreeMap<Integer, WrapperForTwo<Integer, Integer>> images, boolean peggedToHorizon) {
		this.images = images;
		this.peggedToHorizon = peggedToHorizon;
	}


	/**
	 * Applies pre-render logic like shaders
	 */
	public abstract void preRender(Graphics graphics);


	/**
	 * @return the perceived distance of this layer from foreground, 1f is infinity
	 */
	public abstract float getDistanceX();


	/**
	 * @return the perceived distance of this layer from foreground, 1f is infinity
	 */
	public abstract float getDistanceY();


	/**
	 * @return the vertical render offset
	 */
	public abstract float getOffsetY();
}