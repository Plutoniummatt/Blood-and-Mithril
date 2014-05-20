package bloodandmithril.prop.crafting;

import static bloodandmithril.item.equipment.weapon.Broadsword.broadSword;
import static bloodandmithril.item.equipment.weapon.ButterflySword.butterflySword;

import java.util.List;

import bloodandmithril.item.Item;
import bloodandmithril.item.material.metal.Iron;
import bloodandmithril.item.material.metal.Steel;

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
		super(x, y, 44, 18, 0);
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
	public String getTitle() {
		return "Anvil";
	}


	@Override
	public String getAction() {
		return "Smith";
	}


	@Override
	public List<Item> getCraftables() {
		return Lists.newArrayList(
			broadSword(0, Iron.class),
			broadSword(0, Steel.class),
			butterflySword(0, Iron.class),
			butterflySword(0, Steel.class)
		);
	}
}