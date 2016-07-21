package bloodandmithril.item.items.container;

import static bloodandmithril.item.liquid.LiquidMixtureAnalyzer.getTitle;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.proficiency.proficiencies.Carpentry;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.material.PlankItem;
import bloodandmithril.item.liquid.Liquid;
import bloodandmithril.item.material.Material;
import bloodandmithril.item.material.wood.Wood;

/**
 * Bucket that can contain {@link Liquid}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class WoodenBucketItem extends LiquidContainerItem implements Craftable {
	private static final long serialVersionUID = -4162891941797527242L;

	private final Class<? extends Wood> wood;

	/**
	 * Constructor
	 */
	public WoodenBucketItem(final Map<Class<? extends Liquid>, Float> containedLiquids, final Class<? extends Wood> wood) {
		super(1f, 20, 16f, containedLiquids, ItemValues.WOODENBUCKET);
		this.wood = wood;
	}


	/**
	 * Constructor
	 */
	public WoodenBucketItem(final Class<? extends Wood> wood) {
		super(1f, 20, 16f, Maps.newHashMap(), ItemValues.WOODENBUCKET);
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
	public LiquidContainerItem clone() {
		return new WoodenBucketItem(newHashMap(containedLiquids), wood);
	}


	@Override
	protected String internalGetSingular(final boolean firstCap) {
		String content = "";
		if (!containedLiquids.isEmpty()) {
			content = " of " + getTitle(containedLiquids, getTotalAmount()) + " (" + String.format("%.2f", getTotalAmount()) + "/" + String.format("%.2f", maxAmount) + ")";
		}

		return Material.getMaterial(wood).getName() + " Bucket" + content;
	}


	@Override
	protected String internalGetPlural(final boolean firstCap) {
		String content = "";
		if (!containedLiquids.isEmpty()) {
			content = " of " + getTitle(containedLiquids, getTotalAmount()) + " (" + String.format("%.2f", getTotalAmount()) + "/" + String.format("%.2f", maxAmount) + ")";
		}

		return Material.getMaterial(wood).getName() + " Buckets" + content;
	}


	@Override
	public boolean canBeCraftedBy(final Individual individual) {
		return individual.getProficiencies().getProficiency(Carpentry.class).getLevel() >= 0;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		final Map<Item, Integer> map = Maps.newHashMap();
		map.put(PlankItem.plank(wood), 2);
		return map;
	}


	@Override
	public float getCraftingDuration() {
		return 10f;
	}


	@Override
	public TextureRegion getTextureRegion() {
		return null; // TODO wooden bucket texture region
	}


	@Override
	protected LiquidContainerItem copyContainer() {
		return new WoodenBucketItem(wood);
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void crafterEffects(final Individual crafter, final float delta) {
		crafter.getProficiencies().getProficiency(Carpentry.class).increaseExperience(delta * 5f);
	}
}