package bloodandmithril.prop.construction.craftingstation;

import java.util.Map;

import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.food.animal.ChickenLeg;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

public class Campfire extends CraftingStation {
	private static final long serialVersionUID = -8876217926271589078L;
	private boolean lit;

	private static final Map<Item, Integer> craftables = Maps.newHashMap();
	static {
		craftables.put(new ChickenLeg(true), 1);
	}

	/**
	 * Constructor
	 */
	public Campfire(float x, float y) {
		super(x, y, 64, 32, 0f);
		setConstructionProgress(1f);
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return Furnace.FURNACE;
	}


	@Override
	public String getAction() {
		return "Cook";
	}


	@Override
	public Map<Item, Integer> getCraftables() {
		return craftables;
	}


	@Override
	public String getDescription() {
		return "A campfire, can be used for some basic cooking, as well as provide some warmth and light.";
	}


	@Override
	public String getTitle() {
		return "Campfire";
	}


	@Override
	public void preRender() {
	}


	@Override
	public boolean customCanCraft() {
		return lit;
	}
}