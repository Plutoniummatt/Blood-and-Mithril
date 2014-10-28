package bloodandmithril.item.items.equipment.weapon;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.util.datastructure.WrapperForTwo.wrap;
import static bloodandmithril.world.Domain.individualTexture;
import static com.badlogic.gdx.graphics.g2d.Animation.NORMAL;

import java.util.Map;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.individuals.Humanoid;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.characters.Elf;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.items.equipment.weapon.dagger.BushKnife;
import bloodandmithril.item.items.equipment.weapon.dagger.CombatKnife;
import bloodandmithril.item.material.Material;
import bloodandmithril.item.material.metal.Metal;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.AnimationHelper;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.util.datastructure.WrapperForTwo;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Maps;

@Copyright("Matthew Peck 2014")
public abstract class Dagger<T extends Metal> extends OneHandedMeleeWeapon<T> implements Craftable {
	private static final long serialVersionUID = -8932319773500235186L;

	@SuppressWarnings("rawtypes")
	private static Map<Class<? extends Dagger>, TextureRegion> textureRegionMap = Maps.newHashMap();
	private static Map<Class<? extends Individual>, WrapperForTwo<Animation, Vector2>> specialEffectsMap = Maps.newHashMap();

	static {
		if (ClientServerInterface.isClient()) {
			textureRegionMap.put(BushKnife.class, new TextureRegion(Domain.individualTexture, 0, 784, 43, 13));
			textureRegionMap.put(CombatKnife.class, new TextureRegion(Domain.individualTexture, 0, 818, 43, 13));
			specialEffectsMap.put(Elf.class, wrap(AnimationHelper.animation(individualTexture, 64, 858, 102, 25, 8, 0.07f, NORMAL), new Vector2(10f, 34f)));
		}
	}


	/**
	 * Constructor
	 */
	protected Dagger(float mass, int volume, long value, Class<T> metal) {
		super(mass, volume, true, value, metal);
	}


	@Override
	public void render(Vector2 position, float angle, boolean flipX) {
		TextureRegion textureRegion = getTextureRegion();

		spriteBatch.draw(
			Domain.individualTexture,
			position.x - (flipX ? textureRegion.getRegionWidth() - 15 : 15),
			position.y - 7,
			flipX ? textureRegion.getRegionWidth() - 15 : 15,
			7,
			textureRegion.getRegionWidth(),
			textureRegion.getRegionHeight(),
			1f,
			1f,
			angle,
			textureRegion.getRegionX(),
			textureRegion.getRegionY(),
			textureRegion.getRegionWidth(),
			textureRegion.getRegionHeight(),
			flipX,
			false
		);
	}


	@Override
	public boolean rotates() {
		return true;
	}


	@Override
	protected TextureRegion getTextureRegion() {
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
	public Box getActionFrameHitBox(Individual individual) {
		return new Box(
			new Vector2(
				individual.getHitBox().position.x + (individual.getCurrentAction().left() ? - individual.getHitBox().width * (2f/3f) : individual.getHitBox().width * (2f/3f)),
				individual.getHitBox().position.y
			),
			individual.getHitBox().width * 2 / 3,
			individual.getHitBox().height
		);
	}


	@Override
	public float getBaseMinDamage() {
		return 1.5f * Material.getMaterial(getMaterial()).getCombatMultiplier();
	}


	@Override
	public float getBaseMaxDamage() {
		return 3.5f * Material.getMaterial(getMaterial()).getCombatMultiplier();
	}


	@Override
	public boolean stab() {
		return true;
	}


	@Override
	public WrapperForTwo<Animation, Vector2> getAttackAnimationEffects(Individual individual) {
		switch (individual.getCurrentAction()) {
			case ATTACK_LEFT_ONE_HANDED_WEAPON_STAB:
			case ATTACK_RIGHT_ONE_HANDED_WEAPON_STAB:
				return specialEffectsMap.get(individual.getClass());

			default:
				return null;
		}
	}


	@Override
	public float getKnockbackStrength() {
		return 50;
	}


	@Override
	public int getAttackNumber(Individual attacker) {
		if (attacker instanceof Humanoid) {
			return 2;
		}

		return 0;
	}


	@Override
	public int getHitSound() {
		return SoundService.stab;
	}


	@Override
	public float getParryChance() {
		return 0.05f;
	}


	@Override
	public int getBlockSound() {
		return SoundService.broadSwordBlock;
	}


	@Override
	public float getParryChanceIgnored() {
		return 0.5f;
	}


	@Override
	public float getDisarmChance() {
		return 0.01f;
	}


	@Override
	public String getType() {
		return "Dagger";
	}


	@Override
	public void specialEffect(Individual individual) {

	}


	@Override
	public float getBaseCritChance() {
		return 0.35f;
	}


	@Override
	public float getCritDamageMultiplier() {
		return 2f;
	}
}