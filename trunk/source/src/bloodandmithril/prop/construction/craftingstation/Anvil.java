package bloodandmithril.prop.construction.craftingstation;

import java.util.Map;

import bloodandmithril.audio.SoundService;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.dagger.BushKnife;
import bloodandmithril.item.items.equipment.weapon.dagger.CombatKnife;
import bloodandmithril.item.items.equipment.weapon.onehandedsword.Broadsword;
import bloodandmithril.item.items.equipment.weapon.onehandedsword.Machette;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

/**
 * An anvil, used to smith metallic items
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Anvil extends CraftingStation {

	private static final Map<Item, Integer> craftables = Maps.newHashMap();

	static {
		craftables.put(new BushKnife(), 1);
		craftables.put(new CombatKnife(), 1);
		craftables.put(new Machette(), 1);
		craftables.put(new Broadsword(), 1);
	}

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
	public String getDescription() {
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
		return SoundService.anvil;
	}
}