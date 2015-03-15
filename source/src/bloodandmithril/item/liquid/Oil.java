package bloodandmithril.item.liquid;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.Util.Colors;

import com.badlogic.gdx.graphics.Color;

/**
 * Crude oil.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Oil extends Liquid {
	private static final long serialVersionUID = 7506934519654231222L;

	public Oil() {
		super();
	}

	@Override
	public void drink(float amount, Individual affected) {
	}


	@Override
	public String getDescription() {
		return "Oil, can be used as a fuel.";
	}


	@Override
	public Color getColor() {
		return Colors.OIL;
	}
}
