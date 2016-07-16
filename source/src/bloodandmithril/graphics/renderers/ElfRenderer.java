package bloodandmithril.graphics.renderers;

import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_ONE_HANDED_WEAPON;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_ONE_HANDED_WEAPON_MINE;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_ONE_HANDED_WEAPON_STAB;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_UNARMED;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_ONE_HANDED_WEAPON;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_ONE_HANDED_WEAPON_MINE;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_ONE_HANDED_WEAPON_STAB;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_UNARMED;
import static bloodandmithril.character.individuals.Individual.Action.DEAD;
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
import static bloodandmithril.util.datastructure.WrapperForTwo.wrap;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;

import bloodandmithril.character.individuals.Humanoid;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.Individual.Action;
import bloodandmithril.character.individuals.characters.Elf;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.graphics.WorldRenderer;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.AnimationHelper;
import bloodandmithril.util.AnimationHelper.AnimationSwitcher;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.SpacialConfiguration;
import bloodandmithril.util.datastructure.WrapperForTwo;

/**
 * Responsible for the rendering of {@link Elf}
 *
 * @author Matt
 */
@Singleton
@SuppressWarnings("unchecked")
@Copyright("Matthew Peck 2015")
public class ElfRenderer extends IndividualRendererImpl {

	/** Elf female hairstyles */
	public static Map<Integer, TextureRegion> hairStyleFemale = Maps.newHashMap();

	/** Humanoid-specific animation map */
	public static Map<Action, List<WrapperForTwo<AnimationSwitcher, ShaderProgram>>> animationMap = newHashMap();

	static {
		boolean server = !ClientServerInterface.isClient();

		if (ClientServerInterface.isClient()) {
			hairStyleFemale.put(1, new TextureRegion(WorldRenderer.individualTexture, 896, 784, 64, 112));
			hairStyleFemale.put(2, new TextureRegion(WorldRenderer.individualTexture, 960, 784, 64, 112));
			hairStyleFemale.put(3, new TextureRegion(WorldRenderer.individualTexture, 1024, 784, 64, 112));
		}

		AnimationSwitcher dead = new AnimationSwitcher();
		dead.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 0, 1040, 44, 70, 1, 1f, PlayMode.LOOP));

		ArrayList<WrapperForTwo<AnimationSwitcher, ShaderProgram>> deadSequence = newArrayList(
			wrap(dead, server ? null : Shaders.pass)
		);

		AnimationSwitcher walk1 = new AnimationSwitcher();
		AnimationSwitcher walk2 = new AnimationSwitcher();
		AnimationSwitcher walk3 = new AnimationSwitcher();
		AnimationSwitcher walk4 = new AnimationSwitcher();
		AnimationSwitcher walk5 = new AnimationSwitcher();
		AnimationSwitcher walk6 = new AnimationSwitcher();
		walk1.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 0, 112, 64, 112, 10, 0.052f, PlayMode.LOOP));        										// HEAD
		walk2.animations.put(individual -> {return ((Humanoid) individual).offHandEquipped();}, AnimationHelper.animation(WorldRenderer.individualTexture, 0, 0, 64, 112, 10, 0.052f, PlayMode.LOOP));        	// BACK ARM
		walk2.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 0, 448, 64, 112, 10, 0.052f, PlayMode.LOOP));       										// BACK ARM
		walk3.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 0, 672, 64, 112, 10, 0.052f, PlayMode.LOOP));        										// BACK LEG
		walk4.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 0, 224, 64, 112, 10, 0.052f, PlayMode.LOOP));        										// TORSO
		walk5.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 0, 560, 64, 112, 10, 0.052f, PlayMode.LOOP));        										// FRONT LEG
		walk6.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 0, 336, 64, 112, 10, 0.052f, PlayMode.LOOP));        										// FRONT ARM

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
		run1.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 640, 112, 64, 112, 8, 0.052f, PlayMode.LOOP));        										// HEAD
		run2.animations.put(individual -> {return ((Humanoid) individual).offHandEquipped();}, AnimationHelper.animation(WorldRenderer.individualTexture, 640, 0, 64, 112, 8, 0.052f, PlayMode.LOOP));        	// BACK ARM
		run2.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 640, 448, 64, 112, 8, 0.052f, PlayMode.LOOP));       										// BACK ARM
		run3.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 640, 672, 64, 112, 8, 0.052f, PlayMode.LOOP));        										// BACK LEG
		run4.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 640, 224, 64, 112, 8, 0.052f, PlayMode.LOOP));        										// TORSO
		run5.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 640, 560, 64, 112, 8, 0.052f, PlayMode.LOOP));        										// FRONT LEG
		run6.animations.put(individual -> {return true;}, AnimationHelper.animation(WorldRenderer.individualTexture, 640, 336, 64, 112, 8, 0.052f, PlayMode.LOOP));        										// FRONT ARM

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
			DEAD,
			deadSequence
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


	@Override
	public void internalRender(Individual indi, Graphics graphics) {
		Elf elf = (Elf) indi;
		
		Shaders.filterIgnoreReplace.begin();
		Shaders.filterIgnoreReplace.setUniformf("toReplace", Color.RED);
		Shaders.filterIgnoreReplace.setUniformf("color", elf.getEyeColor().r, elf.getEyeColor().g, elf.getEyeColor().b, elf.getEyeColor().a);

		Shaders.filterIgnoreReplace.setUniformf(
			"filter",
			Math.max(elf.getSkinColor().r, elf.getDeathAlpha()),
			Math.max(elf.getSkinColor().g, elf.getDeathAlpha()),
			Math.max(elf.getSkinColor().b, elf.getDeathAlpha()),
			Math.max(elf.getSkinColor().a, elf.getDeathAlpha())
		);

		Shaders.filterIgnoreReplace.setUniformf("ignore", Color.WHITE);

		Shaders.colorize.begin();
		Shaders.colorize.setUniformf("amount", 5f);
		Shaders.colorize.setUniformf(
			"color",
			Math.max(elf.getHairColor().r, elf.getDeathAlpha()),
			Math.max(elf.getHairColor().g, elf.getDeathAlpha()),
			Math.max(elf.getHairColor().b, elf.getDeathAlpha()),
			Math.max(elf.getHairColor().a, elf.getDeathAlpha())
		);

		super.internalRender(elf, graphics);
	}


	@Override
	protected void renderCustomizations(Individual indi, int animationIndex, Graphics graphics) {
		Elf elf = (Elf) indi;
		
		TextureRegion hair = hairStyleFemale.get(elf.getHairStyle());
		SpacialConfiguration helmetConfig = elf.getHelmetSpatialConfigration();

		graphics.getSpriteBatch().setShader(Shaders.colorize);
		Shaders.colorize.setUniformMatrix("u_projTrans", graphics.getCam().combined);
		if (animationIndex == 1) {
			graphics.getSpriteBatch().draw(
				WorldRenderer.individualTexture,
				elf.getState().position.x - hair.getRegionWidth() / 2 + helmetConfig.position.x,
				elf.getState().position.y + helmetConfig.position.y,
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
