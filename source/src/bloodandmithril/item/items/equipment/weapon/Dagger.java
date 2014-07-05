package bloodandmithril.item.items.equipment.weapon;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;

import java.util.Map;

import bloodandmithril.character.conditions.Bleeding;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.material.Ingot;
import bloodandmithril.item.material.metal.Metal;
import bloodandmithril.util.Util;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.util.datastructure.WrapperForTwo;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Maps;

public class Dagger<T extends Metal> extends OneHandedMeleeWeapon<T> implements Craftable {
	private static final long serialVersionUID = -8932319773500235186L;

	public static TextureRegion iron;

	/**
	 * Constructor
	 */
	private Dagger(long value, Class<T> metal) {
		super(2, true, value, metal);
	}


	/**
	 * @return Static instance getter
	 */
	public static <T extends Metal> Dagger<T> dagger(long value, Class<T> metal) {
		return new Dagger<T>(value, metal);
	}


	@Override
	protected String weaponGetSingular(boolean firstCap) {
		return "Dagger";
	}


	@Override
	protected String weaponGetPlural(boolean firstCap) {
		return "Daggers";
	}


	@Override
	public String getDescription() {
		return "A short, sharp blade made from " + getMaterial().getName();
	}


	@Override
	@SuppressWarnings("unchecked")
	protected boolean internalSameAs(Item other) {
		if (other instanceof Dagger) {
			return getMaterial().equals(((Dagger<T>) other).getMaterial());
		} else {
			return false;
		}
	}


	@Override
	public void render(Vector2 position, float angle, boolean flipX) {
		spriteBatch.draw(
			Domain.individualTexture,
			position.x - (flipX ? iron.getRegionWidth() - 15 : 15),
			position.y - 7,
			flipX ? iron.getRegionWidth() - 15 : 15,
			7,
			iron.getRegionWidth(),
			iron.getRegionHeight(),
			1f,
			1f,
			angle,
			iron.getRegionX(),
			iron.getRegionY(),
			iron.getRegionWidth(),
			iron.getRegionHeight(),
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
		return iron;
	}


	@Override
	protected Vector2 getRenderCentreOffset() {
		return new Vector2(getTextureRegion().getRegionWidth() * (5f / 7f), getTextureRegion().getRegionHeight() / 2);
	}


	@Override
	protected Item internalCopy() {
		return new Dagger<T>(getValue(), getMaterial());
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public float getBaseAttackPeriod() {
		return 1f;
	}


	@Override
	public Box getActionFrameHitBox(Individual individual) {
		return new Box(
			new Vector2(
				individual.getHitBox().position.x + (individual.getCurrentAction().flipXAnimation() ? - individual.getHitBox().width * (2f/3f) : individual.getHitBox().width * (2f/3f)),
				individual.getHitBox().position.y
			),
			individual.getHitBox().width * 2 / 3,
			individual.getHitBox().height
		);
	}


	@Override
	public float getBaseDamage() {
		return 0f;
	}


	@Override
	public boolean stab() {
		return true;
	}


	@Override
	public WrapperForTwo<Animation, Vector2> getAttackAnimationEffects(Individual individual) {
		return null;
	}
}