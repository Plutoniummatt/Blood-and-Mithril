package bloodandmithril.character.individuals.characters;

import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_ONE_HANDED_WEAPON;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_ONE_HANDED_WEAPON_STAB;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_UNARMED;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_ONE_HANDED_WEAPON;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_ONE_HANDED_WEAPON_STAB;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_UNARMED;
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
import java.util.List;
import java.util.Map;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.ai.implementations.ElfAI;
import bloodandmithril.character.individuals.Humanoid;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.character.individuals.IndividualState;
import bloodandmithril.csi.ClientServerInterface;
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
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
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
@SuppressWarnings("unchecked")
public class Elf extends Humanoid {
	private static final long serialVersionUID = -5566954059579973505L;

	/** True if female */
	private boolean female;

	/** Hair/eye colors */
	private SerializableColor hairColor, eyeColor;
	
	/** Biography of this Elf */
	private String biography = "";

	/** Humanoid-specific animation map */
	private static Map<Action, List<WrapperForTwo<Animation, ShaderProgram>>> animationMap = newHashMap();

	static {
		if (ClientServerInterface.isClient()) {
			ArrayList<WrapperForTwo<Animation, ShaderProgram>> walkSequence = newArrayList(	
				wrap(AnimationHelper.animation(Domain.individualTexture, 0, 112, 64, 112, 10, 0.13f, Animation.LOOP), Shaders.replaceColor),		// HEAD
				wrap(AnimationHelper.animation(Domain.individualTexture, 0, 0,   64, 112, 10, 0.13f, Animation.LOOP), Shaders.filter),				// HAIR
				wrap(AnimationHelper.animation(Domain.individualTexture, 0, 448, 64, 112, 10, 0.13f, Animation.LOOP), Shaders.pass),				// BACK ARM
				wrap(AnimationHelper.animation(Domain.individualTexture, 0, 672, 64, 112, 10, 0.13f, Animation.LOOP), Shaders.pass),				// BACK LEG
				wrap(AnimationHelper.animation(Domain.individualTexture, 0, 224, 64, 112, 10, 0.13f, Animation.LOOP), Shaders.pass),				// TORSO
				wrap(AnimationHelper.animation(Domain.individualTexture, 0, 560, 64, 112, 10, 0.13f, Animation.LOOP), Shaders.pass),				// FRONT LEG
				wrap(AnimationHelper.animation(Domain.individualTexture, 0, 336, 64, 112, 10, 0.13f, Animation.LOOP), Shaders.pass)					// FRONT ARM
			);

			ArrayList<WrapperForTwo<Animation, ShaderProgram>> standSequence = newArrayList(
				wrap(AnimationHelper.animation(Domain.individualTexture, 1152, 112, 64, 112, 1, 1f, Animation.LOOP), Shaders.replaceColor),			// HEAD
				wrap(AnimationHelper.animation(Domain.individualTexture, 1152, 0,   64, 112, 1, 1f, Animation.LOOP), Shaders.filter),				// HAIR
				wrap(AnimationHelper.animation(Domain.individualTexture, 1152, 448, 64, 112, 1, 1f, Animation.LOOP), Shaders.pass),					// BACK ARM
				wrap(AnimationHelper.animation(Domain.individualTexture, 1152, 672, 64, 112, 1, 1f, Animation.LOOP), Shaders.pass),					// BACK LEG
				wrap(AnimationHelper.animation(Domain.individualTexture, 1152, 224, 64, 112, 1, 1f, Animation.LOOP), Shaders.pass),					// TORSO
				wrap(AnimationHelper.animation(Domain.individualTexture, 1152, 560, 64, 112, 1, 1f, Animation.LOOP), Shaders.pass),					// FRONT LEG
				wrap(AnimationHelper.animation(Domain.individualTexture, 1152, 336, 64, 112, 1, 1f, Animation.LOOP), Shaders.pass)					// FRONT ARM
			);

			ArrayList<WrapperForTwo<Animation, ShaderProgram>> standSequenceCombat = newArrayList(
				wrap(AnimationHelper.animation(Domain.individualTexture, 1216, 112, 64, 112, 1, 1f, Animation.LOOP), Shaders.replaceColor),			// HEAD
				wrap(AnimationHelper.animation(Domain.individualTexture, 1216, 0,   64, 112, 1, 1f, Animation.LOOP), Shaders.filter),				// HAIR
				wrap(AnimationHelper.animation(Domain.individualTexture, 1216, 448, 64, 112, 1, 1f, Animation.LOOP), Shaders.pass),					// BACK ARM
				wrap(AnimationHelper.animation(Domain.individualTexture, 1216, 672, 64, 112, 1, 1f, Animation.LOOP), Shaders.pass),					// BACK LEG
				wrap(AnimationHelper.animation(Domain.individualTexture, 1216, 224, 64, 112, 1, 1f, Animation.LOOP), Shaders.pass),					// TORSO
				wrap(AnimationHelper.animation(Domain.individualTexture, 1216, 560, 64, 112, 1, 1f, Animation.LOOP), Shaders.pass),					// FRONT LEG
				wrap(AnimationHelper.animation(Domain.individualTexture, 1216, 336, 64, 112, 1, 1f, Animation.LOOP), Shaders.pass)					// FRONT ARM
			);

			ArrayList<WrapperForTwo<Animation, ShaderProgram>> runSequence = newArrayList(
				wrap(AnimationHelper.animation(Domain.individualTexture, 640, 112, 64, 112, 8, 0.13f, Animation.LOOP), Shaders.replaceColor),		// HEAD
				wrap(AnimationHelper.animation(Domain.individualTexture, 640, 0,   64, 112, 8, 0.13f, Animation.LOOP), Shaders.filter),				// HAIR
				wrap(AnimationHelper.animation(Domain.individualTexture, 640, 448, 64, 112, 8, 0.13f, Animation.LOOP), Shaders.pass),				// BACK ARM
				wrap(AnimationHelper.animation(Domain.individualTexture, 640, 672, 64, 112, 8, 0.13f, Animation.LOOP), Shaders.pass),				// BACK LEG
				wrap(AnimationHelper.animation(Domain.individualTexture, 640, 224, 64, 112, 8, 0.13f, Animation.LOOP), Shaders.pass),				// TORSO
				wrap(AnimationHelper.animation(Domain.individualTexture, 640, 560, 64, 112, 8, 0.13f, Animation.LOOP), Shaders.pass),				// FRONT LEG
				wrap(AnimationHelper.animation(Domain.individualTexture, 640, 336, 64, 112, 8, 0.13f, Animation.LOOP), Shaders.pass)				// FRONT ARM
			);

			ArrayList<WrapperForTwo<Animation, ShaderProgram>> stabSequence = newArrayList(
				wrap(AnimationHelper.animation(Domain.individualTexture, 1280, 112, 64, 112, 8, 0.07f, Animation.NORMAL), Shaders.replaceColor),	// HEAD
				wrap(AnimationHelper.animation(Domain.individualTexture, 1280, 0,   64, 112, 8, 0.07f, Animation.NORMAL), Shaders.filter),			// HAIR
				wrap(AnimationHelper.animation(Domain.individualTexture, 1280, 448, 64, 112, 8, 0.07f, Animation.NORMAL), Shaders.pass),			// BACK ARM
				wrap(AnimationHelper.animation(Domain.individualTexture, 1280, 672, 64, 112, 8, 0.07f, Animation.NORMAL), Shaders.pass),			// BACK LEG
				wrap(AnimationHelper.animation(Domain.individualTexture, 1280, 224, 64, 112, 8, 0.07f, Animation.NORMAL), Shaders.pass),			// TORSO
				wrap(AnimationHelper.animation(Domain.individualTexture, 1280, 560, 64, 112, 8, 0.07f, Animation.NORMAL), Shaders.pass),			// FRONT LEG
				wrap(AnimationHelper.animation(Domain.individualTexture, 1280, 336, 64, 112, 8, 0.07f, Animation.NORMAL), Shaders.pass)				// FRONT ARM
			);

			ArrayList<WrapperForTwo<Animation, ShaderProgram>> slashSequence = newArrayList(
				wrap(AnimationHelper.animation(Domain.individualTexture, 1792, 112, 64, 112, 10, 0.07f, Animation.NORMAL), Shaders.replaceColor),	// HEAD
				wrap(AnimationHelper.animation(Domain.individualTexture, 1792, 0,   64, 112, 10, 0.07f, Animation.NORMAL), Shaders.filter),			// HAIR
				wrap(AnimationHelper.animation(Domain.individualTexture, 1792, 448, 64, 112, 10, 0.07f, Animation.NORMAL), Shaders.pass),			// BACK ARM
				wrap(AnimationHelper.animation(Domain.individualTexture, 1792, 672, 64, 112, 10, 0.07f, Animation.NORMAL), Shaders.pass),			// BACK LEG
				wrap(AnimationHelper.animation(Domain.individualTexture, 1792, 224, 64, 112, 10, 0.07f, Animation.NORMAL), Shaders.pass),			// TORSO
				wrap(AnimationHelper.animation(Domain.individualTexture, 1792, 560, 64, 112, 10, 0.07f, Animation.NORMAL), Shaders.pass),			// FRONT LEG
				wrap(AnimationHelper.animation(Domain.individualTexture, 1792, 336, 64, 112, 10, 0.07f, Animation.NORMAL), Shaders.pass)			// FRONT ARM
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
		this.setAi(new ElfAI(this));
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
		this.setAi(new ElfAI(this));
		this.hairColor = new SerializableColor(hairColor);
		this.eyeColor = new SerializableColor(eyeColor);
	}


	@Override
	public void render() {
		Shaders.filter.begin();
		Shaders.filter.setUniformf("color", hairColor);

		Shaders.replaceColor.begin();
		Shaders.replaceColor.setUniformf("toReplace", Color.RED);
		Shaders.replaceColor.setUniformf("color", eyeColor);
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
		this.female = ((Elf) other).female;
		this.biography = ((Elf) other).biography;
	}


	@Override
	public Individual copy() {
		Elf elf = new Elf(getId(), getState(), getFactionId(), female, getMaxCapacity(), getWorldId(), hairColor, eyeColor);
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
				getHitBox().position.x + (getCurrentAction().flipXAnimation() ? - getHitBox().width * (1f/3f) : getHitBox().width * (1f/3f)),
				getHitBox().position.y
			),
			getHitBox().width * 2 / 3,
			getHitBox().height
		);
	}


	@Override
	public float getUnarmedDamage() {
		return 0f;
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
		
		SoundService.femaleHit.play(
			SoundService.getVolumne(getState().position),
			1f,
			SoundService.getPan(getState().position)
		);
	}
}