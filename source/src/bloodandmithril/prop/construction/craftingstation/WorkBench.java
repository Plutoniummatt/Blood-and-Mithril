package bloodandmithril.prop.construction.craftingstation;

import static bloodandmithril.item.items.material.Plank.plank;
import static bloodandmithril.item.items.material.Stick.stick;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.WoodenBucket;
import bloodandmithril.item.items.equipment.weapon.ranged.projectile.Arrow.ArrowItem;
import bloodandmithril.item.items.furniture.WoodenChest;
import bloodandmithril.item.items.material.Plank;
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
		craftables.put(new WoodenBucket(StandardWood.class), 1);
		craftables.put(new WoodenChest(StandardWood.class), 1);
	}

	private static final long serialVersionUID = 3667802131168466770L;

	public static TextureRegion workbench;

	/**
	 * Constructor
	 */
	public WorkBench(float x, float y) {
		super(x, y, 80, 33, 0.1f);
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
		requiredItems.put(Plank.plank(StandardWood.class), 20);
		return requiredItems;
	}


	@Override
	public boolean requiresConstruction() {
		return false;
	}
}