package bloodandmithril.prop.plant;

import bloodandmithril.core.Copyright;
import bloodandmithril.prop.Growable;
import bloodandmithril.prop.Prop;

/**
 * A Plant {@link Prop}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class PlantProp extends Growable {
	private static final long serialVersionUID = -4865430066854382581L;

	/**
	 * Constructor
	 */
	protected PlantProp(float x, float y, int width, int height) {
		super(x, y, width, height, true);
	}
}