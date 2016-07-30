package bloodandmithril.item.liquid;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.character.IndividualStateService;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.util.Util.Colors;

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
	public void drink(final float amount, final Individual affected) {
		Wiring.injector().getInstance(IndividualStateService.class).increaseThirst(affected, amount);
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