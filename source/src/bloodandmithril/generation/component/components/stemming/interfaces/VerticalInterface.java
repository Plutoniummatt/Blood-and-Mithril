package bloodandmithril.generation.component.components.stemming.interfaces;

import bloodandmithril.core.Copyright;

/**
 * A unit thickness vertical {@link Interface}
 * 
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class VerticalInterface implements Interface {
	private static final long serialVersionUID = 1685616961384556030L;
	
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