package bloodandmithril.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.Util;

/**
 * Service for adding {@link FloatingText}s
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class FloatingTextService {

	@Inject private UserInterface userInterface;

	/**
	 * Adds a {@link FloatingText} near an {@link Individual}s position
	 */
	public void addFloatingTextToIndividual(final Individual indi, final String text, final Color color) {
		userInterface.addFloatingText(
			text,
			color,
			indi.getState().position.cpy().add(0f, indi.getHeight()).add(new Vector2(0, 15f).rotate(Util.getRandom().nextFloat() * 360f)),
			false,
			indi.getWorldId()
		);
	}
}