package bloodandmithril.prop.construction.craftingstation;

import static bloodandmithril.item.items.material.PlankItem.plank;
import static bloodandmithril.item.items.material.StickItem.stick;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.WoodenBucketItem;
import bloodandmithril.item.items.equipment.offhand.Torch;
import bloodandmithril.item.items.equipment.weapon.ranged.projectile.ArrowProjectile.ArrowItem;
import bloodandmithril.item.items.furniture.WoodenChestItem;
import bloodandmithril.item.items.material.PlankItem;
import bloodandmithril.item.material.metal.Iron;
import bloodandmithril.item.material.wood.StandardWood;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

/**
 * Used to craft various goods.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class WorkBench extends CraftingStation {

	private static final Map<Item, Integer> craftables = Maps.newHashMap();

	static {
		craftables.put(ArrowItem.arrowItem(Iron.class), 1);
		craftables.put(plank(StandardWood.class), 5);
		craftables.put(stick(StandardWood.class), 5);
		craftables.put(new WoodenBucketItem(StandardWood.class), 1);
		craftables.put(new WoodenChestItem(StandardWood.class), 1);
		craftables.put(new Torch(), 1);
	}

	private static final long serialVersionUID = 3667802131168466770L;

	public static TextureRegion workbench;

	/**
	 * Constructor
	 */
	public WorkBench(float x, float y) {
		super(x, y, 90, 43, 0.1f);
		setConstructionProgress(0f);
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return workbench;
	}


	@Override
	public String getDescription() {
		return "The work bench is used to craft various commodities.";
	}


	@Override
	public String getAction() {
		return "Craft";
	}


	@Override
	public Map<Item, Integer> getCraftables() {
		return craftables;
	}


	@Override
	public void preRender() {
	}


	@Override
	public boolean canBeUsedAsFireSource() {
		return false;
	}


	@Override
	protected int getCraftingSound() {
		return -1;
	}


	@Override
	protected String internalGetTitle() {
		return "Work bench";
	}


	@Override
	public boolean canDeconstruct() {
		return true;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		Map<Item, Integer> requiredItems = newHashMap();
		requiredItems.put(PlankItem.plank(StandardWood.class), 20);
		return requiredItems;
	}


	@Override
	public boolean requiresConstruction() {
		return false;
	}


	@Override
	public void affectIndividual(Individual individual, float delta) {
		individual.decreaseThirst(delta / 600f);
		individual.decreaseHunger(delta / 900f);
	}
}