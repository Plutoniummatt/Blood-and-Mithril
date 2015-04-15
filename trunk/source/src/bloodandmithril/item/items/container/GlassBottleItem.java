package bloodandmithril.item.items.container;

import static bloodandmithril.item.liquid.LiquidMixtureAnalyzer.getTitle;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.material.GlassItem;
import bloodandmithril.item.items.material.RockItem;
import bloodandmithril.item.liquid.Liquid;
import bloodandmithril.item.material.mineral.Coal;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A {@link Bottle} made from glass
 *
 * @author Matt
 */
public class GlassBottleItem extends LiquidContainerItem implements Craftable {
	private static final long serialVersionUID = 3427844446930493119L;

	public static TextureRegion GLASSBOTTLE_ICON;
	public static TextureRegion GLASSBOTTLE_ITEM;

	/**
	 * Constructor
	 */
	public GlassBottleItem(Map<Class<? extends Liquid>, Float> containedLiquids) {
		super(0.1f, 4, 2f, containedLiquids, ItemValues.GLASSBOTTLE);
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		String content = "";
		if (!containedLiquids.isEmpty()) {
			content = " of " + getTitle(containedLiquids, getTotalAmount()) + " (" + String.format("%.2f", getTotalAmount()) + "/" + String.format("%.2f", maxAmount) + ")";
		}

		return (firstCap ? "G" : "g") + "lass bottle" + content;
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		String content = "";
		if (!containedLiquids.isEmpty()) {
			content = " of " + getTitle(containedLiquids, getTotalAmount()) + " (" + String.format("%.2f", getTotalAmount()) + "/" + String.format("%.2f", maxAmount) + ")";
		}

		return (firstCap ? "G" : "g") + "lass bottles" + content;
	}


	@Override
	public LiquidContainerItem clone() {
		return new GlassBottleItem(newHashMap(containedLiquids));
	}


	@Override
	public String getDescription() {
		return "A bottle made from glass";
	}


	@Override
	protected String getCotainerTitle() {
		return "Glass bottle";
	}


	@Override
	public boolean canBeCraftedBy(Individual individual) {
		// TODO
		return true;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		Map<Item, Integer> map = newHashMap();
		map.put(new GlassItem(), 1);
		map.put(RockItem.rock(Coal.class), 1);
		return map;
	}


	@Override
	public float getCraftingDuration() {
		return 5f;
	}


	@Override
	public TextureRegion getTextureRegion() {
		return GLASSBOTTLE_ITEM;
	}


	@Override
	protected LiquidContainerItem copyContainer() {
		return new GlassBottleItem(newHashMap());
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		return null;
	}


	@Override
	public void crafterEffects(Individual crafter, float delta) {
	}
}