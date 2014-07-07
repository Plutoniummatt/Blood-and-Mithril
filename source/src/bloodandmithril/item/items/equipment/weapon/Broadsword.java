package bloodandmithril.item.items.equipment.weapon;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.util.datastructure.WrapperForTwo.wrap;
import static bloodandmithril.world.Domain.individualTexture;
import static com.badlogic.gdx.graphics.g2d.Animation.NORMAL;

import java.util.Map;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.conditions.Bleeding;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.characters.Elf;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.material.Ingot;
import bloodandmithril.item.material.Material;
import bloodandmithril.item.material.metal.Iron;
import bloodandmithril.item.material.metal.Metal;
import bloodandmithril.item.material.metal.Steel;
import bloodandmithril.util.AnimationHelper;
import bloodandmithril.util.Util;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.util.datastructure.WrapperForTwo;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Maps;

public class Broadsword<T extends Metal> extends OneHandedMeleeWeapon<T> implements Craftable {
	private static final long serialVersionUID = -8932319773500235186L;

	private static Map<Class<? extends Material>, TextureRegion> materialTextureRegionMap = Maps.newHashMap();
	private static Map<Class<? extends Individual>, WrapperForTwo<Animation, Vector2>> specialEffectsMap = Maps.newHashMap();
	private static Map<Class<? extends Individual>, WrapperForTwo<Animation, Vector2>> specialEffectsMapStab = Maps.newHashMap();

	static {
		materialTextureRegionMap.put(Iron.class, new TextureRegion(Domain.individualTexture, 0, 800, 63, 17));
		materialTextureRegionMap.put(Steel.class, new TextureRegion(Domain.individualTexture, 0, 834, 63, 17));
		specialEffectsMap.put(Elf.class, wrap(AnimationHelper.animation(individualTexture, 64, 784, 36, 74, 10, 0.07f, NORMAL), new Vector2(65f, 40f)));
		specialEffectsMapStab.put(Elf.class, wrap(AnimationHelper.animation(individualTexture, 64, 858, 102, 25, 8, 0.07f, NORMAL), new Vector2(18f, 32f)));
	}

	/**
	 * Constructor
	 */
	private Broadsword(long value, Class<T> metal) {
		super(2, true, value, metal);
	}


	/**
	 * @return Static instance getter
	 */
	public static <T extends Metal> Broadsword<T> broadSword(long value, Class<T> metal) {
		return new Broadsword<T>(value, metal);
	}


	@Override
	protected String weaponGetSingular(boolean firstCap) {
		return "Broadsword";
	}


	@Override
	protected String weaponGetPlural(boolean firstCap) {
		return "Broadswords";
	}


	@Override
	public String getDescription() {
		return "A heavy military sword, made from " + Material.getMaterial(getMaterial()).getName();
	}


	@Override
	@SuppressWarnings("unchecked")
	protected boolean internalSameAs(Item other) {
		if (other instanceof Broadsword) {
			return getMaterial().equals(((Broadsword<T>) other).getMaterial());
		} else {
			return false;
		}
	}


	@Override
	public void render(Vector2 position, float angle, boolean flipX) {
		TextureRegion texture = getTextureRegion();

		spriteBatch.draw(
			Domain.individualTexture,
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
	public void affect(Individual victim) {
		victim.damage(Util.getRandom().nextFloat() * 5f);
		victim.addCondition(new Bleeding(0.03f));
	}


	@Override
	public boolean canBeCraftedBy(Individual individual) {
		return individual.getSkills().getSmithing() >= 10;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		Map<Item, Integer> map = Maps.newHashMap();

		map.put(Ingot.ingot(getMaterial()), 7);

		return map;
	}


	@Override
	public float getCraftingDuration() {
		return 15f;
	}


	@Override
	public boolean rotates() {
		return true;
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return materialTextureRegionMap.get(getMaterial());
	}


	@Override
	protected Vector2 getRenderCentreOffset() {
		return new Vector2(getTextureRegion().getRegionWidth() * (5f / 7f), getTextureRegion().getRegionHeight() / 2);
	}


	@Override
	protected Item internalCopy() {
		return new Broadsword<T>(getValue(), getMaterial());
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public float getBaseAttackPeriod() {
		return 2f;
	}


	@Override
	public Box getActionFrameHitBox(Individual individual) {
		return new Box(
			new Vector2(
				individual.getHitBox().position.x + (individual.getCurrentAction().flipXAnimation() ? - individual.getHitBox().width * (3f/4f) : individual.getHitBox().width  * (3f/4f)),
				individual.getHitBox().position.y
			),
			individual.getHitBox().width,
			individual.getHitBox().height
		);
	}


	@Override
	public float getBaseDamage() {
		return 0f * Material.getMaterial(getMaterial()).getCombatMultiplier();
	}


	@Override
	public boolean stab() {
		return Util.getRandom().nextBoolean();
	}


	@Override
	public WrapperForTwo<Animation, Vector2> getAttackAnimationEffects(Individual individual) {
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
	public Sound getHitSound() {
		return SoundService.stab;
	}


	@Override
	public float getBlockChance() {
		return 0.35f;
	}


	@Override
	public Sound getBlockSound() {
		return SoundService.broadSwordBlock;
	}


	@Override
	public float getBlockChanceIgnored() {
		return 0.05f;
	}


	@Override
	public float getDisarmChance() {
		return 0.1f;
	}


	@Override
	public String getType() {
		return "One-handed sword";
	}
}