package bloodandmithril.generation.component.components.stemming.interfaces;

import bloodandmithril.core.Copyright;

/**
 * A unit thickness horizontal {@link Interface}
 * 
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class HorizontalInterface implements Interface {
	private static final long serialVersionUID = 6387432191883112054L;
	
	public final int tileX, tileY, width;
	private final StemmingDirection stemmingDirection;
	
	/**
	 * Constructor
	 */
	public HorizontalInterface(int tileX, int tileY, int width, StemmingDirection stemmingDirection) {
		this.tileX = tileX;
		this.tileY = tileY;
		this.width = width;
		this.stemmingDirection = stemmingDirection;
	}

	
	@Override
	public StemmingDirection getStemmingDirection() {
		return stemmingDirection;
	}
}