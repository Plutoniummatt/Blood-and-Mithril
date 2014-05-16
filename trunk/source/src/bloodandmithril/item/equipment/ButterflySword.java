package bloodandmithril.item.equipment;

import java.util.Map;

import bloodandmithril.character.Individual;
import bloodandmithril.character.conditions.Bleeding;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.item.Item;
import bloodandmithril.item.material.metal.IronIngot;
import bloodandmithril.item.material.metal.SteelIngot;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Maps;

public class ButterflySword extends OneHandedWeapon implements Craftable {
	private static final long serialVersionUID = -8932319773500235186L;

	public static TextureRegion texture;

	/**
	 * Constructor
	 */
	public ButterflySword(long value) {
		super(1.5f, true, value);
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return "Butterfly sword";
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return "Butterfly swords";
	}


	@Override
	public String getDescription() {
		return "The blade of a butterfly sword is roughly as long as a human forearm, which allows for easy concealment inside loose sleeves or boots, and allows greater maneuverability when spinning and rotating during close-quarters fighting.";
	}


	@Override
	protected boolean internalSameAs(Item other) {
		if (other instanceof ButterflySword) {
			return true;
		} else {
			return false;
		}
	}


	@Override
	public void render(Vector2 position, float angle, boolean flipX) {
		BloodAndMithrilClient.spriteBatch.draw(Domain.individualTexture, position.x - (flipX ? texture.getRegionWidth() - 10 : 10), position.y - 7, flipX ? texture.getRegionWidth() - 10 : 10, 7, texture.getRegionWidth(), texture.getRegionHeight(), 1f, 1f, angle, 419, 587, 47, 12, flipX, false);
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

		map.put(new SteelIngot(), 4);
		map.put(new IronIngot(), 2);

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
		return new ButterflySword(getValue());
	}
}