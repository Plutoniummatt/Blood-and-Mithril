package bloodandmithril.item.items.container;

import static bloodandmithril.item.liquid.LiquidMixtureAnalyzer.getTitle;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import bloodandmithril.character.Individual;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.liquid.Liquid;
import bloodandmithril.item.material.crystalline.Glass;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A {@link Bottle} made from glass
 *
 * @author Matt
 */
public class GlassBottle extends LiquidContainer implements Craftable {
	private static final long serialVersionUID = 3427844446930493119L;

	public static TextureRegion GLASSBOTTLE;

	/**
	 * Constructor
	 */
	public GlassBottle(Map<Class<? extends Liquid>, Float> containedLiquids) {
		super(0.1f, 2f, containedLiquids, ItemValues.GLASSBOTTLE);
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
	public LiquidContainer clone() {
		return new GlassBottle(newHashMap(containedLiquids));
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
		return individual.getSkills().getGlassworking() >= 0;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		Map<Item, Integer> map = newHashMap();
		map.put(new Glass(), 1);
		return map;
	}


	@Override
	public float getCraftingDuration() {
		return 5f;
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return null;
	}


	@Override
	protected LiquidContainer copyContainer() {
		return new GlassBottle(newHashMap());
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		return GLASSBOTTLE;
	}
}