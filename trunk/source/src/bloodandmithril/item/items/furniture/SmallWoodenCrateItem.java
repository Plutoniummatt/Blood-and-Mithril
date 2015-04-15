package bloodandmithril.item.items.furniture;

import java.util.Map;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.proficiency.proficiencies.Carpentry;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.item.items.material.PlankItem;
import bloodandmithril.item.material.Material;
import bloodandmithril.item.material.wood.Wood;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

/**
 * The {@link Item} representation of a WoodenChest.
 * Does not contain items like the {@link Container} version
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class SmallWoodenCrateItem extends FurnitureItem implements Craftable {
	private static final long serialVersionUID = -2605696279754562239L;

	private final Class<? extends Wood> wood;

	/**
	 * Constructor
	 */
	public SmallWoodenCrateItem(Class<? extends Wood> wood) {
		super(10f, 300, ItemValues.WOODENCHEST);
		this.wood = wood;
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return "Small" + Material.getMaterial(wood).getName() + " Crate";
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return "Small" + Material.getMaterial(wood).getName() + " Crates";
	}


	@Override
	public String getDescription() {
		return "A small crate constructed mostly from wood, used to store items.";
	}


	@Override
	protected boolean internalSameAs(Item other) {
		return other instanceof SmallWoodenCrateItem;
	}


	@Override
	public boolean canBeCraftedBy(Individual individual) {
		return false;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		Map<Item, Integer> map = Maps.newHashMap();

		map.put(PlankItem.plank(wood), 15);

		return map;
	}


	@Override
	public float getCraftingDuration() {
		return 30f;
	}


	@Override
	public TextureRegion getTextureRegion() {
		throw new IllegalStateException("Should not be calling this method");
	}


	@Override
	protected Item internalCopy() {
		return new SmallWoodenCrateItem(wood);
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Prop getProp() {
		bloodandmithril.prop.furniture.SmallWoodenCrateProp woodenCrate = new bloodandmithril.prop.furniture.SmallWoodenCrateProp(0, 0, wood);
		if (Domain.getActiveWorld() != null) {
			woodenCrate.setWorldId(Domain.getActiveWorld().getWorldId());
		}
		return woodenCrate;
	}


	@Override
	public void crafterEffects(Individual crafter, float delta) {
		crafter.getProficiencies().getProficiency(Carpentry.class).increaseExperience(delta * 8f);
	}
}