package bloodandmithril.prop.furniture;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.material.wood.StandardWood;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Extention of {@link WoodenChest}, cant be locked and rotten
 * 
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class RottenWoodenChest extends WoodenChest {
	private static final long serialVersionUID = -8577332391703143295L;

	/** {@link TextureRegion} of the {@link WoodenChest} */
	public static TextureRegion rottenWoodenChest;
	
	/**
	 * Constructor
	 */
	public RottenWoodenChest(float x, float y, float capacity, int volume) {
		super(x, y, capacity, volume, StandardWood.class);
	}
	
	
	@Override
	public void render() {
		spriteBatch.draw(rottenWoodenChest, position.x - width / 2, position.y);
	}
}
