package bloodandmithril.character.individuals.characters;

import static bloodandmithril.character.individuals.Individual.Action.RUN_LEFT;
import static bloodandmithril.character.individuals.Individual.Action.RUN_RIGHT;
import static bloodandmithril.character.individuals.Individual.Action.STAND_LEFT;
import static bloodandmithril.character.individuals.Individual.Action.STAND_RIGHT;
import static bloodandmithril.character.individuals.Individual.Action.WALK_LEFT;
import static bloodandmithril.character.individuals.Individual.Action.WALK_RIGHT;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import bloodandmithril.character.ai.implementations.ElfAI;
import bloodandmithril.character.conditions.Exhaustion;
import bloodandmithril.character.conditions.Hunger;
import bloodandmithril.character.conditions.Thirst;
import bloodandmithril.character.individuals.Humanoid;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.character.individuals.IndividualState;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.item.items.equipment.weapon.OneHandedWeapon;
import bloodandmithril.ui.KeyMappings;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.util.AnimationHelper;
import bloodandmithril.util.SerializableColor;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.SpacialConfiguration;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;

/**
 * Exceptional at:
 *
 * - Archery
 * - Walking/Running on water
 * - Infinite stamina
 * - Longer natural lifespan
 * - Better vision
 * - Natural wound healing
 * - Better at magic
 *
 * Disadvantages:
 *
 * - Low hitpoints
 * - Vegetarians
 * - Technophobes
 * - Can't carry much
 *
 * @author Matt
 */
public class Elf extends Humanoid {
	private static final long serialVersionUID = -5566954059579973505L;

	/** True if female */
	private boolean female;

	/** Hair/eye colors */
	private SerializableColor hairColor, eyeColor;

	/** Biography of this Elf */
	private String biography = "";

	/** Elf-specific animation map */
	private static Map<Action, List<Animation>> animationMap = newHashMap();

	static {
		if (ClientServerInterface.isClient()) {
			ArrayList<Animation> walkSequence = newArrayList(
				AnimationHelper.animation(Domain.individualTexture, 0, 112, 64, 112, 10, 0.11f),	// HEAD
				AnimationHelper.animation(Domain.individualTexture, 0, 0,   64, 112, 10, 0.11f),	// HAIR
				AnimationHelper.animation(Domain.individualTexture, 0, 448, 64, 112, 10, 0.11f),	// BACK ARM
				AnimationHelper.animation(Domain.individualTexture, 0, 672, 64, 112, 10, 0.11f),	// BACK LEG
				AnimationHelper.animation(Domain.individualTexture, 0, 224, 64, 112, 10, 0.11f),	// TORSO
				AnimationHelper.animation(Domain.individualTexture, 0, 560, 64, 112, 10, 0.11f),	// FRONT LEG
				AnimationHelper.animation(Domain.individualTexture, 0, 336, 64, 112, 10, 0.11f)		// FRONT ARM
			);

			ArrayList<Animation> standSequence = newArrayList(
				AnimationHelper.animation(Domain.individualTexture, 1152, 112, 64, 112, 1, 1f),		// HEAD
				AnimationHelper.animation(Domain.individualTexture, 1152, 0,   64, 112, 1, 1f),		// HAIR
				AnimationHelper.animation(Domain.individualTexture, 1152, 448, 64, 112, 1, 1f),		// BACK ARM
				AnimationHelper.animation(Domain.individualTexture, 1152, 672, 64, 112, 1, 1f),		// BACK LEG
				AnimationHelper.animation(Domain.individualTexture, 1152, 224, 64, 112, 1, 1f),		// TORSO
				AnimationHelper.animation(Domain.individualTexture, 1152, 560, 64, 112, 1, 1f),		// FRONT LEG
				AnimationHelper.animation(Domain.individualTexture, 1152, 336, 64, 112, 1, 1f)		// FRONT ARM
			);

			ArrayList<Animation> runSequence = newArrayList(
				AnimationHelper.animation(Domain.individualTexture, 640, 112, 64, 112, 8, 0.11f),	// HEAD
				AnimationHelper.animation(Domain.individualTexture, 640, 0,   64, 112, 8, 0.11f),	// HAIR
				AnimationHelper.animation(Domain.individualTexture, 640, 448, 64, 112, 8, 0.11f),	// BACK ARM
				AnimationHelper.animation(Domain.individualTexture, 640, 672, 64, 112, 8, 0.11f),	// BACK LEG
				AnimationHelper.animation(Domain.individualTexture, 640, 224, 64, 112, 8, 0.11f),	// TORSO
				AnimationHelper.animation(Domain.individualTexture, 640, 560, 64, 112, 8, 0.11f),	// FRONT LEG
				AnimationHelper.animation(Domain.individualTexture, 640, 336, 64, 112, 8, 0.11f)	// FRONT ARM
			);

			animationMap.put(
				WALK_RIGHT,
				walkSequence
			);

			animationMap.put(
				WALK_LEFT,
				walkSequence
			);

			animationMap.put(
				STAND_RIGHT,
				standSequence
			);

			animationMap.put(
				STAND_LEFT,
				standSequence
			);

			animationMap.put(
				RUN_RIGHT,
				runSequence
			);

			animationMap.put(
				RUN_LEFT,
				runSequence
			);
		}
	}

	/**
	 * Constructor
	 */
	public Elf(
			IndividualIdentifier id,
			IndividualState state,
			int factionId,
			boolean female,
			float capacity,
			World world,
			Color hairColor,
			Color eyeColor) {
		super(id, state, factionId, capacity, 10, 40, 95, 30, new Box(new Vector2(state.position.x, state.position.y), 120, 120), world.getWorldId(), 2);

		this.female = female;
		this.ai = new ElfAI(this);
		this.hairColor = new SerializableColor(hairColor);
		this.eyeColor = new SerializableColor(eyeColor);
	}


	/**
	 * Constructor
	 */
	private Elf(
			IndividualIdentifier id,
			IndividualState state,
			int factionId,
			boolean female,
			float capacity,
			int worldId,
			Color hairColor,
			Color eyeColor) {
		super(id, state, factionId, capacity, 10, 40, 95, 30, new Box(new Vector2(state.position.x, state.position.y), 120, 120), worldId, 2);

		this.female = female;
		this.ai = new ElfAI(this);
		this.hairColor = new SerializableColor(hairColor);
		this.eyeColor = new SerializableColor(eyeColor);
	}


	@Override
	protected void specificInternalRender() {
		// Render equipped items
		spriteBatch.setShader(Shaders.pass);
		Shaders.pass.setUniformMatrix("u_projTrans", BloodAndMithrilClient.cam.combined);
		for (Item equipped : getEquipped().keySet()) {
			Equipable toRender = (Equipable) equipped;
			if (equipped instanceof OneHandedWeapon) {
				SpacialConfiguration config = getOneHandedWeaponSpatialConfigration();
				if (config != null) {
					toRender.render(config.position.add(getState().position), config.orientation, config.flipX);
				}
			}
		}
	}


	@Override
	protected void internalUpdate(float delta) {
		super.internalUpdate(delta);

		if (ClientServerInterface.isServer()) {
			updateVitals(delta);
		}
	}


	private void updateVitals(float delta) {
		heal(delta * getState().healthRegen);

		decreaseHunger(0.000001f);
		decreaseThirst(0.000003f);

		if (isWalking()) {
			if (isCommandActive(KeyMappings.moveLeft) || isCommandActive(KeyMappings.moveRight)) {
				increaseStamina(delta * getState().staminaRegen / 2f);
			} else {
				increaseStamina(delta * getState().staminaRegen);
			}
		} else {
			if (isCommandActive(KeyMappings.moveLeft) || isCommandActive(KeyMappings.moveRight)) {
				decreaseStamina(0.0004f);
			} else {
				increaseStamina(delta * getState().staminaRegen);
			}
		}

		if (getState().hunger < 0.75f) {
			addCondition(new Hunger(getId().getId()));
		}

		if (getState().thirst < 0.75f) {
			addCondition(new Thirst(getId().getId()));
		}

		if (getState().stamina < 0.75f) {
			addCondition(new Exhaustion(getId().getId()));
		}
	}


	@Override
	public Color getToolTipTextColor() {
		return Color.GREEN;
	}


	@Override
	public List<MenuItem> internalGetContextMenuItems() {
		return Lists.newArrayList();
	}


	@Override
	public String getDescription() {
		return biography;
	}


	@Override
	public void updateDescription(String updated) {
		biography = updated;
	}


	@Override
	protected SpacialConfiguration getOneHandedWeaponSpatialConfigration() {
		return null;
	}


	@Override
	protected void internalCopyFrom(Individual other) {
		if (!(other instanceof Elf)) {
			throw new RuntimeException("Cannot cast " + other.getClass().getSimpleName() + " to Elf.");
		}

		this.hairColor = ((Elf) other).hairColor;
		this.eyeColor = ((Elf) other).eyeColor;
		this.female = ((Elf) other).female;
		this.biography = ((Elf) other).biography;
	}


	@Override
	public Individual copy() {
		Elf elf = new Elf(getId(), getState(), factionId, female, getMaxCapacity(), getWorldId(), hairColor, eyeColor);
		elf.copyFrom(this);
		return elf;
	}


	@Override
	protected Map<Action, List<Animation>> getAnimationMap() {
		return animationMap;
	}


	@Override
	public float getWalkSpeed() {
		return 55f;
	}


	@Override
	public float getRunSpeed() {
		return 120f;
	}
}