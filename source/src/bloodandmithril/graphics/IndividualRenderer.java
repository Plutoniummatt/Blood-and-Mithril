package bloodandmithril.graphics;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;

/**
 * Renders {@link Individual}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public interface IndividualRenderer {

	/**
	 * Renders an instance of {@link Individual}
	 */
	public static void render(Individual indi, Graphics graphics) {
		Wiring.injector().getInstance(indi.getClass().getAnnotation(RenderIndividualWith.class).value()).internalRender(indi, graphics);
	}

	public void internalRender(Individual indi, Graphics graphics);
}
