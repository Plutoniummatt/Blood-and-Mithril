package bloodandmithril.item.items.equipment.weapon;

import static bloodandmithril.util.datastructure.WrapperForTwo.wrap;

import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Maps;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.individuals.Humanoid;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.characters.Elf;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.graphics.Textures;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.material.Material;
import bloodandmithril.item.material.metal.Metal;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.AnimationHelper;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.util.datastructure.WrapperForTwo;

/**
 * One handed axe
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class OneHandedAxe<T extends Metal> extends OneHandedMeleeWeapon<T> implements Craftable {
	private static final long serialVersionUID = -8932319773500235186L;

	@SuppressWarnings("rawtypes")
	private static Map<Class<? extends OneHandedSword>, TextureRegion> textureRegionMap = Maps.newHashMap();
	private static Map<Class<? extends Individual>, WrapperForTwo<Animation, Vector2>> specialEffectsMap = Maps.newHashMap();

	static {
		if (ClientServerInterface.isClient()) {
			specialEffectsMap.put(Elf.class, wrap(AnimationHelper.animation(Textures.INDIVIDUAL_TEXTURE, 64, 784, 36, 74, 10, 0.07f, PlayMode.NORMAL), new Vector2(65f, 40f)));
		}
	}

	/**
	 * Constructor
	 */
	protected OneHandedAxe(float mass, int volume, long value, Class<T> metal) {
		super(mass, volume, true, value, metal);
	}


	@Override
	public void render(Vector2 position, float angle, boolean flipX, Graphics graphics) {
		TextureRegion texture = getTextureRegion();

		graphics.getSpriteBatch().draw(
			Textures.INDIVIDUAL_TEXTURE,
			position.x - (flipX ? texture.getRegionWidth() - 17 : 17),
			position.y - 9,
			flipX ? texture.getRegionWidth() - 17 : 17,
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
		return new Vector2(getTextureRegion().getRegionWidth() * (5f / 7f), getTextureRegion().getRegionHeight() / 2);
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
		return 7f * Material.getMaterial(getMaterial()).getCombatMultiplier();
	}


	@Override
	public float getBaseMaxDamage() {
		return 9f * Material.getMaterial(getMaterial()).getCombatMultiplier();
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
		return 450;
	}


	@Override
	public int getHitSound() {
		return SoundService.stab;
	}


	@Override
	public float getParryChance() {
		return 0.25f;
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
	public ItemCategory getType() {
		return ItemCategory.ONEHANDEDAXE;
	}
}