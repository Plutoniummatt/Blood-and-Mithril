package bloodandmithril.item.items.equipment.misc;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.networking.ClientServerInterface.isClient;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.WorldRenderer;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.item.items.equipment.Equipper.EquipmentSlot;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**
 * {@link Equipable} that goes in the {@link EquipmentSlot#OFFHAND}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public abstract class OffhandEquipment extends Equipable {
	private static final long serialVersionUID = 5112607606681273075L;

	static {
		if (isClient()) {
			Torch.torch = new TextureRegion(WorldRenderer.individualTexture, 0, 851, 43, 7);
			Lantern.lantern = new TextureRegion(WorldRenderer.individualTexture, 43, 851, 11, 25);
		}
	}


	/**
	 * Protected contructor
	 */
	protected OffhandEquipment(float mass, int volume, long value) {
		super(mass, volume, true, value, EquipmentSlot.OFFHAND);
	}



	@Override
	public void render(Vector2 position, float angle, boolean flipX) {
		TextureRegion texture = getTextureRegion();
		Vector2 grip = getGripLocation();

		spriteBatch.draw(
			WorldRenderer.individualTexture,
			position.x - (flipX ? texture.getRegionWidth() - grip.x : grip.x),
			position.y - grip.y,
			flipX ? texture.getRegionWidth() - grip.x : grip.x,
			grip.y,
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


	/**
	 * @return the position of the handle, relative to the bottom left corner of the item
	 */
	public abstract Vector2 getGripLocation();


	@Override
	public int getRenderingIndex(Individual individual) {
		return 1;
	}


	@Override
	public boolean rotates() {
		return true;
	}

	@Override
	public boolean twoHand() {
		return false;
	}

	public float renderAngle() {
		return 0f;
	}

	public float combatAngle() {
		return 0f;
	}

	@Override
	public float getUprightAngle() {
		return 0f;
	}
}