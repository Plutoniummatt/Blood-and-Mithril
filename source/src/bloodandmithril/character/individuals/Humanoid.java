package bloodandmithril.character.individuals;

import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_ONE_HANDED_WEAPON;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_ONE_HANDED_WEAPON_STAB;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_UNARMED;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_ONE_HANDED_WEAPON;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_ONE_HANDED_WEAPON_STAB;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_UNARMED;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import bloodandmithril.audio.SoundService;
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
		Map<Integer, ParameterizedTask<Individual>> attackOneHanded = newHashMap();
		Map<Integer, ParameterizedTask<Individual>> attackOneHandedStab = newHashMap();

		attackUnarmed.put(
			3,
			individual -> {
				individual.attack();
				SoundService.swordSlash.play(
					SoundService.getVolumne(individual.getState().position),
					1f,
					SoundService.getPan(individual.getState().position)
				);
			}
		);

		attackOneHanded.put(
			6,
			individual -> {
				individual.attack();
				SoundService.swordSlash.play(
					SoundService.getVolumne(individual.getState().position),
					1f,
					SoundService.getPan(individual.getState().position)
				);
			}
		);
		
		attackOneHandedStab.put(
			3,
			individual -> {
				individual.attack();
				SoundService.swordSlash.play(
					SoundService.getVolumne(individual.getState().position),
					1f,
					SoundService.getPan(individual.getState().position)
				);
			}
		);

		actionFrames.put(ATTACK_LEFT_UNARMED, attackUnarmed);
		actionFrames.put(ATTACK_RIGHT_UNARMED, attackUnarmed);
		actionFrames.put(ATTACK_LEFT_ONE_HANDED_WEAPON_STAB, attackOneHandedStab);
		actionFrames.put(ATTACK_RIGHT_ONE_HANDED_WEAPON_STAB, attackOneHandedStab);
		actionFrames.put(ATTACK_LEFT_ONE_HANDED_WEAPON, attackOneHanded);
		actionFrames.put(ATTACK_RIGHT_ONE_HANDED_WEAPON, attackOneHanded);
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
		int frameIndex = getCurrentAnimation().get(0).a.getKeyFrameIndex(getAnimationTimer());

		switch(getCurrentAction()) {
			case STAND_LEFT:
				return new SpacialConfiguration(new Vector2(10, 35f), 0f, true);
			case STAND_RIGHT:
				return new SpacialConfiguration(new Vector2(-10, 35f), 0f, false);
			case STAND_LEFT_COMBAT:
				return new SpacialConfiguration(new Vector2(19, 48f), 90f, false);
			case STAND_RIGHT_COMBAT:
				return new SpacialConfiguration(new Vector2(-19, 48f), -90f, true);
			case WALK_LEFT:
				switch (frameIndex) {
					case 0: return new SpacialConfiguration(new Vector2(10, 37f), 0f, true);
					case 1: return new SpacialConfiguration(new Vector2(10, 37f), 0f, true);
					case 2: return new SpacialConfiguration(new Vector2(10, 33f), 0f, true);
					case 3: return new SpacialConfiguration(new Vector2(10, 33f), 0f, true);
					case 4: return new SpacialConfiguration(new Vector2(10, 35f), 0f, true);
					case 5: return new SpacialConfiguration(new Vector2(10, 37f), 0f, true);
					case 6: return new SpacialConfiguration(new Vector2(10, 37f), 0f, true);
					case 7: return new SpacialConfiguration(new Vector2(10, 33f), 0f, true);
					case 8: return new SpacialConfiguration(new Vector2(10, 33f), 0f, true);
					case 9: return new SpacialConfiguration(new Vector2(10, 35f), 0f, true);
				}
			case WALK_RIGHT:
				switch (frameIndex) {
					case 0: return new SpacialConfiguration(new Vector2(-10, 37f), 0f, false);
					case 1: return new SpacialConfiguration(new Vector2(-10, 37f), 0f, false);
					case 2: return new SpacialConfiguration(new Vector2(-10, 33f), 0f, false);
					case 3: return new SpacialConfiguration(new Vector2(-10, 33f), 0f, false);
					case 4: return new SpacialConfiguration(new Vector2(-10, 35f), 0f, false);
					case 5: return new SpacialConfiguration(new Vector2(-10, 37f), 0f, false);
					case 6: return new SpacialConfiguration(new Vector2(-10, 37f), 0f, false);
					case 7: return new SpacialConfiguration(new Vector2(-10, 33f), 0f, false);
					case 8: return new SpacialConfiguration(new Vector2(-10, 33f), 0f, false);
					case 9: return new SpacialConfiguration(new Vector2(-10, 35f), 0f, false);
				}
			case RUN_LEFT:
				switch (frameIndex) {
					case 0: return new SpacialConfiguration(new Vector2(10, 35f), 0f, true);
					case 1: return new SpacialConfiguration(new Vector2(10, 37f), 0f, true);
					case 2: return new SpacialConfiguration(new Vector2(10, 35f), 0f, true);
					case 3: return new SpacialConfiguration(new Vector2(10, 33f), 0f, true);
					case 4: return new SpacialConfiguration(new Vector2(10, 35f), 0f, true);
					case 5: return new SpacialConfiguration(new Vector2(10, 37f), 0f, true);
					case 6: return new SpacialConfiguration(new Vector2(10, 35f), 0f, true);
					case 7: return new SpacialConfiguration(new Vector2(10, 33f), 0f, true);
				}
			case RUN_RIGHT:
				switch (frameIndex) {
					case 0: return new SpacialConfiguration(new Vector2(-10, 35f), 0f, false);
					case 1: return new SpacialConfiguration(new Vector2(-10, 37f), 0f, false);
					case 2: return new SpacialConfiguration(new Vector2(-10, 35f), 0f, false);
					case 3: return new SpacialConfiguration(new Vector2(-10, 33f), 0f, false);
					case 4: return new SpacialConfiguration(new Vector2(-10, 35f), 0f, false);
					case 5: return new SpacialConfiguration(new Vector2(-10, 37f), 0f, false);
					case 6: return new SpacialConfiguration(new Vector2(-10, 35f), 0f, false);
					case 7: return new SpacialConfiguration(new Vector2(-10, 33f), 0f, false);
				}
			case ATTACK_LEFT_ONE_HANDED_WEAPON:
				switch (frameIndex) {
					case 0: return new SpacialConfiguration(new Vector2(19, 48f), 90, false);
					case 1: return new SpacialConfiguration(new Vector2(22, 51f), 80f, false);
					case 2: return new SpacialConfiguration(new Vector2(27, 63f), -180f, true);
					case 3: return new SpacialConfiguration(new Vector2(27, 63f), -180f, true);
					case 4: return new SpacialConfiguration(new Vector2(27, 63f), -180f, true);
					case 5: return new SpacialConfiguration(new Vector2(27, 63f), -180f, true);
					case 6: return new SpacialConfiguration(new Vector2(-27, 44f), 7f, true);
					case 7: return new SpacialConfiguration(new Vector2(-27, 44f), 7f, true);
					case 8: return new SpacialConfiguration(new Vector2(-27, 44f), 7f, true);
					case 9: return new SpacialConfiguration(new Vector2(-27, 44f), 7f, true);
				}
			case ATTACK_RIGHT_ONE_HANDED_WEAPON:
				switch (frameIndex) {
					case 0: return new SpacialConfiguration(new Vector2(-19, 48f), -90f, true);
					case 1: return new SpacialConfiguration(new Vector2(-22, 51f), -80f, true);
					case 2: return new SpacialConfiguration(new Vector2(-27, 63f), 180f, false);
					case 3: return new SpacialConfiguration(new Vector2(-27, 63f), 180f, false);
					case 4: return new SpacialConfiguration(new Vector2(-27, 63f), 180f, false);
					case 5: return new SpacialConfiguration(new Vector2(-27, 63f), 180f, false);
					case 6: return new SpacialConfiguration(new Vector2(27, 44f), -7f, false);
					case 7: return new SpacialConfiguration(new Vector2(27, 44f), -7f, false);
					case 8: return new SpacialConfiguration(new Vector2(27, 44f), -7f, false);
					case 9: return new SpacialConfiguration(new Vector2(27, 44f), -7f, false);
				}
			case ATTACK_LEFT_ONE_HANDED_WEAPON_STAB:
				switch (frameIndex) {
					case 0: return new SpacialConfiguration(new Vector2(17, 45f), 40f, true);
					case 1: return new SpacialConfiguration(new Vector2(18, 46f), 35f, true);
					case 2: return new SpacialConfiguration(new Vector2(18, 46f), 35f, true);
					case 3: return new SpacialConfiguration(new Vector2(18, 46f), 35f, true);
					case 4: return new SpacialConfiguration(new Vector2(-28, 48f), 0f, true);   
					case 5: return new SpacialConfiguration(new Vector2(-28, 48f), 0f, true);   
					case 6: return new SpacialConfiguration(new Vector2(-28, 48f), 0f, true);   
					case 7: return new SpacialConfiguration(new Vector2(-28, 48f), 0f, true);   
				}
			case ATTACK_RIGHT_ONE_HANDED_WEAPON_STAB:
				switch (frameIndex) {
					case 0: return new SpacialConfiguration(new Vector2(-17, 46f), -40f, false);
					case 1: return new SpacialConfiguration(new Vector2(-18, 46f), -35f, false);
					case 2: return new SpacialConfiguration(new Vector2(-18, 46f), -35f, false);
					case 3: return new SpacialConfiguration(new Vector2(-18, 46f), -35f, false);
					case 4: return new SpacialConfiguration(new Vector2(28, 48f), 0f, false);
					case 5: return new SpacialConfiguration(new Vector2(28, 48f), 0f, false);
					case 6: return new SpacialConfiguration(new Vector2(28, 48f), 0f, false);
					case 7: return new SpacialConfiguration(new Vector2(28, 48f), 0f, false);
				}

			default:
				throw new RuntimeException("Unexpected action: " + getCurrentAction());
		}
	}
}