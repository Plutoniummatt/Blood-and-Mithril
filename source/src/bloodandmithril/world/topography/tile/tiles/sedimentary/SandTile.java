package bloodandmithril.world.topography.tile.tiles.sedimentary;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.mineral.earth.SandItem;
import bloodandmithril.world.topography.tile.tiles.SeditmentaryTile;

@Copyright("Matthew Peck 2014")
public class SandTile extends SeditmentaryTile {
	private static final long serialVersionUID = 905567490661951934L;
	private static Color mineExplosionColor = new Color(158f/255f, 136f/255f, 7f/255f, 1f);

	/**
	 * Constructor
	 */
	public SandTile() {
		super(false);
	}


	@Override
	protected float getTexCoordYSpecific() {
		return 5;
	}


	@Override
	public void changeToSmoothCeiling() {
	}


	@Override
	public Item mine() {
		return new SandItem();
	}


	@Override
	public boolean isTransparent() {
		return false;
	}


	@Override
	public Color getMineExplosionColor() {
		return mineExplosionColor;
	}
}