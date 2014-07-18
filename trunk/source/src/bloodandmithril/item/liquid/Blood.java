package bloodandmithril.item.liquid;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.Util.Colors;

import com.badlogic.gdx.graphics.Color;

/**
 * Blood.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Blood extends Liquid {
	private static final long serialVersionUID = -4091849263119830076L;

	public Blood() {
		super();
	}

	@Override
	public void drink(float amount, Individual affected) {
	}


	@Override
	public String getDescription() {
		return "Blood is a bodily fluid in animals that delivers necessary substances such as nutrients and oxygen to the cells and transports metabolic waste products away from those same cells.";
	}


	@Override
	public Color getColor() {
		return Colors.BLOOD;
	}
}
