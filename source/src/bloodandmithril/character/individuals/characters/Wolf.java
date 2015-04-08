package bloodandmithril.character.individuals.characters;

import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_UNARMED;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_UNARMED;
import static bloodandmithril.util.datastructure.WrapperForTwo.wrap;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bloodandmithril.audio.SoundService.SuspicionLevel;
import bloodandmithril.audio.SoundService.SuspiciousSound;
import bloodandmithril.character.ai.AIProcessor;
import bloodandmithril.character.ai.implementations.HareAI;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.perception.Listener;
import bloodandmithril.character.ai.perception.Observer;
import bloodandmithril.character.ai.perception.SightStimulus;
import bloodandmithril.character.ai.perception.Sniffer;
import bloodandmithril.character.ai.perception.SoundStimulus;
import bloodandmithril.character.ai.perception.Visible;
import bloodandmithril.character.individuals.GroundTravellingIndividual;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.character.individuals.IndividualState;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.util.AnimationHelper;
import bloodandmithril.util.AnimationHelper.AnimationSwitcher;
import bloodandmithril.util.ParameterizedTask;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.SpacialConfiguration;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.util.datastructure.WrapperForTwo;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * A Wolf
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
@SuppressWarnings("unchecked")
public class Wolf extends GroundTravellingIndividual implements Visible, Listener, Observer, Sniffer {
	private static final long serialVersionUID = 6519740787651279948L;

	/** Rabbit-specific animation map */
	private static Map<Action, List<WrapperForTwo<AnimationSwitcher, ShaderProgram>>> animationMap = newHashMap();
	private static Map<Action, Map<Integer, ParameterizedTask<Individual>>> actionFrames = newHashMap();

	static {
		boolean server = !ClientServerInterface.isClient();

		AnimationSwitcher bite = new AnimationSwitcher();
		bite.animations.put(individual -> {return true;}, AnimationHelper.animation(Domain.individualTexture, 1260, 965, 140, 75, 7, 0.07f, PlayMode.LOOP_REVERSED));

		ArrayList<WrapperForTwo<AnimationSwitcher, ShaderProgram>> biteSequence = newArrayList(
			wrap(bite, server ? null : Shaders.pass)
		);

		AnimationSwitcher run = new AnimationSwitcher();
		run.animations.put(individual -> {return true;}, AnimationHelper.animation(Domain.individualTexture, 140, 965, 140, 75, 8, 0.06f, PlayMode.LOOP_REVERSED));

		ArrayList<WrapperForTwo<AnimationSwitcher, ShaderProgram>> runSequence = newArrayList(
			wrap(run, server ? null : Shaders.pass)
		);

		AnimationSwitcher stand = new AnimationSwitcher();
		stand.animations.put(individual -> {return true;}, AnimationHelper.animation(Domain.individualTexture, 0, 965, 140, 75, 1, 1f, PlayMode.LOOP_REVERSED));

		ArrayList<WrapperForTwo<AnimationSwitcher, ShaderProgram>> standSequence = newArrayList(
			wrap(stand, server ? null : Shaders.pass)
		);

		AnimationSwitcher jump = new AnimationSwitcher();
		jump.animations.put(individual -> {return true;}, AnimationHelper.animation(Domain.individualTexture, 560, 965, 140, 75, 1, 1f, PlayMode.LOOP_REVERSED));

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
				individual.attack(false);
			}
		);

		actionFrames.put(ATTACK_LEFT_UNARMED, biteAction);
		actionFrames.put(ATTACK_RIGHT_UNARMED, biteAction);
	}

	/**
	 * Constructor
	 */
	public Wolf(
			IndividualIdentifier id,
			IndividualState state,
			int factionId,
			int worldId) {
		super(
			id,
			state,
			factionId,
			0,
			0,
			0,
			110,
			60,
			60,
			new Box(new Vector2(state.position.x, state.position.y), 150, 80),
			worldId,
			2
		);
		this.setAi(new HareAI(this));
		setWalking(false);
	}


	@Override
	protected Map<Action, List<WrapperForTwo<AnimationSwitcher, ShaderProgram>>> getAnimationMap() {
		return animationMap;
	}


	@Override
	protected float getDefaultAttackPeriod() {
		return 2f;
	}


	@Override
	public Vector2 getEmissionPosition() {
		return getState().position.cpy().add(0, 40);
	}


	@Override
	public float getUnarmedMinDamage() {
		return 4f;
	}


	@Override
	public float getUnarmedMaxDamage() {
		return 7f;
	}


	@Override
	protected Box getDefaultAttackingHitBox() {
		return new Box(
			new Vector2(
				getHitBox().position.x,
				getHitBox().position.y
			),
			getHitBox().width,
			getHitBox().height
		);
	}


	@Override
	protected Map<Action, Map<Integer, ParameterizedTask<Individual>>> getActionFrames() {
		return actionFrames;
	}


	@Override
	public Individual copy() {
		Wolf wolf = new Wolf(getId(), getState(), getFactionId(), getWorldId());
		wolf.copyFrom(this);
		return wolf;
	}


	@Override
	protected void internalCopyFrom(Individual other) {
	}


	@Override
	protected float hungerDrain() {
		return 0;
	}


	@Override
	protected float thirstDrain() {
		return 0;
	}


	@Override
	protected float staminaDrain() {
		return 0;
	}


	@Override
	public void moan() {
	}


	@Override
	public float getWalkSpeed() {
		return 155;
	}


	@Override
	public float getRunSpeed() {
		return 155;
	}


	@Override
	public Color getToolTipTextColor() {
		return Color.GRAY;
	}


	@Override
	protected List<MenuItem> internalGetContextMenuItems() {
		return Lists.newArrayList();
	}


	@Override
	public String getDescription() {
		return "Big bad wolf";
	}


	@Override
	public void updateDescription(String updated) {
	}


	@Override
	public Set<Construction> getConstructables() {
		return Sets.newHashSet();
	}


	@Override
	public SpacialConfiguration getOneHandedWeaponSpatialConfigration() {
		return null;
	}


	@Override
	public SpacialConfiguration getTwoHandedWeaponSpatialConfigration() {
		return null;
	}


	@Override
	public int getConcurrentAttackNumber() {
		return 1;
	}


	@Override
	public Collection<Vector2> getVisibleLocations() {
		LinkedList<Vector2> locations = Lists.newLinkedList();
		locations.add(getState().position.cpy().add(0f, 10f));
		locations.add(getState().position.cpy().add(0f, 30f));
		return locations;
	}


	@Override
	public boolean isVisible() {
		return true;
	}


	@Override
	public void listen(SoundStimulus stimulus) {
		if (stimulus instanceof SuspiciousSound) {
			SuspicionLevel suspicionLevel = ((SuspiciousSound) stimulus).getSuspicionLevel();
			if (suspicionLevel.severity >= SuspicionLevel.INVESTIGATE.severity) {
				speak(" !", 1000);
				if (getState().position.x > stimulus.getEmissionPosition().x) {
					AIProcessor.sendPathfindingRequest(this, new WayPoint(getState().position.cpy().add(300f, 0f)), false, 300f, false, false);
				} else {
					AIProcessor.sendPathfindingRequest(this, new WayPoint(getState().position.cpy().add(-300f, 0f)), false, 300f, false, false);
				}
			}
		}
	}


	@Override
	public Vector2 getObservationPosition() {
		return getState().position.cpy().add(0f, 30f);
	}


	@Override
	public Vector2 getObservationDirection() {
		return getCurrentAction().left() ? new Vector2(-1f, 0f) : new Vector2(1f, 0f);
	}


	@Override
	public float getFieldOfView() {
		return 120f;
	}


	@Override
	public void reactToSightStimulus(SightStimulus stimulus) {
	}


	@Override
	public float getViewDistance() {
		return 1000;
	}


	@Override
	public boolean reactIfVisible(SoundStimulus stimulus) {
		return true;
	}


	@Override
	public SpacialConfiguration getOffHandSpatialConfigration() {
		return null;
	}


	@Override
	protected void renderCustomizations(int animationIndex) {
	}
}
