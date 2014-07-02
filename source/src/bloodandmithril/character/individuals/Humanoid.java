package bloodandmithril.character.individuals;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import bloodandmithril.util.ParameterizedTask;
import bloodandmithril.util.SpacialConfiguration;
import bloodandmithril.util.datastructure.Box;

import com.badlogic.gdx.math.Vector2;

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


	@Override
	protected SpacialConfiguration getOneHandedWeaponSpatialConfigration() {
		int frameIndex = getCurrentAnimation().get(0).getKeyFrameIndex(getAnimationTimer());

		switch(getCurrentAction()) {
			case STAND_LEFT:
				return new SpacialConfiguration(new Vector2(0, 40f), 0f, false);
			case STAND_RIGHT:
				return new SpacialConfiguration(new Vector2(0, 40f), 0f, true);
			case WALK_LEFT:
			case WALK_RIGHT:
			case RUN_LEFT:
			case RUN_RIGHT:
			case ATTACK_LEFT_ONE_HANDED_WEAPON:
			case ATTACK_LEFT_ONE_HANDED_WEAPON_STAB:
			case ATTACK_RIGHT_ONE_HANDED_WEAPON:
			case ATTACK_RIGHT_ONE_HANDED_WEAPON_STAB:
				return new SpacialConfiguration(new Vector2(0, 40f), 0f, false);

			default:
				throw new RuntimeException("Unexpected action: " + getCurrentAction());
		}
	}
}