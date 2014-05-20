package bloodandmithril.item.furniture;

import static bloodandmithril.item.material.metal.Ingot.ingot;

import java.util.Map;

import bloodandmithril.character.Individual;
import bloodandmithril.item.Container;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.Item;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.material.metal.Iron;
import bloodandmithril.item.material.plant.Pine;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

/**
 * The {@link Item} representation of a {@link WoodenChest}.
 * Does not contain items like the {@link Container} version
 *
 * @author Matt
 */
public class WoodenChest extends Item implements Craftable {
	private static final long serialVersionUID = -6430848300222528418L;

	/**
	 * Constructor
	 */
	public WoodenChest() {
		super(10f, false, ItemValues.WOODENCHEST);
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return (firstCap ? "W" : "w") + "ooden chest";
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return (firstCap ? "W" : "w") + "ooden chests";
	}


	@Override
	public String getDescription() {
		return bloodandmithril.prop.furniture.WoodenChest.description;
	}


	@Override
	protected boolean internalSameAs(Item other) {
		return other instanceof WoodenChest;
	}


	@Override
	public boolean canBeCraftedBy(Individual individual) {
		return false;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		Map<Item, Integer> map = Maps.newHashMap();

		map.put(new Pine(), 10);
		map.put(ingot(Iron.class), 2);

		return map;
	}


	@Override
	public float getCraftingDuration() {
		return 30f;
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return null;
	}


	@Override
	protected Item internalCopy() {
		return new WoodenChest();
	}
}