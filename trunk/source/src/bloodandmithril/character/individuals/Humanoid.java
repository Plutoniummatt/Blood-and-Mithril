package bloodandmithril.character.individuals;

import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_ONE_HANDED_WEAPON;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_ONE_HANDED_WEAPON_STAB;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_TWO_HANDED_WEAPON_MINE;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_UNARMED;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_ONE_HANDED_WEAPON;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_ONE_HANDED_WEAPON_STAB;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_TWO_HANDED_WEAPON_MINE;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_UNARMED;
import static com.google.common.collect.Iterables.tryFind;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.task.MineTile;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.Weapon;
import bloodandmithril.util.ParameterizedTask;
import bloodandmithril.util.Probablistic;
import bloodandmithril.util.SpacialConfiguration;
import bloodandmithril.util.datastructure.Box;

import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Optional;

/**
 * Uses standard humanoid animations
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class Humanoid extends GroundTravellingIndividual {
	private static final long serialVersionUID = 7634760818045237827L;

	private static Map<Action, Map<Integer, ParameterizedTask<Individual>>> actionFrames = newHashMap();

	public enum HumanoidCombatBodyParts implements Probablistic {
		HEAD(EquipmentSlot.HEAD, 0.1f),
		TORSO(EquipmentSlot.CHEST, 0.4f),
		LEFTARM(EquipmentSlot.LEFTHAND, 0.15f),
		RIGHTARM(EquipmentSlot.RIGHTHAND, 0.15f),
		LEFTLEG(EquipmentSlot.LEGS, 0.1f),
		RIGHTLEG(EquipmentSlot.LEGS, 0.1f);

		private final EquipmentSlot linkedEquipmentSlot;
		private final float chanceToHit;

		private HumanoidCombatBodyParts(EquipmentSlot linkedEquipmentSlot, float chanceToHit) {
			this.linkedEquipmentSlot = linkedEquipmentSlot;
			this.chanceToHit = chanceToHit;
		}

		public EquipmentSlot getLinkedEquipmentSlot() {
			return linkedEquipmentSlot;
		}

		@Override
		public float getProbability() {
			return chanceToHit;
		}
	}

	static {
		Map<Integer, ParameterizedTask<Individual>> attackUnarmed = newHashMap();
		Map<Integer, ParameterizedTask<Individual>> attackOneHanded = newHashMap();
		Map<Integer, ParameterizedTask<Individual>> attackOneHandedStab = newHashMap();
		Map<Integer, ParameterizedTask<Individual>> attackTwoHandedMine = newHashMap();

		attackUnarmed.put(
			3,
			individual -> {
				individual.attack(false);
				SoundService.play(
					SoundService.swordSlash,
					individual.getState().position,
					false
				);
			}
		);

		attackOneHanded.put(
			6,
			individual -> {
				individual.attack(false);
				SoundService.play(
					SoundService.swordSlash,
					individual.getState().position,
					false
				);
			}
		);

		attackOneHandedStab.put(
			3,
			individual -> {
				individual.attack(false);
				SoundService.play(
					SoundService.swordSlash,
					individual.getState().position,
					false
				);
			}
		);

		attackTwoHandedMine.put(
			6,
			individual -> {
				AITask currentTask = individual.getAI().getCurrentTask();
				if (!(currentTask instanceof MineTile)) {
					return;
				}

				MineTile mineTileTask = (MineTile) currentTask;
				MineTile.Mine.mine(individual, mineTileTask.tileCoordinate);
			}
		);

		actionFrames.put(ATTACK_LEFT_UNARMED, attackUnarmed);
		actionFrames.put(ATTACK_RIGHT_UNARMED, attackUnarmed);
		actionFrames.put(ATTACK_LEFT_ONE_HANDED_WEAPON_STAB, attackOneHandedStab);
		actionFrames.put(ATTACK_RIGHT_ONE_HANDED_WEAPON_STAB, attackOneHandedStab);
		actionFrames.put(ATTACK_LEFT_ONE_HANDED_WEAPON, attackOneHanded);
		actionFrames.put(ATTACK_RIGHT_ONE_HANDED_WEAPON, attackOneHanded);
		actionFrames.put(ATTACK_RIGHT_TWO_HANDED_WEAPON_MINE, attackTwoHandedMine);
		actionFrames.put(ATTACK_LEFT_TWO_HANDED_WEAPON_MINE, attackTwoHandedMine);
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
			case ATTACK_LEFT_UNARMED:
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
			case ATTACK_RIGHT_UNARMED:
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


	@Override
	@SuppressWarnings("rawtypes")
	public int getConcurrentAttackNumber() {
		Optional<Item> equippedWeapon = tryFind(getEquipped().keySet(), item -> {
			return item instanceof Weapon;
		});

		if (equippedWeapon.isPresent()) {
			return ((Weapon) equippedWeapon.get()).getAttackNumber(this);
		}

		return 2;
	}
}