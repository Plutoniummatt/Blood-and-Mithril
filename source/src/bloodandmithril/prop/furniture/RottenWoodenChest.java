package bloodandmithril.prop.furniture;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.UpdatedBy;
import bloodandmithril.graphics.RenderPropWith;
import bloodandmithril.item.material.wood.StandardWood;
import bloodandmithril.prop.renderservice.StaticSpritePropRenderingService;
import bloodandmithril.prop.updateservice.NoOpPropUpdateService;

/**
 * Extention of {@link WoodenChestProp}, cant be locked and rotten
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
@UpdatedBy(NoOpPropUpdateService.class)
@RenderPropWith(StaticSpritePropRenderingService.class)
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
	public TextureRegion getTextureRegion() {
		return ROTTEN_WOODEN_CHEST;
	}
}
