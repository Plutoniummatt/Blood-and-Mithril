package bloodandmithril.character.individuals.characters;

import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_ONE_HANDED_WEAPON;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_ONE_HANDED_WEAPON_MINE;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_ONE_HANDED_WEAPON_STAB;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_UNARMED;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_ONE_HANDED_WEAPON;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_ONE_HANDED_WEAPON_MINE;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_ONE_HANDED_WEAPON_STAB;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_UNARMED;
import static bloodandmithril.character.individuals.Individual.Action.JUMP_LEFT;
import static bloodandmithril.character.individuals.Individual.Action.JUMP_RIGHT;
import static bloodandmithril.character.individuals.Individual.Action.RUN_LEFT;
import static bloodandmithril.character.individuals.Individual.Action.RUN_RIGHT;
import static bloodandmithril.character.individuals.Individual.Action.STAND_LEFT;
import static bloodandmithril.character.individuals.Individual.Action.STAND_LEFT_COMBAT;
import static bloodandmithril.character.individuals.Individual.Action.STAND_RIGHT;
import static bloodandmithril.character.individuals.Individual.Action.STAND_RIGHT_COMBAT;
import static bloodandmithril.character.individuals.Individual.Action.WALK_LEFT;
import static bloodandmithril.character.individuals.Individual.Action.WALK_RIGHT;
import static bloodandmithril.util.datastructure.WrapperForTwo.wrap;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bloodandmithril.audio.SoundService;
import bloodandmithril.audio.SoundService.SuspicionLevel;
import bloodandmithril.audio.SoundService.SuspiciousSound;
import bloodandmithril.character.ai.implementations.ElfAI;
import bloodandmithril.character.ai.perception.Listener;
import bloodandmithril.character.ai.perception.Observer;
import bloodandmithril.character.ai.perception.SightStimulus;
import bloodandmithril.character.ai.perception.SoundStimulus;
import bloodandmithril.character.ai.perception.Visible;
import bloodandmithril.character.ai.task.Wait;
import bloodandmithril.character.individuals.Humanoid;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.character.individuals.IndividualState;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Description;
import bloodandmithril.core.Name;
import bloodandmithril.item.material.mineral.SandStone;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.prop.construction.craftingstation.BlacksmithWorkshop;
import bloodandmithril.prop.construction.craftingstation.Campfire;
import bloodandmithril.prop.construction.craftingstation.Furnace;
import bloodandmithril.prop.construction.craftingstation.WorkBench;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.util.AnimationHelper;
import bloodandmithril.util.SerializableColor;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Util;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.util.datastructure.WrapperForTwo;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Exceptional at:
 *
 * - Archery
 * - Physical activities do not deplete stamina
 * - Longer natural life span
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
@Name(name = "Elves")
@Description(description = "Elves are children of nature, they are nimble creatures with a good grip on magic and excel at archery.")
@SuppressWarnings("unchecked")
@Copyright("Matthew Peck 2014")
public class Elf extends Humanoid implements Observer, Visible, Listener {
	private static final long serialVersionUID = -5566954059579973505L;

	/** True if female */
	private boolean female;

	/** Hair/eye colors */
	private SerializableColor hairColor, eyeColor, skinColor;

	/** Biography of this Elf */
	private String biography = "";

	/** Humanoid-specific animation map */
	private static Map<Action, List<WrapperForTwo<Animation, ShaderProgram>>> animationMap = newHashMap();

	static {
		boolean server = !ClientServerInterface.isClient();
		ArrayList<WrapperForTwo<Animation, ShaderProgram>> walkSequence = newArrayList(
			wrap(AnimationHelper.animation(Domain.individualTexture, 0, 112, 64, 112, 10, 0.13f, PlayMode.LOOP), server ? null : Shaders.filterIgnoreReplace),		// HEAD
			wrap(AnimationHelper.animation(Domain.individualTexture, 0, 0,   64, 112, 10, 0.13f, PlayMode.LOOP), server ? null : Shaders.colorize),					// HAIR
			wrap(AnimationHelper.animation(Domain.individualTexture, 0, 448, 64, 112, 10, 0.13f, PlayMode.LOOP), server ? null : Shaders.filterIgnoreReplace),		// BACK ARM
			wrap(AnimationHelper.animation(Domain.individualTexture, 0, 672, 64, 112, 10, 0.13f, PlayMode.LOOP), server ? null : Shaders.filterIgnoreReplace),		// BACK LEG
			wrap(AnimationHelper.animation(Domain.individualTexture, 0, 224, 64, 112, 10, 0.13f, PlayMode.LOOP), server ? null : Shaders.filterIgnoreReplace),		// TORSO
			wrap(AnimationHelper.animation(Domain.individualTexture, 0, 560, 64, 112, 10, 0.13f, PlayMode.LOOP), server ? null : Shaders.filterIgnoreReplace),		// FRONT LEG
			wrap(AnimationHelper.animation(Domain.individualTexture, 0, 336, 64, 112, 10, 0.13f, PlayMode.LOOP), server ? null : Shaders.filterIgnoreReplace)		// FRONT ARM
		);

		ArrayList<WrapperForTwo<Animation, ShaderProgram>> standSequence = newArrayList(
			wrap(AnimationHelper.animation(Domain.individualTexture, 1152, 112, 64, 112, 1, 1f, PlayMode.LOOP), server ? null : Shaders.filterIgnoreReplace),		// HEAD
			wrap(AnimationHelper.animation(Domain.individualTexture, 1152, 0,   64, 112, 1, 1f, PlayMode.LOOP), server ? null : Shaders.colorize),					// HAIR
			wrap(AnimationHelper.animation(Domain.individualTexture, 1152, 448, 64, 112, 1, 1f, PlayMode.LOOP), server ? null : Shaders.filterIgnoreReplace),		// BACK ARM
			wrap(AnimationHelper.animation(Domain.individualTexture, 1152, 672, 64, 112, 1, 1f, PlayMode.LOOP), server ? null : Shaders.filterIgnoreReplace),		// BACK LEG
			wrap(AnimationHelper.animation(Domain.individualTexture, 1152, 224, 64, 112, 1, 1f, PlayMode.LOOP), server ? null : Shaders.filterIgnoreReplace),		// TORSO
			wrap(AnimationHelper.animation(Domain.individualTexture, 1152, 560, 64, 112, 1, 1f, PlayMode.LOOP), server ? null : Shaders.filterIgnoreReplace),		// FRONT LEG
			wrap(AnimationHelper.animation(Domain.individualTexture, 1152, 336, 64, 112, 1, 1f, PlayMode.LOOP), server ? null : Shaders.filterIgnoreReplace)		// FRONT ARM
		);

		ArrayList<WrapperForTwo<Animation, ShaderProgram>> jumpequence = newArrayList(
			wrap(AnimationHelper.animation(Domain.individualTexture, 768, 112, 64, 112, 1, 1f, PlayMode.LOOP), server ? null : Shaders.filterIgnoreReplace),		// HEAD
			wrap(AnimationHelper.animation(Domain.individualTexture, 768, 0,   64, 112, 1, 1f, PlayMode.LOOP), server ? null : Shaders.colorize),					// HAIR
			wrap(AnimationHelper.animation(Domain.individualTexture, 768, 448, 64, 112, 1, 1f, PlayMode.LOOP), server ? null : Shaders.filterIgnoreReplace),		// BACK ARM
			wrap(AnimationHelper.animation(Domain.individualTexture, 768, 672, 64, 112, 1, 1f, PlayMode.LOOP), server ? null : Shaders.filterIgnoreReplace),		// BACK LEG
			wrap(AnimationHelper.animation(Domain.individualTexture, 768, 224, 64, 112, 1, 1f, PlayMode.LOOP), server ? null : Shaders.filterIgnoreReplace),		// TORSO
			wrap(AnimationHelper.animation(Domain.individualTexture, 768, 560, 64, 112, 1, 1f, PlayMode.LOOP), server ? null : Shaders.filterIgnoreReplace),		// FRONT LEG
			wrap(AnimationHelper.animation(Domain.individualTexture, 768, 336, 64, 112, 1, 1f, PlayMode.LOOP), server ? null : Shaders.filterIgnoreReplace)		// FRONT ARM
		);

		ArrayList<WrapperForTwo<Animation, ShaderProgram>> standSequenceCombat = newArrayList(
			wrap(AnimationHelper.animation(Domain.individualTexture, 1216, 112, 64, 112, 1, 1f, PlayMode.LOOP), server ? null : Shaders.filterIgnoreReplace),		// HEAD
			wrap(AnimationHelper.animation(Domain.individualTexture, 1216, 0,   64, 112, 1, 1f, PlayMode.LOOP), server ? null : Shaders.colorize),					// HAIR
			wrap(AnimationHelper.animation(Domain.individualTexture, 1216, 448, 64, 112, 1, 1f, PlayMode.LOOP), server ? null : Shaders.filterIgnoreReplace),		// BACK ARM
			wrap(AnimationHelper.animation(Domain.individualTexture, 1216, 672, 64, 112, 1, 1f, PlayMode.LOOP), server ? null : Shaders.filterIgnoreReplace),		// BACK LEG
			wrap(AnimationHelper.animation(Domain.individualTexture, 1216, 224, 64, 112, 1, 1f, PlayMode.LOOP), server ? null : Shaders.filterIgnoreReplace),		// TORSO
			wrap(AnimationHelper.animation(Domain.individualTexture, 1216, 560, 64, 112, 1, 1f, PlayMode.LOOP), server ? null : Shaders.filterIgnoreReplace),		// FRONT LEG
			wrap(AnimationHelper.animation(Domain.individualTexture, 1216, 336, 64, 112, 1, 1f, PlayMode.LOOP), server ? null : Shaders.filterIgnoreReplace)		// FRONT ARM
		);

		ArrayList<WrapperForTwo<Animation, ShaderProgram>> runSequence = newArrayList(
			wrap(AnimationHelper.animation(Domain.individualTexture, 640, 112, 64, 112, 8, 0.13f, PlayMode.LOOP), server ? null : Shaders.filterIgnoreReplace),	// HEAD
			wrap(AnimationHelper.animation(Domain.individualTexture, 640, 0,   64, 112, 8, 0.13f, PlayMode.LOOP), server ? null : Shaders.colorize),					// HAIR
			wrap(AnimationHelper.animation(Domain.individualTexture, 640, 448, 64, 112, 8, 0.13f, PlayMode.LOOP), server ? null : Shaders.filterIgnoreReplace),	// BACK ARM
			wrap(AnimationHelper.animation(Domain.individualTexture, 640, 672, 64, 112, 8, 0.13f, PlayMode.LOOP), server ? null : Shaders.filterIgnoreReplace),	// BACK LEG
			wrap(AnimationHelper.animation(Domain.individualTexture, 640, 224, 64, 112, 8, 0.13f, PlayMode.LOOP), server ? null : Shaders.filterIgnoreReplace),	// TORSO
			wrap(AnimationHelper.animation(Domain.individualTexture, 640, 560, 64, 112, 8, 0.13f, PlayMode.LOOP), server ? null : Shaders.filterIgnoreReplace),	// FRONT LEG
			wrap(AnimationHelper.animation(Domain.individualTexture, 640, 336, 64, 112, 8, 0.13f, PlayMode.LOOP), server ? null : Shaders.filterIgnoreReplace)		// FRONT ARM
		);

		ArrayList<WrapperForTwo<Animation, ShaderProgram>> stabSequence = newArrayList(
			wrap(AnimationHelper.animation(Domain.individualTexture, 1280, 112, 64, 112, 8, 0.07f, PlayMode.NORMAL), server ? null : Shaders.filterIgnoreReplace),	// HEAD
			wrap(AnimationHelper.animation(Domain.individualTexture, 1280, 0,   64, 112, 8, 0.07f, PlayMode.NORMAL), server ? null : Shaders.filter),				// HAIR
			wrap(AnimationHelper.animation(Domain.individualTexture, 1280, 448, 64, 112, 8, 0.07f, PlayMode.NORMAL), server ? null : Shaders.filterIgnoreReplace),	// BACK ARM
			wrap(AnimationHelper.animation(Domain.individualTexture, 1280, 672, 64, 112, 8, 0.07f, PlayMode.NORMAL), server ? null : Shaders.filterIgnoreReplace),	// BACK LEG
			wrap(AnimationHelper.animation(Domain.individualTexture, 1280, 224, 64, 112, 8, 0.07f, PlayMode.NORMAL), server ? null : Shaders.filterIgnoreReplace),	// TORSO
			wrap(AnimationHelper.animation(Domain.individualTexture, 1280, 560, 64, 112, 8, 0.07f, PlayMode.NORMAL), server ? null : Shaders.filterIgnoreReplace),	// FRONT LEG
			wrap(AnimationHelper.animation(Domain.individualTexture, 1280, 336, 64, 112, 8, 0.07f, PlayMode.NORMAL), server ? null : Shaders.filterIgnoreReplace)	// FRONT ARM
		);

		ArrayList<WrapperForTwo<Animation, ShaderProgram>> slashSequence = newArrayList(
			wrap(AnimationHelper.animation(Domain.individualTexture, 1792, 112, 64, 112, 10, 0.07f, PlayMode.NORMAL), server ? null : Shaders.filterIgnoreReplace),// HEAD
			wrap(AnimationHelper.animation(Domain.individualTexture, 1792, 0,   64, 112, 10, 0.07f, PlayMode.NORMAL), server ? null : Shaders.colorize),				// HAIR
			wrap(AnimationHelper.animation(Domain.individualTexture, 1792, 448, 64, 112, 10, 0.07f, PlayMode.NORMAL), server ? null : Shaders.filterIgnoreReplace),// BACK ARM
			wrap(AnimationHelper.animation(Domain.individualTexture, 1792, 672, 64, 112, 10, 0.07f, PlayMode.NORMAL), server ? null : Shaders.filterIgnoreReplace),// BACK LEG
			wrap(AnimationHelper.animation(Domain.individualTexture, 1792, 224, 64, 112, 10, 0.07f, PlayMode.NORMAL), server ? null : Shaders.filterIgnoreReplace),// TORSO
			wrap(AnimationHelper.animation(Domain.individualTexture, 1792, 560, 64, 112, 10, 0.07f, PlayMode.NORMAL), server ? null : Shaders.filterIgnoreReplace),// FRONT LEG
			wrap(AnimationHelper.animation(Domain.individualTexture, 1792, 336, 64, 112, 10, 0.07f, PlayMode.NORMAL), server ? null : Shaders.filterIgnoreReplace)	// FRONT ARM
		);

		animationMap.put(
			JUMP_RIGHT,
			jumpequence
		);

		animationMap.put(
			JUMP_LEFT,
			jumpequence
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
			STAND_RIGHT_COMBAT,
			standSequenceCombat
		);

		animationMap.put(
			STAND_LEFT_COMBAT,
			standSequenceCombat
		);

		animationMap.put(
			RUN_RIGHT,
			runSequence
		);

		animationMap.put(
			RUN_LEFT,
			runSequence
		);

		animationMap.put(
			ATTACK_RIGHT_UNARMED,
			stabSequence
		);

		animationMap.put(
			ATTACK_LEFT_UNARMED,
			stabSequence
		);

		animationMap.put(
			ATTACK_RIGHT_ONE_HANDED_WEAPON_STAB,
			stabSequence
		);

		animationMap.put(
			ATTACK_LEFT_ONE_HANDED_WEAPON_STAB,
			stabSequence
		);

		animationMap.put(
			ATTACK_RIGHT_ONE_HANDED_WEAPON,
			slashSequence
		);

		animationMap.put(
			ATTACK_LEFT_ONE_HANDED_WEAPON,
			slashSequence
		);

		animationMap.put(
			ATTACK_RIGHT_ONE_HANDED_WEAPON_MINE,
			slashSequence
		);

		animationMap.put(
			ATTACK_LEFT_ONE_HANDED_WEAPON_MINE,
			slashSequence
		);
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
			Color eyeColor,
			Color skinColor) {
		super(
			id,
			state,
			factionId,
			capacity,
			100,
			10,
			40,
			95,
			30,
			new Box(
				new Vector2(
					state.position.x,
					state.position.y
				),
				120,
				120
			),
			world == null ? 0 : world.getWorldId(),
			2
		);

		this.female = female;
		this.setAi(new ElfAI(this));
		this.hairColor = new SerializableColor(hairColor);
		this.eyeColor = new SerializableColor(eyeColor);
		this.skinColor = new SerializableColor(skinColor);
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
			Color eyeColor,
			Color skinColor) {
		super(id, state, factionId, capacity, 100, 10, 40, 95, 30, new Box(new Vector2(state.position.x, state.position.y), 120, 120), worldId, 2);

		this.female = female;
		this.setAi(new ElfAI(this));
		this.hairColor = new SerializableColor(hairColor);
		this.eyeColor = new SerializableColor(eyeColor);
		this.skinColor = new SerializableColor(skinColor);
	}


	@Override
	public void render() {
		Shaders.filterIgnoreReplace.begin();
		Shaders.filterIgnoreReplace.setUniformf("toReplace", Color.RED);
		Shaders.filterIgnoreReplace.setUniformf("color", eyeColor.r, eyeColor.g, eyeColor.b, eyeColor.a);
		Shaders.filterIgnoreReplace.setUniformf("filter", skinColor.r, skinColor.g, skinColor.b, skinColor.a);
		Shaders.filterIgnoreReplace.setUniformf("ignore", Color.WHITE);

		Shaders.colorize.begin();
		Shaders.colorize.setUniformf("amount", 5f);
		Shaders.colorize.setUniformf("color", hairColor.r, hairColor.g, hairColor.b, hairColor.a);
		super.render();
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
	protected void internalCopyFrom(Individual other) {
		if (!(other instanceof Elf)) {
			throw new RuntimeException("Cannot cast " + other.getClass().getSimpleName() + " to Elf.");
		}

		this.hairColor = ((Elf) other).hairColor;
		this.eyeColor = ((Elf) other).eyeColor;
		this.skinColor = ((Elf) other).skinColor;
		this.female = ((Elf) other).female;
		this.biography = ((Elf) other).biography;
	}


	@Override
	public Individual copy() {
		Elf elf = new Elf(
			getId(),
			getState(),
			getFactionId(),
			female,
			getMaxCapacity(),
			getWorldId(),
			new Color(hairColor.r, hairColor.g, hairColor.b, hairColor.a),
			new Color(eyeColor.r, eyeColor.g, eyeColor.b, eyeColor.a),
			new Color(skinColor.r, skinColor.g, skinColor.b, skinColor.a)
		);
		elf.copyFrom(this);
		return elf;
	}


	@Override
	protected Map<Action, List<WrapperForTwo<Animation, ShaderProgram>>> getAnimationMap() {
		return animationMap;
	}


	@Override
	public float getWalkSpeed() {
		return 45f;
	}


	@Override
	public float getRunSpeed() {
		return 90f;
	}


	@Override
	protected Box getDefaultAttackingHitBox() {
		return new Box(
			new Vector2(
				getHitBox().position.x + (getCurrentAction().left() ? - getHitBox().width * (1f/3f) : getHitBox().width * (1f/3f)),
				getHitBox().position.y
			),
			getHitBox().width * 2 / 3,
			getHitBox().height
		);
	}


	@Override
	public float getUnarmedDamage() {
		return 0.5f;
	}


	@Override
	protected float getDefaultAttackPeriod() {
		return 0.8f;
	}


	@Override
	protected float hungerDrain() {
		return 0.000001f;
	}


	@Override
	protected float thirstDrain() {
		return 0.000003f;
	}


	@Override
	protected float staminaDrain() {
		return 0.0004f;
	}


	@Override
	public void moan() {
		if (Util.roll(0.85f)) {
			return;
		}

		if (ClientServerInterface.isClient()) {
			SoundService.play(
				SoundService.femaleHit,
				getState().position,
				true,
				this
			);
		}
	}


	@Override
	public Set<Construction> getConstructables() {
		return Sets.newHashSet(
			new Furnace(SandStone.class, 0, 0),
			new WorkBench(0, 0),
			new BlacksmithWorkshop(0, 0),
			new Campfire(0, 0)
		);
	}


	@Override
	public Vector2 getEmissionPosition() {
		return getState().position.cpy().add(getCurrentAction().left() ? 3 : 7, getHeight() / 2);
	}


	@Override
	public Vector2 getObservationPosition() {
		return getState().position.cpy().add(0f, getHeight() - 15);
	}


	@Override
	public Vector2 getObservationDirection() {
		return null;
	}


	@Override
	public Vector2 getFieldOfView() {
		return null;
	}


	@Override
	public float getViewDistance() {
		return 2000f;
	}


	@Override
	public void reactToSightStimulus(SightStimulus stimulus) {
		if (stimulus instanceof IndividualSighted) {
			Individual sighted = Domain.getIndividual(((IndividualSighted) stimulus).getSightedIndividualId());
			if (sighted != null && Util.roll(0.02f)) {
				speak(Util.randomOneOf("Nice hair, ", "Hey ", "Look it's ", "I see you, ") + sighted.getId().getFirstName(), 1500);
			}
		}
	}


	@Override
	public Collection<Vector2> getVisibleLocations() {
		LinkedList<Vector2> locations = Lists.newLinkedList();
		for (int i = 10; i < getHeight() - 10 ; i += 10) {
			locations.add(getState().position.cpy().add(0f, i));
		}
		return locations;
	}


	@Override
	public void listen(SoundStimulus stimulus) {
		if (stimulus instanceof SuspiciousSound) {
			SuspicionLevel suspicionLevel = ((SuspiciousSound) stimulus).getSuspicionLevel();
			if (suspicionLevel.severity >= SuspicionLevel.INVESTIGATE.severity) {
				if (getState().position.x > stimulus.getEmissionPosition().x) {
					setCurrentAction(Action.STAND_LEFT);
				} else {
					setCurrentAction(Action.STAND_RIGHT);
				}
				getAI().setCurrentTask(new Wait(this, 3f));

				String speech = Util.randomOneOf("What was that sound?", "Hmm?", "You hear that?", "Huh?", "What?", "I hear something...");

				speak(speech, 1500);
			}
		}
	}


	@Override
	public boolean isVisible() {
		return true;
	}


	@Override
	public boolean reactIfVisible(SoundStimulus stimulus) {
		return false;
	}
}