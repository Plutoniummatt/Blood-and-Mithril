package bloodandmithril.character.individuals;

import java.util.HashMap;
import java.util.List;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.implementations.BoarAI;
import bloodandmithril.character.ai.task.Idle;
import bloodandmithril.character.faction.Faction;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.ui.KeyMappings;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.util.AnimationHelper;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.SpacialConfiguration;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Topography;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;

/**
 * A Boar...
 *
 * @author Matt
 */
public class Boar extends Individual {

	private String biography = new String(
		"Wild boar or wild pig (Sus scrofa) is a species of the pig genus Sus, " +
		"part of the biological family Suidae. The species includes many subspecies. " +
		"It is the wild ancestor of the domestic pig, an animal with which it freely hybridises."
	);

	private static final String WALKING_RIGHT = "walkingRight";
	private static final String WALKING_LEFT = "walkingLeft";
	private static final String STANDING_RIGHT = "standingRight";
	private static final String STANDING_LEFT = "standingLeft";

	private static final long serialVersionUID = -5319279288781067263L;

	/** Animations */
	private static HashMap<String, Animation> animations = new HashMap<String, Animation>();

	/** Current animation */
	private String current;

	/**
	 * Constructor
	 */
	public Boar(IndividualIdentifier id, IndividualState state, World world) {
		super(id, state, Faction.NPC, 0f, 0, 64, 32, 120, new Box(new Vector2(state.position.x, state.position.y), 120, 120), world.getWorldId(), 2);
		ai = new BoarAI(this);
		current = STANDING_RIGHT;
	}


	/**
	 * Constructor
	 */
	private Boar(IndividualIdentifier id, IndividualState state, int worldId) {
		super(id, state, Faction.NPC, 0f, 0, 64, 32, 120, new Box(new Vector2(state.position.x, state.position.y), 120, 120), worldId, 2);
		ai = new BoarAI(this);
		current = STANDING_RIGHT;
	}


	/**
	 * Loads animations
	 */
	public static void loadAnimations() {
		animations.put(STANDING_LEFT, AnimationHelper.makeAnimation(Domain.individualTexture, 768, 0, 64, 40, 1, 1));
		animations.put(STANDING_RIGHT, AnimationHelper.makeAnimation(Domain.individualTexture, 704, 0, 64, 40, 1, 1));
		animations.put(WALKING_LEFT, AnimationHelper.makeAnimation(Domain.individualTexture, 704, 80, 64, 40, 5, 0.17f));
		animations.put(WALKING_RIGHT, AnimationHelper.makeAnimation(Domain.individualTexture, 704, 40, 64, 40, 5, 0.17f));
	}


	@Override
	protected void internalRender() {
		BloodAndMithrilClient.spriteBatch.begin();

		// Determine which shader we're using, normal, or highlighted
		if (isMouseOver()) {

			Shaders.elfHighLight.begin();
			Shaders.elfHighLight.setUniformi("hair", 0);
			Shaders.elfHighLight.setUniformf("alpha", 1f);
			Shaders.elfHighLight.end();
			BloodAndMithrilClient.spriteBatch.setShader(Shaders.elfHighLight);
			Shaders.elfHighLight.setUniformMatrix("u_projTrans", BloodAndMithrilClient.cam.combined);
		} else {

			Shaders.elfDayLight.begin();
			Shaders.elfDayLight.setUniformi("hair", 0);
			Shaders.elfDayLight.setUniformf("alpha", 1f);
			Shaders.elfDayLight.end();
			BloodAndMithrilClient.spriteBatch.setShader(Shaders.elfDayLight);
			Shaders.elfDayLight.setUniformMatrix("u_projTrans", BloodAndMithrilClient.cam.combined);
		}

		// Draw the body
		BloodAndMithrilClient.spriteBatch.draw(
			animations.get(current).getKeyFrame(animationTimer, true),
			getState().position.x - animations.get(current).getKeyFrame(0f).getRegionWidth()/2,
			getState().position.y
		);

		BloodAndMithrilClient.spriteBatch.end();
		BloodAndMithrilClient.spriteBatch.flush();
	}


	@Override
	protected void internalUpdate(float delta) {
		updateAnimation();
	}


	@Override
	protected void respondToCommands() {
		//Horizontal movement
		Topography topography = Domain.getWorld(getWorldId()).getTopography();
		if (Math.abs(getState().velocity.y) < 5f) {
			if (isCommandActive(KeyMappings.moveLeft) && (canStepUp(-2, topography) || !obstructed(-2, topography))) {
				if (isCommandActive(KeyMappings.walk)) {
					getState().velocity.x = -30f;
				}
			} else if (isCommandActive(KeyMappings.moveRight) && (canStepUp(2, topography) || !obstructed(2, topography))) {
				if (isCommandActive(KeyMappings.walk)) {
					getState().velocity.x = 30f;
				}
			} else {
				getState().velocity.x = 0f;
				getState().acceleration.x = 0f;

				int offset = isCommandActive(KeyMappings.moveRight) ? 2 : isCommandActive(KeyMappings.moveLeft) ? -2 : 0;
				if (obstructed(offset, topography) && !canStepUp(offset, topography) && !(getAI().getCurrentTask() instanceof Idle)) {
					getAI().setCurrentTask(new Idle());
				}

				sendCommand(KeyMappings.moveRight, false);
				sendCommand(KeyMappings.moveLeft, false);
				sendCommand(KeyMappings.walk, false);
			}
		}
	}


	/** What animation should we use? */
	private void updateAnimation() {
		if (getState().velocity.x > 0) {
			if (isCommandActive(KeyMappings.walk) && !current.equals(WALKING_RIGHT)) {
				current = WALKING_RIGHT;
				animationTimer = 0f;
			}
		} else if (getState().velocity.x < 0) {
			if (isCommandActive(KeyMappings.walk) && !current.equals(WALKING_LEFT)) {
				current = WALKING_LEFT;
				animationTimer = 0f;
			}
		} else if (getState().velocity.x == 0 && !current.equals(STANDING_LEFT) && !current.equals(STANDING_RIGHT)) {
			current = current.equals(WALKING_RIGHT) ? STANDING_RIGHT : STANDING_LEFT;
		}
	}


	@Override
	public boolean isMouseOver() {
		float x = BloodAndMithrilClient.getMouseWorldX();
		float y = BloodAndMithrilClient.getMouseWorldY();

		boolean ans = x >= getState().position.x - getWidth()/2 && x <= getState().position.x + getWidth()/2 && y >= getState().position.y && y <= getState().position.y + getHeight();
		return ans;
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
	protected SpacialConfiguration getOneHandedWeaponSpacialConfigration() {
		return null;
	}


	@Override
	protected void internalCopyFrom(Individual other) {
		if (!(other instanceof Boar)) {
			throw new RuntimeException("Cannot cast " + other.getClass().getSimpleName() + " to Boar.");
		}

		this.biography = ((Boar) other).biography;
		this.current = ((Boar) other).current;
	}


	@Override
	public Individual copy() {
		Boar boar = new Boar(getId(), getState(), getWorldId());
		boar.copyFrom(this);
		return boar;
	}
}