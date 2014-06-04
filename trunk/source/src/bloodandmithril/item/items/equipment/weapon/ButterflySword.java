package bloodandmithril.item.items.equipment.weapon;

import static bloodandmithril.item.items.material.Ingot.ingot;

import java.util.Map;

import bloodandmithril.character.conditions.Bleeding;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.material.metal.Metal;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Maps;

public class ButterflySword<T extends Metal> extends OneHandedWeapon<T> implements Craftable {
	private static final long serialVersionUID = -8932319773500235186L;

	public static TextureRegion texture;

	/**
	 * Constructor
	 */
	public ButterflySword(long value, Class<T> metal) {
		super(1.5f, true, value, metal);
	}


	/**
	 * @return Static instance getter
	 */
	public static <T extends Metal> ButterflySword<T> butterflySword(long value, Class<T> metal) {
		return new ButterflySword<T>(value, metal);
	}


	@Override
	protected String weaponGetSingular(boolean firstCap) {
		return "Butterfly sword";
	}


	@Override
	protected String weaponGetPlural(boolean firstCap) {
		return "Butterfly swords";
	}


	@Override
	public String getDescription() {
		return "The blade of a butterfly sword is roughly as long as a human forearm, which allows for easy concealment inside loose sleeves or boots, and allows greater maneuverability when spinning and rotating during close-quarters fighting.";
	}


	@Override
	@SuppressWarnings("unchecked")
	protected boolean internalSameAs(Item other) {
		if (other instanceof ButterflySword) {
			return getMaterial().equals(((ButterflySword<T>) other).getMaterial());
		} else {
			return false;
		}
	}


	@Override
	public void render(Vector2 position, float angle, boolean flipX) {
		BloodAndMithrilClient.spriteBatch.draw(
			Domain.individualTexture,
			position.x - (flipX ? texture.getRegionWidth() - 10 : 10),
			position.y - 7,
			flipX ? texture.getRegionWidth() - 10 : 10,
			7,
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
		victim.damage(Util.getRandom().nextFloat() * 3f);
		victim.addCondition(new Bleeding(0.06f));
	}


	@Override
	public boolean canBeCraftedBy(Individual individual) {
		return individual.getSkills().getSmithing() >= 15;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		Map<Item, Integer> map = Maps.newHashMap();

		map.put(ingot(getMaterial()), 6);

		return map;
	}


	@Override
	public float getCraftingDuration() {
		return 15f;
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return texture;
	}


	@Override
	public boolean rotates() {
		return true;
	}


	@Override
	protected Vector2 getRenderCentreOffset() {
		return new Vector2(getTextureRegion().getRegionWidth() * (5f / 7f), getTextureRegion().getRegionHeight() / 2);
	}


	@Override
	protected Item internalCopy() {
		return new ButterflySword<T>(getValue(), getMaterial());
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public float getBaseAttackDuration() {
		return 1f;
	}
}