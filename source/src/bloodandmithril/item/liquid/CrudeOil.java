package bloodandmithril.item.liquid;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.util.Util.Colors;

import com.badlogic.gdx.graphics.Color;

/**
 * Crude oil.
 *
 * @author Matt
 */
public class CrudeOil extends Liquid {
	private static final long serialVersionUID = 7506934519654231222L;

	public CrudeOil() {
		super();
	}

	@Override
	public void drink(float amount, Individual affected) {
	}

	
	@Override
	public String getDescription() {
		return "Black gold.";
	}

	
	@Override
	public Color getColor() {
		return Colors.CRUDEOIL;
	}
}
