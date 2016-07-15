package bloodandmithril.prop.furniture;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.item.material.wood.StandardWood;

/**
 * Extention of {@link WoodenChestProp}, cant be locked and rotten
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class RottenWoodenChest extends WoodenChestProp {
	private static final long serialVersionUID = -8577332391703143295L;

	/** {@link TextureRegion} of the {@link WoodenChestProp} */
	public static TextureRegion ROTTEN_WOODEN_CHEST;

	/**
	 * Constructor
	 */
	public RottenWoodenChest(float x, float y, float capacity, int volume) {
		super(x, y, capacity, volume, StandardWood.class);
	}


	@Override
	public void render(Graphics graphics) {
		graphics.getSpriteBatch().draw(ROTTEN_WOODEN_CHEST, position.x - width / 2, position.y);
	}
}
