package bloodandmithril.prop.furniture;

import java.util.List;

import bloodandmithril.item.Item;
import bloodandmithril.item.equipment.Broadsword;
import bloodandmithril.item.equipment.ButterflySword;
import bloodandmithril.prop.crafting.CraftingStation;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Lists;

/**
 * An anvil, used to smith metallic items
 *
 * @author Matt
 */
public class Anvil extends CraftingStation {
	private static final long serialVersionUID = -7172034409582382182L;

	/** {@link TextureRegion} of the {@link Anvl} */
	public static TextureRegion anvil;

	/**
	 * Constructor
	 */
	public Anvil(float x, float y) {
		super(x, y, 44, 18);
		setConstructionProgress(1f);
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return anvil;
	}


	@Override
	protected String getDescription() {
		return "An anvil is a basic tool, a block with a hard surface on which another object is struck.  Used to smith metallic objects.";
	}


	@Override
	protected String getTitle() {
		return "Anvil";
	}


	@Override
	public String getAction() {
		return "Smith";
	}


	@Override
	public List<Item> getCraftables() {
		return Lists.newArrayList(
			new Broadsword(0),
			new ButterflySword(0)
		);
	}
}