package bloodandmithril.item.items.equipment.weapon;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.util.datastructure.WrapperForTwo.wrap;
import static bloodandmithril.world.Domain.individualTexture;

import java.util.Map;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.individuals.Humanoid;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.characters.Elf;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.material.Material;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.AnimationHelper;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.util.datastructure.WrapperForTwo;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Maps;

/**
 * One handed axe
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class OneHandedBlunt<T extends Material> extends OneHandedMeleeWeapon<T> implements Craftable {
	private static final long serialVersionUID = -8932319773500235186L;

	@SuppressWarnings("rawtypes")
	private static Map<Class<? extends OneHandedBlunt>, TextureRegion> textureRegionMap = Maps.newHashMap();
	private static Map<Class<? extends Individual>, WrapperForTwo<Animation, Vector2>> specialEffectsMap = Maps.newHashMap();

	static {
		if (ClientServerInterface.isClient()) {
			specialEffectsMap.put(Elf.class, wrap(AnimationHelper.animation(individualTexture, 64, 784, 36, 74, 10, 0.07f, PlayMode.NORMAL), new Vector2(65f, 40f)));
		}
	}

	/**
	 * Constructor
	 */
	protected OneHandedBlunt(float mass, int volume, long value, Class<T> metal) {
		super(mass, volume, true, value, metal);
	}


	@Override
	public void render(Vector2 position, float angle, boolean flipX) {
		TextureRegion texture = getTextureRegion();

		spriteBatch.draw(
			Domain.individualTexture,
			position.x - (flipX ? texture.getRegionWidth() - 15 : 15),
			position.y - 3,
			flipX ? texture.getRegionWidth() - 15 : 15,
			9,
			texture.getRegionWidth(),
			texture.getRegionHeight(),
			1f,
			1f,
			angle,
			texture.getRegionX(),
			texture.getRegionY(),
			texture.getRegionWidth(),
			texture.getRegionHeight(),
			flipX,
			false
		);
	}


	@Override
	public boolean rotates() {
		return true;
	}


	@Override
	public TextureRegion getTextureRegion() {
		return textureRegionMap.get(getClass());
	}


	@Override
	protected Vector2 getRenderCentreOffset() {
		return new Vector2(getTextureRegion().getRegionWidth() * (1f / 2f), getTextureRegion().getRegionHeight() / 2);
	}


	@Override
	public float getBaseAttackPeriod() {
		return 3f;
	}


	@Override
	public Box getActionFrameHitBox(Individual individual) {
		return new Box(
			new Vector2(
				individual.getHitBox().position.x + (individual.getCurrentAction().left() ? - individual.getHitBox().width * (3f/4f) : individual.getHitBox().width  * (3f/4f)),
				individual.getHitBox().position.y
			),
			individual.getHitBox().width,
			individual.getHitBox().height
		);
	}


	@Override
	public float getBaseMinDamage() {
		return 2.5f * Material.getMaterial(getMaterial()).getCombatMultiplier();
	}


	@Override
	public float getBaseMaxDamage() {
		return 5f * Material.getMaterial(getMaterial()).getCombatMultiplier();
	}


	@Override
	public boolean stab() {
		return false;
	}


	@Override
	public WrapperForTwo<Animation, Vector2> getAttackAnimationEffects(Individual individual) {
		switch (individual.getCurrentAction()) {
			case ATTACK_LEFT_ONE_HANDED_WEAPON:
			case ATTACK_RIGHT_ONE_HANDED_WEAPON:
				return specialEffectsMap.get(individual.getClass());

			default:
				return null;
		}
	}


	@Override
	public float getKnockbackStrength() {
		return 550;
	}


	@Override
	public int getHitSound() {
		return SoundService.stab;
	}


	@Override
	public float getParryChance() {
		return 0.15f;
	}


	@Override
	public int getBlockSound() {
		return SoundService.broadSwordBlock;
	}


	@Override
	public int getAttackNumber(Individual attacker) {
		if (attacker instanceof Humanoid) {
			return 2;
		}

		return 0;
	}


	@Override
	public float getParryChanceIgnored() {
		return 0f;
	}


	@Override
	public float getDisarmChance() {
		return 0.05f;
	}


	@Override
	public Category getType() {
		return Category.ONEHANDEDBLUNT;
	}
}