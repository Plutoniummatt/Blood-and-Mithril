package bloodandmithril.item.items.equipment.weapon;

import java.util.Map;

import bloodandmithril.character.Individual;
import bloodandmithril.character.conditions.Bleeding;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.material.Ingot;
import bloodandmithril.item.material.metal.Metal;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Maps;

public class Broadsword<T extends Metal> extends OneHandedWeapon<T> implements Craftable {
	private static final long serialVersionUID = -8932319773500235186L;

	public static TextureRegion texture;

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
		return "Broad sword";
	}


	@Override
	protected String weaponGetPlural(boolean firstCap) {
		return "Broad swords";
	}


	@Override
	public String getDescription() {
		return "Broadswords are heavy military swords, this one is made from " + getMaterial().getName();
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
		BloodAndMithrilClient.spriteBatch.draw(
			Domain.individualTexture,
			position.x - (flipX ? texture.getRegionWidth() - 13 : 13),
			position.y - 7,
			flipX ? texture.getRegionWidth() - 13 : 13,
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
		return texture;
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
}