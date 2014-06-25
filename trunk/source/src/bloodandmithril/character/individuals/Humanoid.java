package bloodandmithril.character.individuals;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import bloodandmithril.util.ParameterizedTask;
import bloodandmithril.util.datastructure.Box;

/**
 * Uses standard humanoid animations
 *
 * @author Matt
 */
public abstract class Humanoid extends GroundTravellingIndividual {
	private static final long serialVersionUID = 7634760818045237827L;

	private static Map<Action, Map<Integer, ParameterizedTask<Individual>>> actionFrames = newHashMap();

	static {
		Map<Integer, ParameterizedTask<Individual>> attackUnarmed = newHashMap();
		attackUnarmed.put(
			3,
			individual -> {
				individual.attack();
				System.out.println("lol");
			}
		);

		actionFrames.put(Action.ATTACK_LEFT_UNARMED, attackUnarmed);
		actionFrames.put(Action.ATTACK_RIGHT_UNARMED, attackUnarmed);
	}

	/**
	 * Constructor
	 */
	protected Humanoid(
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
	protected Map<Action, Map<Integer, ParameterizedTask<Individual>>> getActionFrames() {
		return actionFrames;
	}
}