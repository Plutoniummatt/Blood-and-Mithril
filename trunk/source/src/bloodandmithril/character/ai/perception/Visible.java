package bloodandmithril.character.ai.perception;

import java.util.Collection;

import bloodandmithril.core.Copyright;

import com.badlogic.gdx.math.Vector2;

/**
 * Indicates an entity is visible to an {@link Observer}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public interface Visible {

	/**
	 * @return a collection of all visibilty test positions, in world coordinates
	 */
	public Collection<Vector2> getVisibleLocation();
}