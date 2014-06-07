package bloodandmithril.character.individuals;

import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_ONE_HANDED_WEAPON;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import bloodandmithril.util.Task;
import bloodandmithril.util.datastructure.Box;

/**
 * Uses standard humanoid animations
 *
 * @author Matt
 */
public abstract class Humanoid extends GroundTravellingIndividual {
	private static final long serialVersionUID = 7634760818045237827L;

	private static Map<Action, Map<Integer, Task>> actionFrames = newHashMap();

	static {
		Map<Integer, Task> attackRightOneHandedWeapon = newHashMap();
		attackRightOneHandedWeapon.put(0, () -> {
			System.out.println("HYAH!");
		});
		actionFrames.put(ATTACK_RIGHT_ONE_HANDED_WEAPON, attackRightOneHandedWeapon);
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
	protected Map<Action, Map<Integer, Task>> getActionFrames() {
		return actionFrames;
	}
}