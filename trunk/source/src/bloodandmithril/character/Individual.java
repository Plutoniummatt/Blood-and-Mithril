package bloodandmithril.character;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;


import bloodandmithril.Fortress;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ArtificialIntelligence;
import bloodandmithril.character.ai.task.GoToLocation;
import bloodandmithril.character.ai.task.Idle;
import bloodandmithril.character.individuals.Boar;
import bloodandmithril.character.individuals.Elf;
import bloodandmithril.item.Container;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.world.Epoch;
import bloodandmithril.world.GameWorld;
import bloodandmithril.world.WorldState;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/**
 * Class representing a character, PC or NPC.
 *
 * @author Matt
 */
public abstract class Individual extends Container {
	private static final long serialVersionUID = 2821835360311044658L;

	/** Identifier of this character */
	public IndividualIdentifier id;

	/** State of this character */
	public IndividualState state;

	/** The AI responsible for this character */
	public ArtificialIntelligence ai;

	/** Whether or not this character is able to be controlled by the player */
	public boolean controllable;

	/** Whether or not this character is currently selected */
	public boolean selected;

	/** Which actions are currently active */
	protected HashMap<String, Boolean> commands = new HashMap<String, Boolean>();

	/** Width and Height of the individual */
	public int width, height;
	
	/** The box definining the region where this {@link Individual} can interact with entities */
	public Box interactionBox;

	/** For animation frame timing */
	protected float animationTimer;

	/** Time between sending AI to be processed by the AI processing thread */
	private final float aITaskDelay;

	/** The 'reaction' time of the AI instance controlling this {@link Individual} */
	private float aiReactionTimer;

	/** Coordinates of the tile to jump off (ignored by ground detection) */
	private Vector2 jumpOff = null;

	/** Used for platform jump-off processing */
	private boolean jumpedOff = false;

	/** Height at which it's deemed unsafe to fall to the ground */
	public int safetyHeight;
	
	/** True if this {@link Individual} is walking */
	public boolean walking = true;

	/**
	 * Constructor
	 */
	protected Individual(IndividualIdentifier id, IndividualState state, boolean controllable, float aiDelay, float inventoryMassCapacity, int width, int height, int safetyHeight, Box interactionBox) {
		super(inventoryMassCapacity);
		this.id = id;
		this.state = state;
		this.controllable = controllable;
		this.aITaskDelay = aiDelay;
		this.width = width;
		this.height = height;
		this.safetyHeight = safetyHeight;
		this.interactionBox = interactionBox;
	}


	/**
	 * Setups up all individual resources
	 */
	public static void setup() {
		Elf.loadAnimations();
		Boar.loadAnimations();
	}


	/** Renders the character and any other sprites */
	public void render() {
		internalRender();
	}


	/** Renders any decorations for UI */
	public void renderArrows() {
		if (isSelected()) {
			Fortress.spriteBatch.setShader(Shaders.pass);
			Shaders.pass.setUniformMatrix("u_projTrans", Fortress.cam.combined);
			Fortress.spriteBatch.draw(UserInterface.currentArrow, state.position.x - 5, state.position.y + height);
		}
	}
	
	
	/** Select this {@link Individual} */
	public void select() {
		selected = true;
		ai.setToManual();
	}
	
	
	/** Deselect this {@link Individual} */
	public void deselect(boolean clearTask) {
		selected = false;
		ai.setToAuto(clearTask);
	}


	/** How old this {@link Individual} is */
	public int getAge() {
		int age = WorldState.currentEpoch.year - id.birthday.year;

		if (age == 0) {
			return 0;
		}

		if (WorldState.currentEpoch.monthOfYear < id.birthday.monthOfYear) {
			age--;
		} else if (WorldState.currentEpoch.monthOfYear == id.birthday.monthOfYear && WorldState.currentEpoch.dayOfMonth < id.birthday.dayOfMonth) {
			age--;
		}

		return age;
	}


	/** Clears all commands */
	public synchronized void clearCommands() {
		commands.clear();
	}


	/** Handles commands */
	public synchronized void sendCommand(int keyCode, boolean val) {
		commands.put(Integer.toString(keyCode), val);
	}


	public synchronized void setJumpOffToNull() {
		jumpOff = null;
		jumpedOff = false;
	}


	/**
	 * Updates the individual
	 */
	public void update(float delta) {
		
		// If chunk has not yet been loaded, do not update
		try {
			Topography.getTile(state.position, true);
		} catch (NullPointerException e) {
			return;
		}

		// Update interaction box location
		interactionBox.position.x = state.position.x;
		interactionBox.position.y = state.position.y + height / 2;

		aiReactionTimer += delta;
		if (aiReactionTimer >= aITaskDelay) {
			ai.update();
			aiReactionTimer = 0f;
		}
		
		animationTimer += delta;
		
		internalUpdate(delta);
		
		respondToCommands();
		
		kinetics(delta);
	}


	/** Is this individual the current one? */
	public boolean isSelected() {
		return GameWorld.selectedIndividuals.contains(this) && selected;
	}


	/**
	 * Handles standard kinematics
	 */
	protected void kinetics(float delta) {
		jumpOffLogic();

		//Calculate position
		state.position.add(state.velocity.cpy().mul(delta));

		//Calculate velocity based on acceleration, including gravity
		if (Math.abs((state.velocity.y - delta * GameWorld.GRAVITY) * delta) < Topography.TILE_SIZE/2) {
			state.velocity.y = state.velocity.y - delta * GameWorld.GRAVITY;
		} else {
			state.velocity.y = state.velocity.y + delta * GameWorld.GRAVITY;
		}
		state.velocity.add(state.acceleration.cpy().mul(delta));

		//Ground detection
		//If the position is not on an empty tile and is not a platform tile run the ground detection routine
		//If the position is on a platform tile and if the tile below current position is not an empty tile, run ground detection routine
		//If position below is a platform tile and the next waypoint is directly below current position, skip ground detection
		if (groundDetectionCriteriaMet()) {
			state.velocity.y = 0f;

			if (state.position.y >= 0f) {
				state.position.y = (int)state.position.y % Topography.TILE_SIZE == 0 ?
					(int)state.position.y / Topography.TILE_SIZE * Topography.TILE_SIZE :
					(int)state.position.y / Topography.TILE_SIZE * Topography.TILE_SIZE + Topography.TILE_SIZE;

			} else {
				state.position.y = (int)state.position.y / Topography.TILE_SIZE * Topography.TILE_SIZE;
			}
		} else if (state.position.y == 0f && !(Topography.getTile(state.position.x, state.position.y - 1, true) instanceof Tile.EmptyTile)) {
			state.velocity.y = 0f;
		} else {
			state.velocity.x = state.velocity.x * 0.9f;
		}

		//Wall check routine, only perform this if we're moving
		if (state.velocity.x != 0 && obstructed(0)) {
			if (canStepUp(0)) {
				state.position.y = state.position.y + Topography.TILE_SIZE;
			} else {
				boolean check = false;
				while (obstructed(0)) {
					if (state.velocity.x > 0) {
 						state.position.x = state.position.x - 1;
					} else {
						state.position.x = state.position.x + 1;
					}
					check = true;
				}
				if (check) {
					state.velocity.x = 0;
					ai.setCurrentTask(new Idle());
				}
			}
		}
	}


	/**
	 * Whether we should be running ground detection
	 */
	protected boolean groundDetectionCriteriaMet() {
		Tile currentTile = Topography.getTile(state.position.x, state.position.y, true);
		Tile tileBelow = Topography.getTile(state.position.x, state.position.y - Topography.TILE_SIZE/2, true);
		return (!(currentTile instanceof Tile.EmptyTile) && !currentTile.isPlatformTile || currentTile.isPlatformTile && !(tileBelow instanceof EmptyTile)) &&
			     !isToBeIgnored(state.position);
	}


	/**
	 * Sets {@link #jumpOff} to null if we've passed it
	 */
	private void jumpOffLogic() {
		AITask currentTask = ai.getCurrentTask();
		if (currentTask instanceof GoToLocation) {
			if (((GoToLocation) currentTask).isAboveNext(state.position)) {
				jumpOff();
				jumpedOff = false;
			}
		}

		if (jumpOff != null) {
			if (jumpedOff && !Topography.convertToWorldCoord(state.position, false).equals(jumpOff)) {
				jumpedOff = false;
				jumpOff = null;
			} else if (Math.abs(Topography.convertToWorldCoord(state.position, false).cpy().sub(jumpOff).len()) > 2 * Topography.TILE_SIZE) {
				jumpedOff = true;
			}
		}
	}


	/**
	 * @return true if {@link Tile} at location is to be ignored, according to {@link #jumpOff}
	 */
	private boolean isToBeIgnored(Vector2 location) {
		if (jumpOff != null) {
			return Topography.convertToWorldCoord(location, false).equals(jumpOff) || Topography.convertToWorldCoord(location.x, location.y - 1, false).equals(jumpOff);
		}
		return false;
	}


	/**
	 * Jump off the tile this {@link Individual} is currently standing on, as long as its a platform
	 */
	public void jumpOff() {
		if (Topography.getTile(state.position.x, state.position.y - Topography.TILE_SIZE/2, true).isPlatformTile) {
			jumpOff = Topography.convertToWorldCoord(state.position.x, state.position.y - Topography.TILE_SIZE/2, false);
		}
	}


	/**
	 * Determines during {@link #kinetics(float)} whether we can step up
	 */
	protected boolean canStepUp(int offsetX) {
		int blockspan = height/Topography.TILE_SIZE + (height % Topography.TILE_SIZE == 0 ? 0 : 1);

		for (int block = 1; block != blockspan + 1; block++) {
			if (!isPassable(state.position.x + offsetX, state.position.y + Topography.TILE_SIZE*block + Topography.TILE_SIZE/2)) {
				return false;
			}
		}
		return !isPassable(state.position.x + offsetX, state.position.y + Topography.TILE_SIZE/2);
	}


	/** Whether this {@link Individual} is obstructed by {@link Tile}s */
	protected boolean obstructed(int offsetX) {
		int blockspan = height/Topography.TILE_SIZE + (height % Topography.TILE_SIZE == 0 ? 0 : 1);
		for (int block = 0; block != blockspan; block++) {
			if (!isPassable(state.position.x + offsetX, state.position.y + Topography.TILE_SIZE/2 + Topography.TILE_SIZE * block)) {
				return true;
			}
		}
		return false;
	}


	/**
	 * True if a {@link Tile#isPassable()}, taking into account the path
	 */
	protected boolean isPassable(float x, float y) {
		AITask current = ai.getCurrentTask();
		Tile tile = Topography.getTile(x, y, true);

		if (Topography.convertToWorldCoord(x, y, false).equals(jumpOff)) {
			return true;
		}

		//If we're on an empty tile, its obviously passable
		if (tile instanceof EmptyTile) {
			return true;
		}

		//If we're on a platform and we're GoingToLocation, then check to see if the tile above is part of the path, if it is, then not passable, otherwise passable
		if (tile.isPlatformTile) {
			if (current instanceof GoToLocation) {
				return !((GoToLocation)current).isPartOfPath(new Vector2(x, y + Topography.TILE_SIZE));
			} else {
				return true;
			}
		}

		//By this point we're not empty, and we're not a platform, not passable
		return false;
	}


	/**
	 * A condition that applies to a character
	 *
	 * @author Matt
	 */
	public static interface Condition {

		/** Affect the character suffering from this condition */
		public void affect(Individual effected);

		/** Infect another character */
		public void infect(Individual infected);
	}


	/**
	 * @return true if a command is currently active
	 */
	public synchronized boolean isCommandActive(int keycode) {
		return commands.get(Integer.toString(keycode)) == null ? false : commands.get(Integer.toString(keycode));
	}


	/** Renders this character */
	protected abstract void internalRender();


	/** Updates this character */
	protected abstract void internalUpdate(float delta);


	/** Responds to commands */
	protected abstract void respondToCommands();


	/** True if mouse is over */
	public abstract boolean isMouseOver();


	/** Returns the tooltip text color */
	public abstract Color getToolTipTextColor();


	/** Constructs a implementation-specific {@link ContextMenu} */
	public abstract ContextMenu getContextMenu();
	
	/** Gets the description for this {@link Individual} */
	public abstract String getDescription();
	
	/** Updates the description for this {@link Individual} */
	public abstract void updateDescription(String updated);


	/**
	 * The current state of the character
	 *
	 * @author Matt
	 */
	public static class IndividualState implements Serializable {
		private static final long serialVersionUID = 3678630824613212498L;

		public float health, maxHealth;
		public Vector2 position;
		public Vector2 velocity;
		public Vector2 acceleration;
		public Set<Condition> currentConditions;

		/**
		 * Constructor
		 */
		public IndividualState(float health, float maxHealth) {
			this.health = health;
			this.maxHealth = maxHealth;
		}
	}


	/**
	 * Uniquely identifies a character.
	 *
	 * @author Matt
	 */
	public static class IndividualIdentifier implements Serializable {
		private static final long serialVersionUID = 468971814825676707L;

		public String firstName, lastName;
		public String nickName;
		public Epoch birthday;
		public int id;

		/**
		 * Constructor
		 */
		public IndividualIdentifier(String firstName, String lastName, Epoch birthday) {
			this.firstName = firstName;
			this.lastName = lastName;
			this.birthday = birthday;
			this.id = ParameterPersistenceService.getParameters().getNextIndividualId();
		}


		/** Gets the simple first name + last name representation of this {@link IndividualIdentifier} */
		public String getSimpleName() {
			if (lastName.equals("")) {
				return firstName;
			}
			return firstName + " " + lastName;
		}
	}
}