package bloodandmithril.generation.component.components.stemming.interfaces;

import bloodandmithril.core.Copyright;

/**
 * A unit thickness horizontal {@link Interface}
 * 
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class HorizontalInterface implements Interface {

	public final int tileX, tileY, width;
	
	/**
	 * Constructor
	 */
	public HorizontalInterface(int tileX, int tileY, int width) {
		this.tileX = tileX;
		this.tileY = tileY;
		this.width = width;
	}
}