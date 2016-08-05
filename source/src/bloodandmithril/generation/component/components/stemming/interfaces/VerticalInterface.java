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
	private final StemmingDirection direction;
	
	/**
	 * Constructor
	 */
	public VerticalInterface(int tileX, int tileY, int height, StemmingDirection direction) {
		this.tileX = tileX;
		this.tileY = tileY;
		this.height = height;
		this.direction = direction;
	}

	
	@Override
	public StemmingDirection getStemmingDirection() {
		return direction;
	}
}