package bloodandmithril.character.individuals.characters;

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
import bloodandmithril.util.ParameterizedTask;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.SpacialConfiguration;
import bloodandmithril.util.Util;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.util.datastructure.WrapperForTwo;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * A wild brown furred hare
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@SuppressWarnings("unchecked")
public class Hare extends GroundTravellingIndividual implements Visible, Listener, Observer {
	private static final long serialVersionUID = -1907997976760409204L;

	private static final Color brown = new Color(117f/255f, 76f/255f, 36f/255f, 1f);

	/** Rabbit-specific animation map */
	private static Map<Action, List<WrapperForTwo<Animation, ShaderProgram>>> animationMap = newHashMap();

	static {
		boolean server = !ClientServerInterface.isClient();
		ArrayList<WrapperForTwo<Animation, ShaderProgram>> walkSequence = newArrayList(
			wrap(AnimationHelper.animation(Domain.individualTexture, 0, 899, 48, 48, 4, 0.07f, PlayMode.LOOP), server ? null : Shaders.pass)
		);

		ArrayList<WrapperForTwo<Animation, ShaderProgram>> runSequence = newArrayList(
			wrap(AnimationHelper.animation(Domain.individualTexture, 0, 899, 48, 48, 4, 0.05f, PlayMode.LOOP), server ? null : Shaders.pass)
		);

		ArrayList<WrapperForTwo<Animation, ShaderProgram>> standSequence = newArrayList(
			wrap(AnimationHelper.animation(Domain.individualTexture, 0, 899, 48, 48, 1, 1f, PlayMode.LOOP), server ? null : Shaders.pass)
		);

		animationMap.put(Action.RUN_LEFT, runSequence);
		animationMap.put(Action.RUN_RIGHT, runSequence);

		animationMap.put(Action.WALK_LEFT, walkSequence);
		animationMap.put(Action.WALK_RIGHT, walkSequence);

		animationMap.put(Action.STAND_LEFT, standSequence);
		animationMap.put(Action.STAND_RIGHT, standSequence);
	}

	/**
	 * Constructor
	 */
	public Hare(
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
			16,
			16,
			60,
			new Box(new Vector2(state.position.x, state.position.y), 60, 60),
			worldId,
			2
		);
		this.setAi(new HareAI(this));
		setWalking(Util.getRandom().nextBoolean());
	}


	@Override
	protected Map<Action, List<WrapperForTwo<Animation, ShaderProgram>>> getAnimationMap() {
		return animationMap;
	}


	@Override
	protected float getDefaultAttackPeriod() {
		return 0;
	}


	@Override
	public Vector2 getEmissionPosition() {
		return getState().position.cpy().add(0, 10);
	}


	@Override
	public float getUnarmedDamage() {
		return 0;
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
		return Maps.newHashMap();
	}


	@Override
	public Individual copy() {
		Hare hare = new Hare(getId(), getState(), getFactionId(), getWorldId());
		hare.copyFrom(this);
		return hare;
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
		return 65;
	}


	@Override
	public float getRunSpeed() {
		return 90;
	}


	@Override
	public Color getToolTipTextColor() {
		return brown;
	}


	@Override
	protected List<MenuItem> internalGetContextMenuItems() {
		return Lists.newArrayList();
	}


	@Override
	public String getDescription() {
		return "Just a rabbit";
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
		return null;
	}


	@Override
	public Vector2 getFieldOfView() {
		return null;
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
}