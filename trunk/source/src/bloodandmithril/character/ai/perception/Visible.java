package bloodandmithril.character.ai.perception;

import java.io.Serializable;
import java.util.Collection;

import bloodandmithril.core.Copyright;
import bloodandmithril.world.World;

import com.badlogic.gdx.math.Vector2;

/**
 * Indicates an entity is visible to an {@link Observer}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public interface Visible extends Serializable {

	/**
	 * @return a collection of all visibilty test positions, in world coordinates
	 */
	public Collection<Vector2> getVisibleLocations();

	public default boolean isVisibleTo(Observer observer, World world) {
		return observer.canSee(this, world) && isVisible();
	}

	/**
	 * @return whether this {@link Visible} is actually visible.
	 */
	public boolean isVisible();


	/**
	 * @return whether this {@link Visible} is the same entity as another
	 */
	public boolean sameAs(Visible other);


	public static Visible getVisible(Object object) {
		if (object instanceof Visible) {
			return (Visible) object;
		}

		return null;
	}
}