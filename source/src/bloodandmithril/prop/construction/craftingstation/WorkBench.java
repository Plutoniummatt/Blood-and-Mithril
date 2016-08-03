package bloodandmithril.prop.construction.craftingstation;

import static bloodandmithril.item.items.material.PlankItem.plank;
import static bloodandmithril.item.items.material.StickItem.stick;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

import bloodandmithril.character.IndividualStateService;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.core.UpdatedBy;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.RenderPropWith;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.WoodenBucketItem;
import bloodandmithril.item.items.equipment.offhand.Torch;
import bloodandmithril.item.items.equipment.weapon.ranged.projectile.ArrowProjectile.ArrowItem;
import bloodandmithril.item.items.furniture.WoodenChestItem;
import bloodandmithril.item.items.material.PlankItem;
import bloodandmithril.item.material.metal.Iron;
import bloodandmithril.item.material.wood.StandardWood;
import bloodandmithril.prop.renderservice.ConstructionRenderingService;
import bloodandmithril.prop.updateservice.CraftingStationUpdateService;

/**
 * Used to craft various goods.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@Name(name = "Work bench")
@UpdatedBy(CraftingStationUpdateService.class)
@RenderPropWith(ConstructionRenderingService.class)
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

	public static TextureRegion WORKBENCH;

	/**
	 * Constructor
	 */
	public WorkBench(final float x, final float y) {
		super(x, y, 90, 43, 0.1f);
		setConstructionProgress(0f);
	}


	@Override
	public TextureRegion getTextureRegion() {
		return WORKBENCH;
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
	public boolean canBeUsedAsFireSource() {
		return false;
	}


	@Override
	protected int getCraftingSound() {
		return -1;
	}


	@Override
	public boolean canDeconstruct() {
		return true;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		final Map<Item, Integer> requiredItems = newHashMap();
		requiredItems.put(PlankItem.plank(StandardWood.class), 20);
		return requiredItems;
	}


	@Override
	public boolean requiresConstruction() {
		return false;
	}


	@Override
	public void affectIndividual(final Individual individual, final float delta) {
		final IndividualStateService individualStateService = Wiring.injector().getInstance(IndividualStateService.class);

		individualStateService.decreaseHunger(individual, delta / 900f);
		individualStateService.decreaseThirst(individual, delta / 600f);
	}
}