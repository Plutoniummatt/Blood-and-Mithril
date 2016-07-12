package bloodandmithril.item.items.furniture;

import static bloodandmithril.item.items.material.IngotItem.ingot;

import java.util.Map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.proficiency.proficiencies.Carpentry;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.Wiring;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.item.items.material.PlankItem;
import bloodandmithril.item.material.Material;
import bloodandmithril.item.material.metal.Iron;
import bloodandmithril.item.material.wood.Wood;
import bloodandmithril.prop.Prop;

/**
 * The {@link Item} representation of a {@link WoodenChestItem}.
 * Does not contain items like the {@link Container} version
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class WoodenChestItem extends FurnitureItem implements Craftable {
	private static final long serialVersionUID = -6430848300222528418L;

	private final Class<? extends Wood> wood;

	/**
	 * Constructor
	 */
	public WoodenChestItem(final Class<? extends Wood> wood) {
		super(10f, 300, ItemValues.WOODENCHEST);
		this.wood = wood;
	}


	@Override
	protected String internalGetSingular(final boolean firstCap) {
		return Material.getMaterial(wood).getName() + " Chest";
	}


	@Override
	protected String internalGetPlural(final boolean firstCap) {
		return Material.getMaterial(wood).getName() + " Chests";
	}


	@Override
	public String getDescription() {
		return "A chest constructed mostly from wood, used to store items, this one is made from " + Material.getMaterial(wood).getName() + ".";
	}


	@Override
	protected boolean internalSameAs(final Item other) {
		return other instanceof WoodenChestItem;
	}


	@Override
	public boolean canBeCraftedBy(final Individual individual) {
		return false;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		final Map<Item, Integer> map = Maps.newHashMap();

		map.put(PlankItem.plank(wood), 10);
		map.put(ingot(Iron.class), 2);

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
		return new WoodenChestItem(wood);
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Prop getProp() {
		final bloodandmithril.prop.furniture.WoodenChestProp woodenChest = new bloodandmithril.prop.furniture.WoodenChestProp(0, 0, 1000000f, 800, wood);
		if (Wiring.injector().getInstance(GameClientStateTracker.class).getActiveWorld() != null) {
			woodenChest.setWorldId(Wiring.injector().getInstance(GameClientStateTracker.class).getActiveWorld().getWorldId());
		}
		return woodenChest;
	}


	@Override
	public void crafterEffects(final Individual crafter, final float delta) {
		crafter.getProficiencies().getProficiency(Carpentry.class).increaseExperience(delta * 8f);
	}
}