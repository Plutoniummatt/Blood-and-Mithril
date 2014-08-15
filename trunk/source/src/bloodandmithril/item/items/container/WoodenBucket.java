package bloodandmithril.item.items.container;

import static bloodandmithril.item.liquid.LiquidMixtureAnalyzer.getTitle;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.material.Plank;
import bloodandmithril.item.liquid.Liquid;
import bloodandmithril.item.material.Material;
import bloodandmithril.item.material.wood.Wood;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

/**
 * Bucket that can contain {@link Liquid}s
 *
 * @author Matt
 */
public class WoodenBucket extends LiquidContainer implements Craftable {
	private static final long serialVersionUID = -4162891941797527242L;

	private final Class<? extends Wood> wood;

	/**
	 * Constructor
	 */
	public WoodenBucket(Map<Class<? extends Liquid>, Float> containedLiquids, Class<? extends Wood> wood) {
		super(1f, 16f, containedLiquids, ItemValues.WOODENBUCKET);
		this.wood = wood;
	}


	/**
	 * Constructor
	 */
	public WoodenBucket(Class<? extends Wood> wood) {
		super(1f, 16f, Maps.newHashMap(), ItemValues.WOODENBUCKET);
		this.wood = wood;
	}


	@Override
	public String getDescription() {
		return "A wooden bucket made from " + Material.getMaterial(wood).getName();
	}


	@Override
	protected String getCotainerTitle() {
		return "Wooden Bucket";
	}


	@Override
	public LiquidContainer clone() {
		return new WoodenBucket(newHashMap(containedLiquids), wood);
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		String content = "";
		if (!containedLiquids.isEmpty()) {
			content = " of " + getTitle(containedLiquids, getTotalAmount()) + " (" + String.format("%.2f", getTotalAmount()) + "/" + String.format("%.2f", maxAmount) + ")";
		}

		return Material.getMaterial(wood).getName() + " Bucket" + content;
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		String content = "";
		if (!containedLiquids.isEmpty()) {
			content = " of " + getTitle(containedLiquids, getTotalAmount()) + " (" + String.format("%.2f", getTotalAmount()) + "/" + String.format("%.2f", maxAmount) + ")";
		}

		return Material.getMaterial(wood).getName() + " Buckets" + content;
	}


	@Override
	public boolean canBeCraftedBy(Individual individual) {
		return individual.getSkills().getCarpentry() >= 0;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		Map<Item, Integer> map = Maps.newHashMap();
		map.put(Plank.plank(wood), 2);
		return map;
	}


	@Override
	public float getCraftingDuration() {
		return 10f;
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return null; // TODO wooden bucket texture region
	}


	@Override
	protected LiquidContainer copyContainer() {
		return new WoodenBucket(wood);
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		// TODO Auto-generated method stub
		return null;
	}
}