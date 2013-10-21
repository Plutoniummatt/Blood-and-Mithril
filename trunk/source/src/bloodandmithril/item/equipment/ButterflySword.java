package bloodandmithril.item.equipment;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import bloodandmithril.Fortress;
import bloodandmithril.item.Item;
import bloodandmithril.ui.components.window.Window;
import bloodandmithril.world.GameWorld;

public class ButterflySword extends OneHandedWeapon {
	private static final long serialVersionUID = -8932319773500235186L;

	private static final TextureRegion texture = new TextureRegion(GameWorld.individualTexture, 419, 587, 47, 12);
	
	/**
	 * Constructor
	 */
	public ButterflySword(float mass, boolean equippable, long value) {
		super(mass, equippable, value);
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
		return null;
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
		Fortress.spriteBatch.draw(GameWorld.individualTexture, position.x - (flipX ? texture.getRegionWidth() - 10 : 10), position.y - 7, flipX ? texture.getRegionWidth() - 10 : 10, 7, texture.getRegionWidth(), texture.getRegionHeight(), 1f, 1f, angle, 419, 587, 47, 12, flipX, false); 
	}
}