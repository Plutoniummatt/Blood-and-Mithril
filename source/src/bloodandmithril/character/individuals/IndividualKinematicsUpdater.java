package bloodandmithril.character.individuals;

import com.google.inject.ImplementedBy;

import bloodandmithril.core.Copyright;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * Processes Kinematics for {@link Individual}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@ImplementedBy(ImprovedIndividualKinematicsUpdater.class)
public interface IndividualKinematicsUpdater {

	/**
	 * Updates the individual's kinematics data
	 */
	public void update(final Individual individual, final float delta) throws NoTileFoundException;
}