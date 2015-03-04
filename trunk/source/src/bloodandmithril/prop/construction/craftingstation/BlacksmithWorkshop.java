package bloodandmithril.prop.construction.craftingstation;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.item.items.material.ArrowHead.arrowHead;

import java.util.Map;

import bloodandmithril.audio.SoundService;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.graphics.particles.ParticleService;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.dagger.BushKnife;
import bloodandmithril.item.items.equipment.weapon.dagger.CombatKnife;
import bloodandmithril.item.items.equipment.weapon.onehandedsword.Broadsword;
import bloodandmithril.item.items.equipment.weapon.onehandedsword.Machette;
import bloodandmithril.item.material.metal.Iron;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain.Depth;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
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
	public static TextureRegion blackSmithWorkshopWorking;
	private int sparkCountdown;

	/**
	 * Constructor
	 */
	public BlacksmithWorkshop(float x, float y) {
		super(x, y, 116, 61, 0);
		setConstructionProgress(1f);
	}
	
	
	@Override
	protected void internalRender(float constructionProgress) {
		if (isOccupied()) {
			spriteBatch.draw(blackSmithWorkshopWorking, position.x - width / 2, position.y);
		} else {
			spriteBatch.draw(blackSmithWorkshop, position.x - width / 2, position.y);
		}
	}
	
	
	@Override
	public synchronized void update(float delta) {
		super.update(delta);

		if (isOccupied()) {
			if (BloodAndMithrilClient.isOnScreen(position, 50f)) {
				ParticleService.randomVelocityDiminishing(position.cpy().add(17, height - 23), 6f, 30f, Color.ORANGE, 2f, 8f, MovementMode.WEIGHTLESS, Util.getRandom().nextInt(200), Depth.MIDDLEGROUND, false, Color.RED);
				ParticleService.randomVelocityDiminishing(position.cpy().add(17, height - 23), 6f, 30f, Color.ORANGE, 1f, 6f, MovementMode.WEIGHTLESS, Util.getRandom().nextInt(200), Depth.MIDDLEGROUND, false, Color.RED);
				ParticleService.randomVelocityDiminishing(position.cpy().add(17, height - 23), 10f, 10f, Colors.LIGHT_SMOKE, 10f, 0f, MovementMode.EMBER, Util.getRandom().nextInt(3000) + 3000, Depth.BACKGROUND, false, null);
				
				
				if (sparkCountdown == 0) {
					ParticleService.parrySpark(position.cpy().add(-37, height - 33), new Vector2(-80f, -100f), Depth.MIDDLEGROUND, new Color(1f, 0.85f, 0.5f, 1f), 1000);
					sparkCountdown = 90;
				}
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