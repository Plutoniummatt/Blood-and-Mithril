package bloodandmithril.world.topography.tile.tiles.soil;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.food.plant.SeedItem;
import bloodandmithril.item.items.mineral.earth.DirtItem;
import bloodandmithril.world.topography.tile.tiles.SoilTile;

@Copyright("Matthew Peck 2014")
public class StandardSoilTile extends SoilTile {
	private static final long serialVersionUID = 8075638248883710200L;
	private static Color mineExplosionColor = new Color(61f/255f, 31/255f, 31/255f, 1f);

	/**
	 * Constructor
	 */
	public StandardSoilTile() {
		super(false);
	}


	@Override
	protected float getTexCoordYSpecific() {
		return 3;
	}


	@Override
	public void changeToSmoothCeiling() {
	}


	@Override
	public Item mine() {
		return new DirtItem();
	}


	@Override
	public boolean canPlant(SeedItem seed) {
		return true;
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