package bloodandmithril.world.topography.tile.tiles.brick;

import bloodandmithril.world.topography.tile.tiles.BrickTile;

public class YellowBrickTile extends BrickTile {
	private static final long serialVersionUID = 8759613388552944453L;

	/**
	 * Constructor
	 */
	public YellowBrickTile() {
		super(false);
	}


	@Override
	protected float getTexCoordYSpecific() {
		return 8;
	}


	@Override
	public void changeToStair() {
		this.isStair = true;
	}
}