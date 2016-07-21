package bloodandmithril.character.individuals;

import com.google.inject.ImplementedBy;

import bloodandmithril.core.Copyright;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * Processes Kinematics for {@link Individual}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@ImplementedBy(LegacyIndividualKinematicsUpdater.class)
public interface IndividualKinematicsUpdater {

	/**
	 * Updates the individual's kinematics data
	 */
	public void update(final float delta, final World world, final Individual individual) throws NoTileFoundException;
}