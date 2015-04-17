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
import static bloodandmithril.character.individuals.Individual.Action.STAND_LEFT_COMBAT_ONE_HANDED;
import static bloodandmithril.character.individuals.Individual.Action.STAND_RIGHT;
import static bloodandmithril.character.individuals.Individual.Action.STAND_RIGHT_COMBAT_ONE_HANDED;
import static bloodandmithril.character.individuals.Individual.Action.WALK_LEFT;
import static bloodandmithril.character.individuals.Individual.Action.WALK_RIGHT;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
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
import bloodandmithril.character.individuals.Humanoid;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.character.individuals.IndividualState;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Description;
import bloodandmithril.core.Name;
import bloodandmithril.graphics.WorldRenderer;
import bloodandmithril.item.material.mineral.SandStone;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.prop.construction.craftingstation.BlacksmithWorkshop;
import bloodandmithril.prop.construction.craftingstation.Campfire;
import bloodandmithril.prop.construction.craftingstation.Furnace;
import bloodandmithril.prop.construction.craftingstation.WorkBench;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.util.AnimationHelper;
import bloodandmithril.util.AnimationHelper.AnimationSwitcher;
import bloodandmithril.util.SerializableColor;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.SpacialConfiguration;
import bloodandmithril.util.Util;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.util.datastructure.WrapperForTwo;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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

	/** Hair style of this elf */
	private int hairStyle;

	/** Elf female hairstyles */
	private static Map<Integer, TextureRegion> hairStyleFemale = Maps.newHashMap();

	/** Humanoid-specific animation map */
	private static Map<Action, List<WrapperForTwo<AnimationSwitcher, ShaderProgram>>> animationMap = newHashMap();

	static {
		boolean server = !ClientServerInterface.isClient();

		if (ClientServerInterface.isClient()) {
			hairStyleFemale.put(1, new TextureRegion(WorldRenderer.individualTexture, 896, 784, 64, 112));
			hairStyleFemale.put(2, new TextureRegion(WorldRenderer.individualTexture, 960, 784, 64, 112));
			hairStyleFemale.put(3, new TextureRegion(WorldRenderer.individualTexture, 1024, 784, 64, 112));
		}

		AnimationSwitcher walk1 = new AnimationSwitcher();
		AnimationSwitcher walk2 = new AnimationSwitcher();
		AnimationSwitcher walk3 = new AnimationSwitcher();
		AnimationSwitcher walk4 = new AnimationSwitcher();
		AnimationSwitcher walk5 = new AnimationSwitcher();
		AnimationSwitcher walk6 = new AnimationSwitcher();
		walk1.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 0, 112, 64, 112, 10, 0.13f, PlayMode.LOOP));        										// HEAD
		walk2.animations.put(individual -> {return ((Humanoid) individual).offHandEquipped();}, AnimationHelper.animation(WorldRenderer.individualTexture, 0, 0, 64, 112, 10, 0.13f, PlayMode.LOOP));        	// BACK ARM
		walk2.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 0, 448, 64, 112, 10, 0.13f, PlayMode.LOOP));       										// BACK ARM
		walk3.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 0, 672, 64, 112, 10, 0.13f, PlayMode.LOOP));        										// BACK LEG
		walk4.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 0, 224, 64, 112, 10, 0.13f, PlayMode.LOOP));        										// TORSO
		walk5.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 0, 560, 64, 112, 10, 0.13f, PlayMode.LOOP));        										// FRONT LEG
		walk6.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 0, 336, 64, 112, 10, 0.13f, PlayMode.LOOP));        										// FRONT ARM

		ArrayList<WrapperForTwo<AnimationSwitcher, ShaderProgram>> walkSequence = newArrayList(
			wrap(walk1, server ? null : Shaders.filterIgnoreReplace),
			wrap(walk2, server ? null : Shaders.filterIgnoreReplace),
			wrap(walk3, server ? null : Shaders.filterIgnoreReplace),
			wrap(walk4, server ? null : Shaders.filterIgnoreReplace),
			wrap(walk5, server ? null : Shaders.filterIgnoreReplace),
			wrap(walk6, server ? null : Shaders.filterIgnoreReplace)
		);

		AnimationSwitcher stand1 = new AnimationSwitcher();
		AnimationSwitcher stand2 = new AnimationSwitcher();
		AnimationSwitcher stand3 = new AnimationSwitcher();
		AnimationSwitcher stand4 = new AnimationSwitcher();
		AnimationSwitcher stand5 = new AnimationSwitcher();
		AnimationSwitcher stand6 = new AnimationSwitcher();
		stand1.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 1152, 112, 64, 112, 1, 1f, PlayMode.LOOP));        									// HEAD
		stand2.animations.put(individual -> {return ((Humanoid) individual).offHandEquipped();}, AnimationHelper.animation(WorldRenderer.individualTexture, 1152, 0, 64, 112, 1, 1f, PlayMode.LOOP));    	// BACK ARM
		stand2.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 1152, 448, 64, 112, 1, 1f, PlayMode.LOOP));        									// BACK ARM
		stand3.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 1152, 672, 64, 112, 1, 1f, PlayMode.LOOP));       									// BACK LEG
		stand4.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 1152, 224, 64, 112, 1, 1f, PlayMode.LOOP));       									// TORSO
		stand5.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 1152, 560, 64, 112, 1, 1f, PlayMode.LOOP));       									// FRONT LEG
		stand6.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 1152, 336, 64, 112, 1, 1f, PlayMode.LOOP));       									// FRONT ARM

		ArrayList<WrapperForTwo<AnimationSwitcher, ShaderProgram>> standSequence = newArrayList(
			wrap(stand1, server ? null : Shaders.filterIgnoreReplace),
			wrap(stand2, server ? null : Shaders.filterIgnoreReplace),
			wrap(stand3, server ? null : Shaders.filterIgnoreReplace),
			wrap(stand4, server ? null : Shaders.filterIgnoreReplace),
			wrap(stand5, server ? null : Shaders.filterIgnoreReplace),
			wrap(stand6, server ? null : Shaders.filterIgnoreReplace)
		);

		AnimationSwitcher jump1 = new AnimationSwitcher();
		AnimationSwitcher jump2 = new AnimationSwitcher();
		AnimationSwitcher jump3 = new AnimationSwitcher();
		AnimationSwitcher jump4 = new AnimationSwitcher();
		AnimationSwitcher jump5 = new AnimationSwitcher();
		AnimationSwitcher jump6 = new AnimationSwitcher();
		jump1.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 768, 112, 64, 112, 1, 1f, PlayMode.LOOP));        								// HEAD
		jump2.animations.put(individual -> {return ((Humanoid) individual).offHandEquipped();}, AnimationHelper.animation(WorldRenderer.individualTexture, 768, 0, 64, 112, 1, 1f, PlayMode.LOOP));	// BACK ARM
		jump2.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 768, 448, 64, 112, 1, 1f, PlayMode.LOOP));        								// BACK ARM
		jump3.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 768, 672, 64, 112, 1, 1f, PlayMode.LOOP));        								// BACK LEG
		jump4.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 768, 224, 64, 112, 1, 1f, PlayMode.LOOP));       								// TORSO
		jump5.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 768, 560, 64, 112, 1, 1f, PlayMode.LOOP));        								// FRONT LEG
		jump6.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 768, 336, 64, 112, 1, 1f, PlayMode.LOOP));        								// FRONT ARM

		ArrayList<WrapperForTwo<AnimationSwitcher, ShaderProgram>> jumpSequence = newArrayList(
			wrap(jump1, server ? null : Shaders.filterIgnoreReplace),
			wrap(jump2, server ? null : Shaders.filterIgnoreReplace),
			wrap(jump3, server ? null : Shaders.filterIgnoreReplace),
			wrap(jump4, server ? null : Shaders.filterIgnoreReplace),
			wrap(jump5, server ? null : Shaders.filterIgnoreReplace),
			wrap(jump6, server ? null : Shaders.filterIgnoreReplace)
		);

		AnimationSwitcher standCombat1 = new AnimationSwitcher();
		AnimationSwitcher standCombat2 = new AnimationSwitcher();
		AnimationSwitcher standCombat3 = new AnimationSwitcher();
		AnimationSwitcher standCombat4 = new AnimationSwitcher();
		AnimationSwitcher standCombat5 = new AnimationSwitcher();
		AnimationSwitcher standCombat6 = new AnimationSwitcher();
		standCombat1.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 1216, 112, 64, 112, 1, 1f, PlayMode.LOOP));        // HEAD
		standCombat2.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 1216, 448, 64, 112, 1, 1f, PlayMode.LOOP));        // BACK ARM
		standCombat3.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 1216, 672, 64, 112, 1, 1f, PlayMode.LOOP));        // BACK LEG
		standCombat4.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 1216, 224, 64, 112, 1, 1f, PlayMode.LOOP));        // TORSO
		standCombat5.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 1216, 560, 64, 112, 1, 1f, PlayMode.LOOP));        // FRONT LEG
		standCombat6.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 1216, 336, 64, 112, 1, 1f, PlayMode.LOOP));        // FRONT ARM

		ArrayList<WrapperForTwo<AnimationSwitcher, ShaderProgram>> standSequenceCombat = newArrayList(
			wrap(standCombat1, server ? null : Shaders.filterIgnoreReplace),
			wrap(standCombat2, server ? null : Shaders.filterIgnoreReplace),
			wrap(standCombat3, server ? null : Shaders.filterIgnoreReplace),
			wrap(standCombat4, server ? null : Shaders.filterIgnoreReplace),
			wrap(standCombat5, server ? null : Shaders.filterIgnoreReplace),
			wrap(standCombat6, server ? null : Shaders.filterIgnoreReplace)
		);

		AnimationSwitcher run1 = new AnimationSwitcher();
		AnimationSwitcher run2 = new AnimationSwitcher();
		AnimationSwitcher run3 = new AnimationSwitcher();
		AnimationSwitcher run4 = new AnimationSwitcher();
		AnimationSwitcher run5 = new AnimationSwitcher();
		AnimationSwitcher run6 = new AnimationSwitcher();
		run1.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 640, 112, 64, 112, 8, 0.13f, PlayMode.LOOP));        										// HEAD
		run2.animations.put(individual -> {return ((Humanoid) individual).offHandEquipped();}, AnimationHelper.animation(WorldRenderer.individualTexture, 640, 0, 64, 112, 8, 0.13f, PlayMode.LOOP));        	// BACK ARM
		run2.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 640, 448, 64, 112, 8, 0.13f, PlayMode.LOOP));       										// BACK ARM
		run3.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 640, 672, 64, 112, 8, 0.13f, PlayMode.LOOP));        										// BACK LEG
		run4.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 640, 224, 64, 112, 8, 0.13f, PlayMode.LOOP));        										// TORSO
		run5.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 640, 560, 64, 112, 8, 0.13f, PlayMode.LOOP));        										// FRONT LEG
		run6.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 640, 336, 64, 112, 8, 0.13f, PlayMode.LOOP));        										// FRONT ARM

		ArrayList<WrapperForTwo<AnimationSwitcher, ShaderProgram>> runSequence = newArrayList(
			wrap(run1, server ? null : Shaders.filterIgnoreReplace),
			wrap(run2, server ? null : Shaders.filterIgnoreReplace),
			wrap(run3, server ? null : Shaders.filterIgnoreReplace),
			wrap(run4, server ? null : Shaders.filterIgnoreReplace),
			wrap(run5, server ? null : Shaders.filterIgnoreReplace),
			wrap(run6, server ? null : Shaders.filterIgnoreReplace)
		);

		AnimationSwitcher stab1 = new AnimationSwitcher();
		AnimationSwitcher stab2 = new AnimationSwitcher();
		AnimationSwitcher stab3 = new AnimationSwitcher();
		AnimationSwitcher stab4 = new AnimationSwitcher();
		AnimationSwitcher stab5 = new AnimationSwitcher();
		AnimationSwitcher stab6 = new AnimationSwitcher();
		stab1.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 1280, 112, 64, 112, 8, 0.07f, PlayMode.NORMAL));        // HEAD
		stab2.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 1280, 448, 64, 112, 8, 0.07f, PlayMode.NORMAL));        // BACK ARM
		stab3.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 1280, 672, 64, 112, 8, 0.07f, PlayMode.NORMAL));        // BACK LEG
		stab4.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 1280, 224, 64, 112, 8, 0.07f, PlayMode.NORMAL));        // TORSO
		stab5.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 1280, 560, 64, 112, 8, 0.07f, PlayMode.NORMAL));        // FRONT LEG
		stab6.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 1280, 336, 64, 112, 8, 0.07f, PlayMode.NORMAL));        // FRONT ARM

		ArrayList<WrapperForTwo<AnimationSwitcher, ShaderProgram>> stabSequence = newArrayList(
			wrap(stab1, server ? null : Shaders.filterIgnoreReplace),
			wrap(stab2, server ? null : Shaders.filterIgnoreReplace),
			wrap(stab3, server ? null : Shaders.filterIgnoreReplace),
			wrap(stab4, server ? null : Shaders.filterIgnoreReplace),
			wrap(stab5, server ? null : Shaders.filterIgnoreReplace),
			wrap(stab6, server ? null : Shaders.filterIgnoreReplace)
		);

		AnimationSwitcher slash1 = new AnimationSwitcher();
		AnimationSwitcher slash2 = new AnimationSwitcher();
		AnimationSwitcher slash3 = new AnimationSwitcher();
		AnimationSwitcher slash4 = new AnimationSwitcher();
		AnimationSwitcher slash5 = new AnimationSwitcher();
		AnimationSwitcher slash6 = new AnimationSwitcher();
		slash1.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 1792, 112, 64, 112, 10, 0.07f, PlayMode.NORMAL));      // HEAD
		slash2.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 1792, 448, 64, 112, 10, 0.07f, PlayMode.NORMAL));      // BACK ARM
		slash3.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 1792, 672, 64, 112, 10, 0.07f, PlayMode.NORMAL));      // BACK LEG
		slash4.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 1792, 224, 64, 112, 10, 0.07f, PlayMode.NORMAL));      // TORSO
		slash5.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 1792, 560, 64, 112, 10, 0.07f, PlayMode.NORMAL));      // FRONT LEG
		slash6.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 1792, 336, 64, 112, 10, 0.07f, PlayMode.NORMAL));      // FRONT ARM

		ArrayList<WrapperForTwo<AnimationSwitcher, ShaderProgram>> slashSequence = newArrayList(
			wrap(slash1, server ? null : Shaders.filterIgnoreReplace),
			wrap(slash2, server ? null : Shaders.filterIgnoreReplace),
			wrap(slash3, server ? null : Shaders.filterIgnoreReplace),
			wrap(slash4, server ? null : Shaders.filterIgnoreReplace),
			wrap(slash5, server ? null : Shaders.filterIgnoreReplace),
			wrap(slash6, server ? null : Shaders.filterIgnoreReplace)
		);

		animationMap.put(
			JUMP_RIGHT,
			jumpSequence
		);

		animationMap.put(
			JUMP_LEFT,
			jumpSequence
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
			STAND_RIGHT_COMBAT_ONE_HANDED,
			standSequenceCombat
		);

		animationMap.put(
			STAND_LEFT_COMBAT_ONE_HANDED,
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

	
	public static void setup() {
	}
	

	/**
	 * Constructor
	 */
	public Elf(
			IndividualIdentifier id,
			IndividualState state,
			int factionId,
			boolean female,
			World world,
			Color hairColor,
			Color eyeColor,
			Color skinColor) {
		super(
			id,
			state,
			factionId,
			50f,
			250,
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

		this.hairStyle = Util.getRandom().nextInt(3) + 1;
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

		this.hairStyle = Util.getRandom().nextInt(3) + 1;
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
	protected Map<Action, List<WrapperForTwo<AnimationSwitcher, ShaderProgram>>> getAnimationMap() {
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
	public float getUnarmedMinDamage() {
		return 0.5f;
	}


	@Override
	public float getUnarmedMaxDamage() {
		return 1.0f;
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
		return getCurrentAction().left() ? new Vector2(-1f, 0f) : new Vector2(1f, 0f);
	}


	@Override
	public float getFieldOfView() {
		return 150f;
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
	public void sayStuck() {
		speak("Looks like I'm stuck...", 1000);
	}


	@Override
	public boolean reactIfVisible(SoundStimulus stimulus) {
		return false;
	}


	@Override
	protected void renderCustomizations(int animationIndex) {
		TextureRegion hair = hairStyleFemale.get(hairStyle);
		SpacialConfiguration helmetConfig = getHelmetSpatialConfigration();

		spriteBatch.setShader(Shaders.colorize);
		Shaders.colorize.setUniformMatrix("u_projTrans", BloodAndMithrilClient.cam.combined);
		if (animationIndex == 1) {
			spriteBatch.draw(
				WorldRenderer.individualTexture,
				getState().position.x - hair.getRegionWidth() / 2 + helmetConfig.position.x,
				getState().position.y + helmetConfig.position.y,
				0,
				0,
				hair.getRegionWidth(),
				hair.getRegionHeight(),
				1f,
				1f,
				0,
				hair.getRegionX(),
				hair.getRegionY(),
				hair.getRegionWidth(),
				hair.getRegionHeight(),
				helmetConfig.flipX,
				false
			);
		}
	}
}