package bloodandmithril.prop.construction.craftingstation;

import static bloodandmithril.item.items.material.ArrowHeadItem.arrowHead;

import java.util.Map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.ai.task.Idle;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.core.UpdatedBy;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.dagger.BushKnife;
import bloodandmithril.item.items.equipment.weapon.dagger.CombatKnife;
import bloodandmithril.item.items.equipment.weapon.onehandedsword.Broadsword;
import bloodandmithril.item.items.equipment.weapon.onehandedsword.Machette;
import bloodandmithril.item.items.furniture.MedievalWallTorchItem;
import bloodandmithril.item.material.metal.Iron;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.updateservice.BlacksmithWorkshopUpdateService;

/**
 * An blacksmith workshop, used to smith metallic items
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@Name(name = "Blacksmith Workshop")
@UpdatedBy(BlacksmithWorkshopUpdateService.class)
public class BlacksmithWorkshop extends CraftingStation {

	private static final Map<Item, Integer> craftables = Maps.newHashMap();

	static {
		craftables.put(new BushKnife(), 1);
		craftables.put(new CombatKnife(), 1);
		craftables.put(new Machette(), 1);
		craftables.put(new Broadsword(), 1);
		craftables.put(new MedievalWallTorchItem(), 1);
		craftables.put(arrowHead(Iron.class), 25);
	}

	private static final long serialVersionUID = -7172034409582382182L;

	/** {@link TextureRegion} of the {@link Anvl} */
	public static TextureRegion BLACKSMITH_WORKSHOP;
	public static TextureRegion BLACKSMITH_WORKSHOP_WORKING;
	private int sparkCountdown = 0;

	/**
	 * Constructor
	 */
	public BlacksmithWorkshop(float x, float y) {
		super(x, y, 117, 43, 0);
		setConstructionProgress(1f);
	}


	@Override
	protected void internalRender(float constructionProgress, Graphics graphics) {
		if (isOccupied()) {
			graphics.getSpriteBatch().draw(BLACKSMITH_WORKSHOP_WORKING, position.x - width / 2, position.y);
		} else {
			graphics.getSpriteBatch().draw(BLACKSMITH_WORKSHOP, position.x - width / 2, position.y);
		}
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return BLACKSMITH_WORKSHOP;
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
		return SoundService.campfireCooking;
	}


	@Override
	public void synchronizeProp(Prop other) {
		this.setSparkCountdown(((BlacksmithWorkshop) other).getSparkCountdown());
		super.synchronizeProp(other);
	}


	@Override
	public boolean canDeconstruct() {
		return true;
	}


	@Override
	public boolean requiresConstruction() {
		return false;
	}


	@Override
	public void affectIndividual(Individual individual, float delta) {
		individual.decreaseStamina(delta / 30f);
		individual.decreaseThirst(delta / 300f);
		individual.decreaseHunger(delta / 600f);

		if (individual.getState().stamina <= 0.01f) {
			individual.getAI().setCurrentTask(new Idle());
			individual.speak("Too tired, need a break...", 1500);
		}
	}


	public int getSparkCountdown() {
		return sparkCountdown;
	}


	public void setSparkCountdown(int sparkCountdown) {
		this.sparkCountdown = sparkCountdown;
	}
}