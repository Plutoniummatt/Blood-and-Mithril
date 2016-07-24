package bloodandmithril.graphics.renderers;

import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_UNARMED;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_UNARMED;
import static bloodandmithril.util.datastructure.WrapperForTwo.wrap;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.google.inject.Singleton;

import bloodandmithril.character.combat.CombatService;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.Individual.Action;
import bloodandmithril.character.individuals.characters.Wolf;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Textures;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.AnimationHelper;
import bloodandmithril.util.AnimationHelper.AnimationSwitcher;
import bloodandmithril.util.ParameterizedTask;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.datastructure.WrapperForTwo;

/**
 * Responsible for the rendering of {@link Wolf}
 *
 * @author Matt
 */
@Singleton
@SuppressWarnings("unchecked")
@Copyright("Matthew Peck 2015")
public class WolfRenderer extends IndividualRendererImpl {

	/** Wolf-specific animation map */
	public static Map<Action, List<WrapperForTwo<AnimationSwitcher, ShaderProgram>>> animationMap = newHashMap();
	public static Map<Action, Map<Integer, ParameterizedTask<Individual>>> actionFrames = newHashMap();

	static {
		boolean server = !ClientServerInterface.isClient();

		AnimationSwitcher bite = new AnimationSwitcher();
		bite.animations.put(individual -> {return true;}, AnimationHelper.animation(Textures.INDIVIDUAL_TEXTURE, 1260, 965, 140, 75, 7, 0.07f, PlayMode.LOOP_REVERSED));

		ArrayList<WrapperForTwo<AnimationSwitcher, ShaderProgram>> biteSequence = newArrayList(
			wrap(bite, server ? null : Shaders.pass)
		);

		AnimationSwitcher run = new AnimationSwitcher();
		run.animations.put(individual -> {return true;}, AnimationHelper.animation(Textures.INDIVIDUAL_TEXTURE, 140, 965, 140, 75, 8, 0.06f, PlayMode.LOOP_REVERSED));

		ArrayList<WrapperForTwo<AnimationSwitcher, ShaderProgram>> runSequence = newArrayList(
			wrap(run, server ? null : Shaders.pass)
		);

		AnimationSwitcher stand = new AnimationSwitcher();
		stand.animations.put(individual -> {return true;}, AnimationHelper.animation(Textures.INDIVIDUAL_TEXTURE, 0, 965, 140, 75, 1, 1f, PlayMode.LOOP_REVERSED));

		ArrayList<WrapperForTwo<AnimationSwitcher, ShaderProgram>> standSequence = newArrayList(
			wrap(stand, server ? null : Shaders.pass)
		);

		AnimationSwitcher jump = new AnimationSwitcher();
		jump.animations.put(individual -> {return true;}, AnimationHelper.animation(Textures.INDIVIDUAL_TEXTURE, 560, 965, 140, 75, 1, 1f, PlayMode.LOOP_REVERSED));

		ArrayList<WrapperForTwo<AnimationSwitcher, ShaderProgram>> jumpSequence = newArrayList(
			wrap(jump, server ? null : Shaders.pass)
		);

		animationMap.put(Action.RUN_LEFT, runSequence);
		animationMap.put(Action.RUN_RIGHT, runSequence);

		animationMap.put(Action.WALK_LEFT, runSequence);
		animationMap.put(Action.WALK_RIGHT, runSequence);

		animationMap.put(Action.JUMP_LEFT, jumpSequence);
		animationMap.put(Action.JUMP_RIGHT, jumpSequence);

		animationMap.put(Action.ATTACK_LEFT_UNARMED, biteSequence);
		animationMap.put(Action.ATTACK_RIGHT_UNARMED, biteSequence);

		animationMap.put(Action.STAND_LEFT, standSequence);
		animationMap.put(Action.STAND_RIGHT, standSequence);
		animationMap.put(Action.STAND_LEFT_COMBAT_ONE_HANDED, standSequence);
		animationMap.put(Action.STAND_RIGHT_COMBAT_ONE_HANDED, standSequence);

		Map<Integer, ParameterizedTask<Individual>> biteAction = newHashMap();
		biteAction.put(
			2,
			individual -> {
				CombatService.strike(individual);
			}
		);

		actionFrames.put(ATTACK_LEFT_UNARMED, biteAction);
		actionFrames.put(ATTACK_RIGHT_UNARMED, biteAction);
	}
}