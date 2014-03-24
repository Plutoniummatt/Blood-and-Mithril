package bloodandmithril.item.equipment;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.character.Individual;
import bloodandmithril.character.conditions.Bleeding;
import bloodandmithril.item.Item;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.ui.components.window.Window;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class ButterflySword extends OneHandedWeapon {
	private static final long serialVersionUID = -8932319773500235186L;

	public static TextureRegion texture;

	/**
	 * Constructor
	 */
	public ButterflySword(long value) {
		super(10, true, value);
	}


	@Override
	public String getSingular(boolean firstCap) {
		return "Butterfly sword";
	}


	@Override
	public String getPlural(boolean firstCap) {
		return "Butterfly swords";
	}


	@Override
	public Window getInfoWindow() {
		return new MessageWindow(
			"The blade of a butterfly sword is roughly as long as a human forearm, which allows for easy concealment inside loose sleeves or boots, and allows greater maneuverability when spinning and rotating during close-quarters fighting.",
			Color.ORANGE,
			BloodAndMithrilClient.WIDTH/2 - 175,
			BloodAndMithrilClient.HEIGHT/2 + 100,
			350,
			200,
			"Butterfly Sword",
			true,
			100,
			100
		);
	}


	@Override
	public boolean sameAs(Item other) {
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
	public Item combust(int heatLevel) {
		return this;
	}
}