package bloodandmithril.character.individuals;

import bloodandmithril.util.datastructure.Box;

/**
 * An {@link Individual} that is grounded, moves on ground.
 *
 * @author Matt
 */
public abstract class GroundedIndividual extends Individual {

	/**
	 * Constructor
	 */
	protected GroundedIndividual(
			IndividualIdentifier id,
			IndividualState state,
			int factionId,
			float inventoryMassCapacity,
			int maxRings,
			int width,
			int height,
			int safetyHeight,
			Box interactionBox,
			int worldId,
			int maximumConcurrentMeleeAttackers) {
		super(id, state, factionId, inventoryMassCapacity, maxRings, width, height, safetyHeight, interactionBox, worldId, maximumConcurrentMeleeAttackers);
	}


	@Override
	protected void internalUpdate(float delta) {
		// Animations
	}
}