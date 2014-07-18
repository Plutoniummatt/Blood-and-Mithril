package bloodandmithril.item.liquid;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.Util.Colors;

import com.badlogic.gdx.graphics.Color;

/**
 * Milk.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Milk extends Liquid {
	private static final long serialVersionUID = 7506934519654231222L;

	public Milk() {
		super();
	}

	@Override
	public void drink(float amount, Individual affected) {
	}


	@Override
	public String getDescription() {
		return "Cows milk.";
	}


	@Override
	public Color getColor() {
		return Colors.MILK;
	}
}
