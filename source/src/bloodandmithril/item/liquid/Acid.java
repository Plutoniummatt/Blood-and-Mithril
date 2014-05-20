package bloodandmithril.item.liquid;

import bloodandmithril.character.Individual;
import bloodandmithril.util.Util.Colors;

import com.badlogic.gdx.graphics.Color;

/**
 * Acid.
 *
 * @author Matt
 */
public class Acid extends Liquid {
	private static final long serialVersionUID = 7506934519654231222L;

	public Acid() {
		super();
	}

	@Override
	public void drink(float amount, Individual affected) {
	}

	
	@Override
	public String getDescription() {
		return "Corrosive liquid.";
	}

	
	@Override
	public Color getColor() {
		return Colors.ACID;
	}
}
