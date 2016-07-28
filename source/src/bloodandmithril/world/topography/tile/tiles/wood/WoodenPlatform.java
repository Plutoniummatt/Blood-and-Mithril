package bloodandmithril.world.topography.tile.tiles.wood;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.material.PlankItem;
import bloodandmithril.item.material.wood.StandardWood;
import bloodandmithril.world.topography.tile.Tile;

@Copyright("Matthew Peck 2016")
public class WoodenPlatform extends Tile {
	private static final long serialVersionUID = -2351748149257815321L;

	public WoodenPlatform() {
		super(true);
	}

	
	@Override
	public Color getMineExplosionColor() {
		return Color.MAROON;
	}

	
	@Override
	protected float getTexCoordYSpecific() {
		return 10;
	}

	
	@Override
	public boolean isTransparent() {
		return false;
	}

	
	@Override
	public void changeToSmoothCeiling() {
	}

	
	@Override
	public Item mine() {
		return PlankItem.plank(StandardWood.class);
	}
}