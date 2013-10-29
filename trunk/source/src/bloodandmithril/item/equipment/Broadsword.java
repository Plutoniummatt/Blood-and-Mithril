package bloodandmithril.item.equipment;

import bloodandmithril.Fortress;
import bloodandmithril.item.Item;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.ui.components.window.Window;
import bloodandmithril.world.GameWorld;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class Broadsword extends OneHandedWeapon {
	private static final long serialVersionUID = -8932319773500235186L;

	private static final TextureRegion texture = new TextureRegion(GameWorld.individualTexture, 417, 621, 52, 11);

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
			Fortress.getMouseScreenX(),
			Fortress.getMouseScreenY(),
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
		Fortress.spriteBatch.draw(GameWorld.individualTexture, position.x - (flipX ? texture.getRegionWidth() - 10 : 10), position.y - 7, flipX ? texture.getRegionWidth() - 10 : 10, 7, texture.getRegionWidth(), texture.getRegionHeight(), 1f, 1f, angle, 417, 621, 52, 11, flipX, false);
	}
}