package bloodandmithril.generation.component.components.stemming.interfaces;

import bloodandmithril.core.Copyright;

/**
 * A unit thickness vertical {@link Interface}
 * 
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class VerticalInterface implements Interface {

	public final int tileX, tileY, height;
	
	/**
	 * Constructor
	 */
	public VerticalInterface(int tileX, int tileY, int height) {
		this.tileX = tileX;
		this.tileY = tileY;
		this.height = height;
	}
}