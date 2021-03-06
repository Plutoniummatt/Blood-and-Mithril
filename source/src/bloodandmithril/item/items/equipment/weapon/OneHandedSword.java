package bloodandmithril.item.items.equipment.weapon;

import static bloodandmithril.util.datastructure.WrapperForTwo.wrap;
import static com.badlogic.gdx.graphics.g2d.Animation.PlayMode.NORMAL;

import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Maps;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.IndividualStateService;
import bloodandmithril.character.conditions.Bleeding;
import bloodandmithril.character.individuals.Humanoid;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.characters.Elf;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.graphics.Textures;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.items.equipment.weapon.onehandedsword.Broadsword;
import bloodandmithril.item.items.equipment.weapon.onehandedsword.Machette;
import bloodandmithril.item.material.Material;
import bloodandmithril.item.material.metal.Metal;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.AnimationHelper;
import bloodandmithril.util.Util;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.util.datastructure.WrapperForTwo;

@Copyright("Matthew Peck 2014")
public abstract class OneHandedSword<T extends Metal> extends OneHandedMeleeWeapon<T> implements Craftable {
	private static final long serialVersionUID = -8932319773500235186L;

	@SuppressWarnings("rawtypes")
	private static Map<Class<? extends OneHandedSword>, TextureRegion> textureRegionMap = Maps.newHashMap();
	private static Map<Class<? extends Individual>, WrapperForTwo<Animation, Vector2>> specialEffectsMap = Maps.newHashMap();
	private static Map<Class<? extends Individual>, WrapperForTwo<Animation, Vector2>> specialEffectsMapStab = Maps.newHashMap();

	static {
		if (ClientServerInterface.isClient()) {
			textureRegionMap.put(Machette.class, new TextureRegion(Textures.INDIVIDUAL_TEXTURE, 0, 800, 63, 17));
			textureRegionMap.put(Broadsword.class, new TextureRegion(Textures.INDIVIDUAL_TEXTURE, 0, 834, 63, 17));
			specialEffectsMap.put(Elf.class, wrap(AnimationHelper.animation(Textures.INDIVIDUAL_TEXTURE, 64, 784, 36, 74, 10, 0.07f, NORMAL), new Vector2(65f, 40f)));
			specialEffectsMapStab.put(Elf.class, wrap(AnimationHelper.animation(Textures.INDIVIDUAL_TEXTURE, 64, 858, 102, 25, 8, 0.07f, NORMAL), new Vector2(18f, 32f)));
		}
	}

	/**
	 * Constructor
	 */
	protected OneHandedSword(final float mass, final int volume, final long value, final Class<T> metal) {
		super(mass, volume, true, value, metal);
	}


	@Override
	public void render(final Vector2 position, final float angle, final boolean flipX, final Graphics graphics) {
		final TextureRegion texture = getTextureRegion();

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
		return 1f;
	}


	@Override
	public int getAttackNumber(final Individual attacker) {
		if (attacker instanceof Humanoid) {
			return 2;
		}

		return 0;
	}


	@Override
	public Box getActionFrameHitBox(final Individual individual) {
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
		return 3.5f * Material.getMaterial(getMaterial()).getCombatMultiplier();
	}


	@Override
	public float getBaseMaxDamage() {
		return 7.5f * Material.getMaterial(getMaterial()).getCombatMultiplier();
	}


	@Override
	public boolean stab() {
		return Util.getRandom().nextBoolean();
	}


	@Override
	public WrapperForTwo<Animation, Vector2> getAttackAnimationEffects(final Individual individual) {
		switch (individual.getCurrentAction()) {
			case ATTACK_LEFT_ONE_HANDED_WEAPON:
			case ATTACK_RIGHT_ONE_HANDED_WEAPON:
				return specialEffectsMap.get(individual.getClass());

			case ATTACK_LEFT_ONE_HANDED_WEAPON_STAB:
			case ATTACK_RIGHT_ONE_HANDED_WEAPON_STAB:
				return specialEffectsMapStab.get(individual.getClass());

			default:
				return null;
		}
	}


	@Override
	public float getKnockbackStrength() {
		return 350;
	}


	@Override
	public int getHitSound() {
		return SoundService.stab;
	}


	@Override
	public float getParryChance() {
		return 0.35f;
	}


	@Override
	public int getBlockSound() {
		return SoundService.broadSwordBlock;
	}


	@Override
	public float getParryChanceIgnored() {
		return 0f;
	}


	@Override
	public ItemCategory getType() {
		return ItemCategory.ONEHANDEDSWORD;
	}


	@Override
	public void specialEffect(final Individual individual) {
		Wiring.injector().getInstance(IndividualStateService.class).addCondition(individual, new Bleeding(0.05f));
	}


	@Override
	public float getBaseCritChance() {
		return 0.2f;
	}


	@Override
	public float getCritDamageMultiplier() {
		return 1.5f;
	}
}