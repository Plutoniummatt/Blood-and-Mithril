package bloodandmithril.prop.construction.craftingstation;

import static bloodandmithril.item.items.material.ArrowHead.arrowHead;

import java.util.Map;

import bloodandmithril.audio.SoundService;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.dagger.BushKnife;
import bloodandmithril.item.items.equipment.weapon.dagger.CombatKnife;
import bloodandmithril.item.items.equipment.weapon.onehandedsword.Broadsword;
import bloodandmithril.item.items.equipment.weapon.onehandedsword.Machette;
import bloodandmithril.item.material.metal.Iron;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

/**
 * An anvil, used to smith metallic items
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class BlacksmithWorkshop extends CraftingStation {

	private static final Map<Item, Integer> craftables = Maps.newHashMap();

	static {
		craftables.put(new BushKnife(), 1);
		craftables.put(new CombatKnife(), 1);
		craftables.put(new Machette(), 1);
		craftables.put(new Broadsword(), 1);
		craftables.put(arrowHead(Iron.class), 25);
	}

	private static final long serialVersionUID = -7172034409582382182L;

	/** {@link TextureRegion} of the {@link Anvl} */
	public static TextureRegion blackSmithWorkshop;

	/**
	 * Constructor
	 */
	public BlacksmithWorkshop(float x, float y) {
		super(x, y, 71, 31, 0);
		setConstructionProgress(1f);
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return blackSmithWorkshop;
	}


	@Override
	public String getDescription() {
		return "A blacksmith workshop, with an anvil and necessary tools to shape metal into useful objects.";
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


	@Override
	protected String internalGetTitle() {
		return "Blacksmith Workshop";
	}


	@Override
	public boolean canDeconstruct() {
		return true;
	}


	@Override
	public boolean requiresConstruction() {
		return false;
	}
}