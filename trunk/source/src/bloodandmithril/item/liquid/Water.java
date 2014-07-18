package bloodandmithril.item.liquid;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.Util.Colors;

import com.badlogic.gdx.graphics.Color;

/**
 * Water, only quenches thirst
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Water extends Liquid {
	private static final long serialVersionUID = -4641409972631236862L;

	public Water() {
		super();
	}

	@Override
	public void drink(float amount, Individual affected) {
		affected.increaseThirst(amount);
	}

	@Override
	public String getDescription() {
		return "The liquid that keeps you alive.";
	}

	@Override
	public Color getColor() {
		return Colors.WATER;
	}
}