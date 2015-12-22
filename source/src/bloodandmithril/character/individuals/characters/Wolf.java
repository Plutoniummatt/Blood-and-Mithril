package bloodandmithril.character.individuals.characters;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import bloodandmithril.character.ai.implementations.WolfAI;
import bloodandmithril.character.ai.perception.Listener;
import bloodandmithril.character.ai.perception.Observer;
import bloodandmithril.character.ai.perception.Sniffer;
import bloodandmithril.character.ai.perception.SoundStimulus;
import bloodandmithril.character.ai.perception.Visible;
import bloodandmithril.character.individuals.Animal;
import bloodandmithril.character.individuals.GroundTravellingIndividual;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.character.individuals.IndividualState;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.graphics.renderers.WolfRenderer;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.util.AnimationHelper.AnimationSwitcher;
import bloodandmithril.util.ParameterizedTask;
import bloodandmithril.util.SpacialConfiguration;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.util.datastructure.WrapperForTwo;

/**
 * A Wolf
 *
 * @author Matt
 */
@Name(name = "Wolves")
@Copyright("Matthew Peck 2015")
public class Wolf extends GroundTravellingIndividual implements Listener, Observer, Sniffer, Animal {
	private static final long serialVersionUID = 6519740787651279948L;

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
			Behaviour.HOSTILE,
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
		this.setAi(new WolfAI(this));
		setWalking(false);
	}


	@Override
	protected Map<Action, List<WrapperForTwo<AnimationSwitcher, ShaderProgram>>> getAnimationMap() {
		return WolfRenderer.animationMap;
	}


	@Override
	public float getDefaultAttackPeriod() {
		return 1.3f;
	}


	@Override
	public Vector2 getEmissionPosition() {
		return getState().position.cpy().add(0, 40);
	}


	@Override
	public float getUnarmedMinDamage() {
		return 3f;
	}


	@Override
	public float getUnarmedMaxDamage() {
		return 6f;
	}


	@Override
	public Box getDefaultAttackingHitBox() {
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
		return WolfRenderer.actionFrames;
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
	protected void internalKill() {
	}


	@Override
	public boolean sameAs(Visible other) {
		if (other instanceof Hare) {
			return ((Wolf) other).getId().getId() == getId().getId();
		}
		return false;
	}


	@Override
	public void playAffirmativeSound() {
		// TODO Auto-generated method stub
		
	}
}
