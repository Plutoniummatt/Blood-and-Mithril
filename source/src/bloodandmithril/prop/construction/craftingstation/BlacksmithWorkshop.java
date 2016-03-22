package bloodandmithril.prop.construction.craftingstation;

import static bloodandmithril.item.items.material.ArrowHeadItem.arrowHead;
import static bloodandmithril.networking.ClientServerInterface.isClient;
import static bloodandmithril.networking.ClientServerInterface.isServer;

import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Maps;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.ai.task.Idle;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.graphics.particles.ParticleService;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.dagger.BushKnife;
import bloodandmithril.item.items.equipment.weapon.dagger.CombatKnife;
import bloodandmithril.item.items.equipment.weapon.onehandedsword.Broadsword;
import bloodandmithril.item.items.equipment.weapon.onehandedsword.Machette;
import bloodandmithril.item.items.furniture.MedievalWallTorchItem;
import bloodandmithril.item.material.metal.Iron;
import bloodandmithril.prop.Prop;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;

/**
 * An blacksmith workshop, used to smith metallic items
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@Name(name = "Blacksmith Workshop")
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
	public static TextureRegion blackSmithWorkshop;
	public static TextureRegion blackSmithWorkshopWorking;
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
			graphics.getSpriteBatch().draw(blackSmithWorkshopWorking, position.x - width / 2, position.y);
		} else {
			graphics.getSpriteBatch().draw(blackSmithWorkshop, position.x - width / 2, position.y);
		}
	}


	@Override
	public synchronized void update(float delta) {
		super.update(delta);

		if (isOccupied()) {
			if (isClient()) {
				if (BloodAndMithrilClient.isOnScreen(position, 50f)) {
					ParticleService.randomVelocityDiminishing(position.cpy().add(35, height - 2), 10f, 15f, Colors.FIRE_START, Colors.FIRE_START, Util.getRandom().nextFloat() * 1.5f, 2f, MovementMode.EMBER, Util.getRandom().nextInt(1000), Depth.MIDDLEGROUND, false, Colors.FIRE_END);
					ParticleService.randomVelocityDiminishing(position.cpy().add(35, height - 2), 7f, 30f, Colors.LIGHT_SMOKE, Color.BLACK, 5f, 0f, MovementMode.EMBER, Util.getRandom().nextInt(4000), Depth.MIDDLEGROUND, false, null);
				}
			}

			if (sparkCountdown <= 0) {
				if (isClient()) {
					ParticleService.parrySpark(position.cpy().add(-40, height - 10), new Vector2(-30f, -100f), Depth.MIDDLEGROUND, Color.WHITE, new Color(1f, 0.8f, 0.3f, 1f), 3500, true, 30, 200f);
				}
				if (isServer()) {
					if (Util.getRandom().nextBoolean()) {
						SoundService.play(SoundService.anvil1, position.cpy(), true, this);
					} else {
						SoundService.play(SoundService.anvil1, position.cpy(), true, this);
					}
				}
				sparkCountdown = 90;
			}

			sparkCountdown--;
		}
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
		return SoundService.campfireCooking;
	}


	@Override
	public void synchronizeProp(Prop other) {
		this.sparkCountdown = ((BlacksmithWorkshop) other).sparkCountdown;
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
}