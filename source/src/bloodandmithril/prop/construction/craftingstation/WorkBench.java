package bloodandmithril.prop.construction.craftingstation;

import static bloodandmithril.item.items.material.Plank.plank;

import java.util.Map;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.WoodenBucket;
import bloodandmithril.item.items.furniture.WoodenChest;
import bloodandmithril.item.material.wood.Pine;

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
		craftables.put(plank(Pine.class), 5);
		craftables.put(new WoodenBucket(Pine.class), 1);
		craftables.put(new WoodenChest(Pine.class), 1);
	}

	private static final long serialVersionUID = 3667802131168466770L;

	public static TextureRegion workbench;

	/**
	 * Constructor
	 */
	public WorkBench(float x, float y) {
		super(x, y, 80, 33, 0f);
		setConstructionProgress(1f);
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return workbench;
	}


	@Override
	protected String getDescription() {
		return "The work bench is used to craft various commodities.";
	}


	@Override
	public String getTitle() {
		return "Work bench";
	}


	@Override
	public String getAction() {
		return "Craft";
	}


	@Override
	public Map<Item, Integer> getCraftables() {
		return craftables;
	}
}