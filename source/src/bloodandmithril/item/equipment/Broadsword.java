package bloodandmithril.item.equipment;

import java.util.Map;

import bloodandmithril.character.Individual;
import bloodandmithril.character.conditions.Bleeding;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.item.Item;
import bloodandmithril.item.material.metal.SteelIngot;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Maps;

public class Broadsword extends OneHandedWeapon implements Craftable {
	private static final long serialVersionUID = -8932319773500235186L;

	public static TextureRegion texture;

	/**
	 * Constructor
	 */
	public Broadsword(long value) {
		super(2, true, value);
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return "Broad sword";
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return "Broad swords";
	}


	@Override
	public String getDescription() {
		return "Broadswords are heavy military swords, contrasting with rapier, the light sword worn with civilian dress. Since the blade of the rapier had become narrow and thrust-oriented, the heavier blades became known as Broadsword";
	}


	@Override
	protected boolean internalSameAs(Item other) {
		if (other instanceof Broadsword) {
			return true;
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
			417,
			621,
			52,
			11,
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

		map.put(new SteelIngot(), 7);

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
		return new Broadsword(getValue());
	}
}