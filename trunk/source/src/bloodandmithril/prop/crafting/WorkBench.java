package bloodandmithril.prop.crafting;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import bloodandmithril.item.Item;
import bloodandmithril.item.container.WoodenBucket;
import bloodandmithril.item.furniture.WoodenChest;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Used to craft various goods.
 *
 * @author Matt
 */
public class WorkBench extends CraftingStation {
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
	public List<Item> getCraftables() {
		return newArrayList(new WoodenBucket(), new WoodenChest());
	}
}