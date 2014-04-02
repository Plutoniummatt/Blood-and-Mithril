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

public class Broadsword extends OneHandedWeapon {
	private static final long serialVersionUID = -8932319773500235186L;

	public static TextureRegion texture;

	/**
	 * Constructor
	 */
	public Broadsword(long value) {
		super(10, true, value);
	}


	@Override
	public String getSingular(boolean firstCap) {
		return "Broad sword";
	}


	@Override
	public String getPlural(boolean firstCap) {
		return "Broad swords";
	}


	@Override
	public Window getInfoWindow() {
		return new MessageWindow(
			"Broadswords are heavy military swords, contrasting with rapier, the light sword worn with civilian dress. Since the blade of the rapier had become narrow and thrust-oriented, the heavier blades became known as Broadsword",
			Color.ORANGE,
			BloodAndMithrilClient.WIDTH/2 - 175,
			BloodAndMithrilClient.HEIGHT/2 + 100,
			350,
			200,
			"Broadsword",
			true,
			100,
			100
		);
	}


	@Override
	public boolean sameAs(Item other) {
		if (other instanceof Broadsword) {
			return true;
		} else {
			return false;
		}
	}


	@Override
	public void render(Vector2 position, float angle, boolean flipX) {
		BloodAndMithrilClient.spriteBatch.draw(
			Domain.individualTexture, position.x - (flipX ? texture.getRegionWidth() - 13 : 13),
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
	public Item combust(int heatLevel) {
		return this;
	}


	@Override
	public void render() {
		
	}
}