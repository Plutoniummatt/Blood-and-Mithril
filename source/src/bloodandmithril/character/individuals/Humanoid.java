package bloodandmithril.character.individuals;

import static bloodandmithril.character.individuals.Action.ATTACK_LEFT_ONE_HANDED_WEAPON;
import static bloodandmithril.character.individuals.Action.ATTACK_LEFT_ONE_HANDED_WEAPON_MINE;
import static bloodandmithril.character.individuals.Action.ATTACK_LEFT_ONE_HANDED_WEAPON_STAB;
import static bloodandmithril.character.individuals.Action.ATTACK_LEFT_UNARMED;
import static bloodandmithril.character.individuals.Action.ATTACK_RIGHT_ONE_HANDED_WEAPON;
import static bloodandmithril.character.individuals.Action.ATTACK_RIGHT_ONE_HANDED_WEAPON_MINE;
import static bloodandmithril.character.individuals.Action.ATTACK_RIGHT_ONE_HANDED_WEAPON_STAB;
import static bloodandmithril.character.individuals.Action.ATTACK_RIGHT_UNARMED;
import static com.google.common.collect.Iterables.tryFind;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.perception.Visible;
import bloodandmithril.character.ai.task.minetile.MineTile;
import bloodandmithril.character.combat.CombatService;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.core.Wiring;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.offhand.OffhandEquipment;
import bloodandmithril.item.items.equipment.weapon.Weapon;
import bloodandmithril.util.ParameterizedTask;
import bloodandmithril.util.Probablistic;
import bloodandmithril.util.SpacialConfiguration;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.world.topography.MineTileService;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * Uses standard humanoid animations
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@Name(name = "Humanoid")
public abstract class Humanoid extends GroundTravellingIndividual {
	private static final long serialVersionUID = 7634760818045237827L;

	private static Map<Action, Map<Integer, ParameterizedTask<Individual>>> actionFrames = newHashMap();

	public enum HumanoidCombatBodyParts implements Probablistic {
		HEAD(EquipmentSlot.HEAD, 0.1f),
		TORSO(EquipmentSlot.CHEST, 0.4f),
		LEFTARM(EquipmentSlot.OFFHAND, 0.15f),
		RIGHTARM(EquipmentSlot.MAINHAND, 0.15f),
		LEFTLEG(EquipmentSlot.LEGS, 0.1f),
		RIGHTLEG(EquipmentSlot.LEGS, 0.1f);

		private final EquipmentSlot linkedEquipmentSlot;
		private final float chanceToHit;

		private HumanoidCombatBodyParts(final EquipmentSlot linkedEquipmentSlot, final float chanceToHit) {
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
		final Map<Integer, ParameterizedTask<Individual>> attackUnarmed = newHashMap();
		final Map<Integer, ParameterizedTask<Individual>> attackOneHanded = newHashMap();
		final Map<Integer, ParameterizedTask<Individual>> attackOneHandedStab = newHashMap();
		final Map<Integer, ParameterizedTask<Individual>> attackTwoHandedMine = newHashMap();

		attackUnarmed.put(
			3,
			individual -> {
				CombatService.strike(individual);
				SoundService.play(
					SoundService.swordSlash,
					individual.getState().position,
					false,
					Visible.getVisible(individual)
				);
			}
		);

		attackOneHanded.put(
			6,
			individual -> {
				CombatService.strike(individual);
				SoundService.play(
					SoundService.swordSlash,
					individual.getState().position,
					false,
					Visible.getVisible(individual)
				);
			}
		);

		attackOneHandedStab.put(
			3,
			individual -> {
				CombatService.strike(individual);
				SoundService.play(
					SoundService.swordSlash,
					individual.getState().position,
					false,
					Visible.getVisible(individual)
				);
			}
		);

		attackTwoHandedMine.put(
			6,
			individual -> {
				final AITask currentTask = individual.getAI().getCurrentTask();
				if (!(currentTask instanceof MineTile)) {
					return;
				}

				final MineTile mineTileTask = (MineTile) currentTask;
				try {
					Wiring.injector().getInstance(MineTileService.class).mine(individual, Topography.convertToWorldCoord(mineTileTask.tileCoordinate, false));
				} catch (final NoTileFoundException e) {}
			}
		);

		actionFrames.put(ATTACK_LEFT_UNARMED, attackUnarmed);
		actionFrames.put(ATTACK_RIGHT_UNARMED, attackUnarmed);
		actionFrames.put(ATTACK_LEFT_ONE_HANDED_WEAPON_STAB, attackOneHandedStab);
		actionFrames.put(ATTACK_RIGHT_ONE_HANDED_WEAPON_STAB, attackOneHandedStab);
		actionFrames.put(ATTACK_LEFT_ONE_HANDED_WEAPON, attackOneHanded);
		actionFrames.put(ATTACK_RIGHT_ONE_HANDED_WEAPON, attackOneHanded);
		actionFrames.put(ATTACK_RIGHT_ONE_HANDED_WEAPON_MINE, attackTwoHandedMine);
		actionFrames.put(ATTACK_LEFT_ONE_HANDED_WEAPON_MINE, attackTwoHandedMine);
	}

	/**
	 * Constructor
	 */
	protected Humanoid(
			final IndividualIdentifier id,
			final IndividualState state,
			final int factionId,
			final Behaviour naturalBehaviour,
			final float inventoryMassCapacity,
			final int inventoryVolumeCapacity,
			final int maxRings,
			final int width,
			final int height,
			final int safetyHeight,
			final Box interactionBox,
			final int worldId,
			final int maximumConcurrentMeleeAttackers) {
		super(id, state, factionId, naturalBehaviour, inventoryMassCapacity, inventoryVolumeCapacity, maxRings, width, height, safetyHeight, interactionBox, worldId, maximumConcurrentMeleeAttackers);
	}


	@Override
	protected Map<Action, Map<Integer, ParameterizedTask<Individual>>> getActionFrames() {
		return actionFrames;
	}


	public SpacialConfiguration getHelmetSpatialConfigration() {
		final int frameIndex = getCurrentAnimation().get(0).a.getAnimation(this).getKeyFrameIndex(getAnimationTimer());

		switch(getCurrentAction()) {
			case STAND_LEFT:
				return new SpacialConfiguration(new Vector2(0, -2f), 0f, true);
			case JUMP_LEFT:
				return new SpacialConfiguration(new Vector2(0, -4f), 0f, true);
			case STAND_RIGHT:
				return new SpacialConfiguration(new Vector2(0, -2f), 0f, false);
			case JUMP_RIGHT:
				return new SpacialConfiguration(new Vector2(0, -4f), 0f, false);
			case STAND_LEFT_COMBAT_ONE_HANDED:
			case ATTACK_LEFT_ONE_HANDED_WEAPON_MINE:
			case ATTACK_LEFT_ONE_HANDED_WEAPON:
			case ATTACK_LEFT_ONE_HANDED_WEAPON_STAB:
			case ATTACK_LEFT_UNARMED:
				return new SpacialConfiguration(new Vector2(0, -4f), 0f, true);
			case STAND_RIGHT_COMBAT_ONE_HANDED:
			case ATTACK_RIGHT_ONE_HANDED_WEAPON_MINE:
			case ATTACK_RIGHT_ONE_HANDED_WEAPON:
			case ATTACK_RIGHT_ONE_HANDED_WEAPON_STAB:
			case ATTACK_RIGHT_UNARMED:
				return new SpacialConfiguration(new Vector2(0, -4f), 0f, false);
			case WALK_LEFT:
				switch (frameIndex) {
					case 0: return new SpacialConfiguration(new Vector2(0, 0f), 0f, true);
					case 1: return new SpacialConfiguration(new Vector2(0, 0f), 0f, true);
					case 2: return new SpacialConfiguration(new Vector2(0, -6f), 0f, true);
					case 3: return new SpacialConfiguration(new Vector2(0, -4f), 0f, true);
					case 4: return new SpacialConfiguration(new Vector2(0, -2f), 0f, true);
					case 5: return new SpacialConfiguration(new Vector2(0, 0f), 0f, true);
					case 6: return new SpacialConfiguration(new Vector2(0, 0f), 0f, true);
					case 7: return new SpacialConfiguration(new Vector2(0, -6f), 0f, true);
					case 8: return new SpacialConfiguration(new Vector2(0, -4f), 0f, true);
					case 9: return new SpacialConfiguration(new Vector2(0, -2f), 0f, true);
				}
			case WALK_RIGHT:
				switch (frameIndex) {
					case 0: return new SpacialConfiguration(new Vector2(0, 0f), 0f, false);
					case 1: return new SpacialConfiguration(new Vector2(0, 0f), 0f, false);
					case 2: return new SpacialConfiguration(new Vector2(0, -6f), 0f, false);
					case 3: return new SpacialConfiguration(new Vector2(0, -4f), 0f, false);
					case 4: return new SpacialConfiguration(new Vector2(0, -2f), 0f, false);
					case 5: return new SpacialConfiguration(new Vector2(0, 0f), 0f, false);
					case 6: return new SpacialConfiguration(new Vector2(0, 0f), 0f, false);
					case 7: return new SpacialConfiguration(new Vector2(0, -6f), 0f, false);
					case 8: return new SpacialConfiguration(new Vector2(0, -4f), 0f, false);
					case 9: return new SpacialConfiguration(new Vector2(0, -2f), 0f, false);
				}
			case RUN_LEFT:
				switch (frameIndex) {
					case 0: return new SpacialConfiguration(new Vector2(0, -2f), 0f, true);
					case 1: return new SpacialConfiguration(new Vector2(0, 0f), 0f, true);
					case 2: return new SpacialConfiguration(new Vector2(0, -4f), 0f, true);
					case 3: return new SpacialConfiguration(new Vector2(0, -6f), 0f, true);
					case 4: return new SpacialConfiguration(new Vector2(0, -2f), 0f, true);
					case 5: return new SpacialConfiguration(new Vector2(0, 0f), 0f, true);
					case 6: return new SpacialConfiguration(new Vector2(0, -4f), 0f, true);
					case 7: return new SpacialConfiguration(new Vector2(0, -6f), 0f, true);
				}
			case RUN_RIGHT:
				switch (frameIndex) {
					case 0: return new SpacialConfiguration(new Vector2(0, -2f), 0f, false);
					case 1: return new SpacialConfiguration(new Vector2(0, 0f), 0f, false);
					case 2: return new SpacialConfiguration(new Vector2(0, -4f), 0f, false);
					case 3: return new SpacialConfiguration(new Vector2(0, -6f), 0f, false);
					case 4: return new SpacialConfiguration(new Vector2(0, -2f), 0f, false);
					case 5: return new SpacialConfiguration(new Vector2(0, 0f), 0f, false);
					case 6: return new SpacialConfiguration(new Vector2(0, -4f), 0f, false);
					case 7: return new SpacialConfiguration(new Vector2(0, -6f), 0f, false);
				}

			case DEAD:
				return new SpacialConfiguration(new Vector2(0, 0f), 0f, false);

			default:
				throw new RuntimeException("Unexpected action: " + getCurrentAction());
		}
	}


	@Override
	public SpacialConfiguration getOneHandedWeaponSpatialConfigration() {
		final int frameIndex = getCurrentAnimation().get(0).a.getAnimation(this).getKeyFrameIndex(getAnimationTimer());

		switch(getCurrentAction()) {
			case STAND_LEFT:
			case JUMP_LEFT:
				return new SpacialConfiguration(new Vector2(12, 33f), 0f, true);
			case STAND_RIGHT:
			case JUMP_RIGHT:
				return new SpacialConfiguration(new Vector2(-12, 33f), 0f, false);
			case STAND_LEFT_COMBAT_ONE_HANDED:
				return new SpacialConfiguration(new Vector2(19, 48f), 90f, false);
			case STAND_RIGHT_COMBAT_ONE_HANDED:
				return new SpacialConfiguration(new Vector2(-19, 48f), -90f, true);
			case WALK_LEFT:
				switch (frameIndex) {
					case 0: return new SpacialConfiguration(new Vector2(12, 35f), 0f, true);
					case 1: return new SpacialConfiguration(new Vector2(12, 35f), 0f, true);
					case 2: return new SpacialConfiguration(new Vector2(12, 31f), 0f, true);
					case 3: return new SpacialConfiguration(new Vector2(12, 31f), 0f, true);
					case 4: return new SpacialConfiguration(new Vector2(12, 33f), 0f, true);
					case 5: return new SpacialConfiguration(new Vector2(12, 35f), 0f, true);
					case 6: return new SpacialConfiguration(new Vector2(12, 35f), 0f, true);
					case 7: return new SpacialConfiguration(new Vector2(12, 31f), 0f, true);
					case 8: return new SpacialConfiguration(new Vector2(12, 31f), 0f, true);
					case 9: return new SpacialConfiguration(new Vector2(12, 33f), 0f, true);
				}
			case WALK_RIGHT:
				switch (frameIndex) {
					case 0: return new SpacialConfiguration(new Vector2(-12, 35f), 0f, false);
					case 1: return new SpacialConfiguration(new Vector2(-12, 35f), 0f, false);
					case 2: return new SpacialConfiguration(new Vector2(-12, 31f), 0f, false);
					case 3: return new SpacialConfiguration(new Vector2(-12, 31f), 0f, false);
					case 4: return new SpacialConfiguration(new Vector2(-12, 33f), 0f, false);
					case 5: return new SpacialConfiguration(new Vector2(-12, 35f), 0f, false);
					case 6: return new SpacialConfiguration(new Vector2(-12, 35f), 0f, false);
					case 7: return new SpacialConfiguration(new Vector2(-12, 31f), 0f, false);
					case 8: return new SpacialConfiguration(new Vector2(-12, 31f), 0f, false);
					case 9: return new SpacialConfiguration(new Vector2(-12, 33f), 0f, false);
				}
			case RUN_LEFT:
				switch (frameIndex) {
					case 0: return new SpacialConfiguration(new Vector2(12, 33f), 0f, true);
					case 1: return new SpacialConfiguration(new Vector2(12, 35f), 0f, true);
					case 2: return new SpacialConfiguration(new Vector2(12, 33f), 0f, true);
					case 3: return new SpacialConfiguration(new Vector2(12, 31f), 0f, true);
					case 4: return new SpacialConfiguration(new Vector2(12, 33f), 0f, true);
					case 5: return new SpacialConfiguration(new Vector2(12, 35f), 0f, true);
					case 6: return new SpacialConfiguration(new Vector2(12, 33f), 0f, true);
					case 7: return new SpacialConfiguration(new Vector2(12, 31f), 0f, true);
				}
			case RUN_RIGHT:
				switch (frameIndex) {
					case 0: return new SpacialConfiguration(new Vector2(-12, 33f), 0f, false);
					case 1: return new SpacialConfiguration(new Vector2(-12, 35f), 0f, false);
					case 2: return new SpacialConfiguration(new Vector2(-12, 33f), 0f, false);
					case 3: return new SpacialConfiguration(new Vector2(-12, 31f), 0f, false);
					case 4: return new SpacialConfiguration(new Vector2(-12, 33f), 0f, false);
					case 5: return new SpacialConfiguration(new Vector2(-12, 35f), 0f, false);
					case 6: return new SpacialConfiguration(new Vector2(-12, 33f), 0f, false);
					case 7: return new SpacialConfiguration(new Vector2(-12, 31f), 0f, false);
				}
			case ATTACK_LEFT_ONE_HANDED_WEAPON_MINE:
			case ATTACK_LEFT_ONE_HANDED_WEAPON:
				switch (frameIndex) {
					case 0: return new SpacialConfiguration(new Vector2(19, 48f), 90, false);
					case 1: return new SpacialConfiguration(new Vector2(22, 51f), 80f, false);
					case 2: return new SpacialConfiguration(new Vector2(27, 63f), -180f, true);
					case 3: return new SpacialConfiguration(new Vector2(27, 63f), -180f, true);
					case 4: return new SpacialConfiguration(new Vector2(27, 63f), -180f, true);
					case 5: return new SpacialConfiguration(new Vector2(27, 63f), -180f, true);
					case 6: return new SpacialConfiguration(new Vector2(-24, 44f), 7f, true);
					case 7: return new SpacialConfiguration(new Vector2(-24, 44f), 7f, true);
					case 8: return new SpacialConfiguration(new Vector2(-24, 44f), 7f, true);
					case 9: return new SpacialConfiguration(new Vector2(-24, 44f), 7f, true);
				}
			case ATTACK_RIGHT_ONE_HANDED_WEAPON_MINE:
			case ATTACK_RIGHT_ONE_HANDED_WEAPON:
				switch (frameIndex) {
					case 0: return new SpacialConfiguration(new Vector2(-19, 48f), -90f, true);
					case 1: return new SpacialConfiguration(new Vector2(-22, 51f), -80f, true);
					case 2: return new SpacialConfiguration(new Vector2(-27, 63f), 180f, false);
					case 3: return new SpacialConfiguration(new Vector2(-27, 63f), 180f, false);
					case 4: return new SpacialConfiguration(new Vector2(-27, 63f), 180f, false);
					case 5: return new SpacialConfiguration(new Vector2(-27, 63f), 180f, false);
					case 6: return new SpacialConfiguration(new Vector2(24, 44f), -7f, false);
					case 7: return new SpacialConfiguration(new Vector2(24, 44f), -7f, false);
					case 8: return new SpacialConfiguration(new Vector2(24, 44f), -7f, false);
					case 9: return new SpacialConfiguration(new Vector2(24, 44f), -7f, false);
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

			case DEAD:
				return new SpacialConfiguration(new Vector2(0, 0f), 0f, false);

			default:
				throw new RuntimeException("Unexpected action: " + getCurrentAction());
		}
	}


	@Override
	public SpacialConfiguration getTwoHandedWeaponSpatialConfigration() {
		final int frameIndex = getCurrentAnimation().get(0).a.getAnimation(this).getKeyFrameIndex(getAnimationTimer());

		switch(getCurrentAction()) {
			case STAND_LEFT:
			case JUMP_LEFT:
				return new SpacialConfiguration(new Vector2(12, 33f), 0f, true);
			case STAND_RIGHT:
			case JUMP_RIGHT:
				return new SpacialConfiguration(new Vector2(-12, 33f), 0f, false);
			case STAND_LEFT_COMBAT_ONE_HANDED:
				return new SpacialConfiguration(new Vector2(19, 48f), 90f, false);
			case STAND_RIGHT_COMBAT_ONE_HANDED:
				return new SpacialConfiguration(new Vector2(-19, 48f), -90f, true);
			case WALK_LEFT:
				switch (frameIndex) {
					case 0: return new SpacialConfiguration(new Vector2(12, 35f), 0f, true);
					case 1: return new SpacialConfiguration(new Vector2(12, 35f), 0f, true);
					case 2: return new SpacialConfiguration(new Vector2(12, 31f), 0f, true);
					case 3: return new SpacialConfiguration(new Vector2(12, 31f), 0f, true);
					case 4: return new SpacialConfiguration(new Vector2(12, 33f), 0f, true);
					case 5: return new SpacialConfiguration(new Vector2(12, 35f), 0f, true);
					case 6: return new SpacialConfiguration(new Vector2(12, 35f), 0f, true);
					case 7: return new SpacialConfiguration(new Vector2(12, 31f), 0f, true);
					case 8: return new SpacialConfiguration(new Vector2(12, 31f), 0f, true);
					case 9: return new SpacialConfiguration(new Vector2(12, 33f), 0f, true);
				}
			case WALK_RIGHT:
				switch (frameIndex) {
					case 0: return new SpacialConfiguration(new Vector2(-12, 35f), 0f, false);
					case 1: return new SpacialConfiguration(new Vector2(-12, 35f), 0f, false);
					case 2: return new SpacialConfiguration(new Vector2(-12, 31f), 0f, false);
					case 3: return new SpacialConfiguration(new Vector2(-12, 31f), 0f, false);
					case 4: return new SpacialConfiguration(new Vector2(-12, 33f), 0f, false);
					case 5: return new SpacialConfiguration(new Vector2(-12, 35f), 0f, false);
					case 6: return new SpacialConfiguration(new Vector2(-12, 35f), 0f, false);
					case 7: return new SpacialConfiguration(new Vector2(-12, 31f), 0f, false);
					case 8: return new SpacialConfiguration(new Vector2(-12, 31f), 0f, false);
					case 9: return new SpacialConfiguration(new Vector2(-12, 33f), 0f, false);
				}
			case RUN_LEFT:
				switch (frameIndex) {
					case 0: return new SpacialConfiguration(new Vector2(12, 33f), 0f, true);
					case 1: return new SpacialConfiguration(new Vector2(12, 35f), 0f, true);
					case 2: return new SpacialConfiguration(new Vector2(12, 33f), 0f, true);
					case 3: return new SpacialConfiguration(new Vector2(12, 31f), 0f, true);
					case 4: return new SpacialConfiguration(new Vector2(12, 33f), 0f, true);
					case 5: return new SpacialConfiguration(new Vector2(12, 35f), 0f, true);
					case 6: return new SpacialConfiguration(new Vector2(12, 33f), 0f, true);
					case 7: return new SpacialConfiguration(new Vector2(12, 31f), 0f, true);
				}
			case RUN_RIGHT:
				switch (frameIndex) {
					case 0: return new SpacialConfiguration(new Vector2(-12, 33f), 0f, false);
					case 1: return new SpacialConfiguration(new Vector2(-12, 35f), 0f, false);
					case 2: return new SpacialConfiguration(new Vector2(-12, 33f), 0f, false);
					case 3: return new SpacialConfiguration(new Vector2(-12, 31f), 0f, false);
					case 4: return new SpacialConfiguration(new Vector2(-12, 33f), 0f, false);
					case 5: return new SpacialConfiguration(new Vector2(-12, 35f), 0f, false);
					case 6: return new SpacialConfiguration(new Vector2(-12, 33f), 0f, false);
					case 7: return new SpacialConfiguration(new Vector2(-12, 31f), 0f, false);
				}

			default:
				throw new RuntimeException("Unexpected action: " + getCurrentAction());
		}
	}


	@Override
	public SpacialConfiguration getOffHandSpatialConfigration() {
		final int frameIndex = getCurrentAnimation().get(0).a.getAnimation(this).getKeyFrameIndex(getAnimationTimer());

		float angle = 0f;
		float combatAngle = 0f;
		if (!getAvailableEquipmentSlots().get(EquipmentSlot.OFFHAND).call()) {
			final Item offHandItem = Iterables.find(getEquipped().keySet(), offHand -> {
				return offHand instanceof OffhandEquipment;
			});
			angle = ((OffhandEquipment) offHandItem).renderAngle();
			combatAngle = ((OffhandEquipment) offHandItem).combatAngle();
		}

		switch(getCurrentAction()) {
			case STAND_LEFT:
			case JUMP_LEFT:
				return new SpacialConfiguration(new Vector2(-18, 37), -angle, true);
			case STAND_RIGHT:
			case JUMP_RIGHT:
				return new SpacialConfiguration(new Vector2(18, 37), angle, false);
			case WALK_LEFT:
				switch (frameIndex) {
					case 0: return new SpacialConfiguration(new Vector2(-18, 39f), -angle, true);
					case 1: return new SpacialConfiguration(new Vector2(-18, 39f), -angle, true);
					case 2: return new SpacialConfiguration(new Vector2(-18, 35f), -angle, true);
					case 3: return new SpacialConfiguration(new Vector2(-18, 35f), -angle, true);
					case 4: return new SpacialConfiguration(new Vector2(-18, 37f), -angle, true);
					case 5: return new SpacialConfiguration(new Vector2(-18, 39f), -angle, true);
					case 6: return new SpacialConfiguration(new Vector2(-18, 39f), -angle, true);
					case 7: return new SpacialConfiguration(new Vector2(-18, 35f), -angle, true);
					case 8: return new SpacialConfiguration(new Vector2(-18, 35f), -angle, true);
					case 9: return new SpacialConfiguration(new Vector2(-18, 37f), -angle, true);
				}
			case WALK_RIGHT:
				switch (frameIndex) {
					case 0: return new SpacialConfiguration(new Vector2(18, 39f), angle, false);
					case 1: return new SpacialConfiguration(new Vector2(18, 39f), angle, false);
					case 2: return new SpacialConfiguration(new Vector2(18, 35f), angle, false);
					case 3: return new SpacialConfiguration(new Vector2(18, 35f), angle, false);
					case 4: return new SpacialConfiguration(new Vector2(18, 37f), angle, false);
					case 5: return new SpacialConfiguration(new Vector2(18, 39f), angle, false);
					case 6: return new SpacialConfiguration(new Vector2(18, 39f), angle, false);
					case 7: return new SpacialConfiguration(new Vector2(18, 35f), angle, false);
					case 8: return new SpacialConfiguration(new Vector2(18, 35f), angle, false);
					case 9: return new SpacialConfiguration(new Vector2(18, 37f), angle, false);
				}
			case RUN_LEFT:
				switch (frameIndex) {
					case 0: return new SpacialConfiguration(new Vector2(-18, 37f), -angle, true);
					case 1: return new SpacialConfiguration(new Vector2(-18, 39f), -angle, true);
					case 2: return new SpacialConfiguration(new Vector2(-18, 37f), -angle, true);
					case 3: return new SpacialConfiguration(new Vector2(-18, 35f), -angle, true);
					case 4: return new SpacialConfiguration(new Vector2(-18, 37f), -angle, true);
					case 5: return new SpacialConfiguration(new Vector2(-18, 39f), -angle, true);
					case 6: return new SpacialConfiguration(new Vector2(-18, 37f), -angle, true);
					case 7: return new SpacialConfiguration(new Vector2(-18, 35f), -angle, true);
				}
			case RUN_RIGHT:
				switch (frameIndex) {
					case 0: return new SpacialConfiguration(new Vector2(18, 37f), angle, false);
					case 1: return new SpacialConfiguration(new Vector2(18, 39f), angle, false);
					case 2: return new SpacialConfiguration(new Vector2(18, 37f), angle, false);
					case 3: return new SpacialConfiguration(new Vector2(18, 35f), angle, false);
					case 4: return new SpacialConfiguration(new Vector2(18, 37f), angle, false);
					case 5: return new SpacialConfiguration(new Vector2(18, 39f), angle, false);
					case 6: return new SpacialConfiguration(new Vector2(18, 37f), angle, false);
					case 7: return new SpacialConfiguration(new Vector2(18, 35f), angle, false);
				}
			case ATTACK_LEFT_ONE_HANDED_WEAPON_MINE:
			case ATTACK_LEFT_ONE_HANDED_WEAPON:
			case ATTACK_LEFT_ONE_HANDED_WEAPON_STAB:
			case ATTACK_LEFT_UNARMED:
			case STAND_LEFT_COMBAT_ONE_HANDED:
				return new SpacialConfiguration(new Vector2(-19, 50f), -combatAngle, true);
			case ATTACK_RIGHT_ONE_HANDED_WEAPON_STAB:
			case ATTACK_RIGHT_UNARMED:
			case ATTACK_RIGHT_ONE_HANDED_WEAPON_MINE:
			case ATTACK_RIGHT_ONE_HANDED_WEAPON:
			case STAND_RIGHT_COMBAT_ONE_HANDED:
				return new SpacialConfiguration(new Vector2(19, 50f), combatAngle, false);

			default:
				throw new RuntimeException("Unexpected action: " + getCurrentAction());
		}
	}


	public boolean offHandEquipped() {
		return !getAvailableEquipmentSlots().get(EquipmentSlot.OFFHAND).call();
	}


	@Override
	@SuppressWarnings("rawtypes")
	public int getConcurrentAttackNumber() {
		final Optional<Item> equippedWeapon = tryFind(getEquipped().keySet(), item -> {
			return item instanceof Weapon;
		});

		if (equippedWeapon.isPresent()) {
			return ((Weapon) equippedWeapon.get()).getAttackNumber(this);
		}

		return 2;
	}
}