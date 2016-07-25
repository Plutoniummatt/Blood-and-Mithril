package bloodandmithril.prop.plant;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.UpdatedBy;
import bloodandmithril.graphics.RenderPropWith;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.prop.Growable;
import bloodandmithril.prop.Prop;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.world.topography.tile.Tile;

/**
 * A Plant {@link Prop}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@UpdatedBy()
@RenderPropWith()
public abstract class PlantProp extends Growable {
	private static final long serialVersionUID = -4865430066854382581L;

	/**
	 * Constructor
	 */
	protected PlantProp(float x, float y, int width, int height, Depth depth, SerializableMappingFunction<Tile, Boolean> canPlaceOnTopOf, boolean preventsMining) {
		super(x, y, width, height, true, depth, canPlaceOnTopOf, preventsMining);
	}
	
	
	@Override
	public Color getContextMenuColor() {
		return Color.GREEN;
	}
}