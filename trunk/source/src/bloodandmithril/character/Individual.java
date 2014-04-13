package bloodandmithril.character;

import static bloodandmithril.core.BloodAndMithrilClient.HEIGHT;
import static bloodandmithril.core.BloodAndMithrilClient.WIDTH;
import static bloodandmithril.core.BloodAndMithrilClient.cam;
import static bloodandmithril.core.BloodAndMithrilClient.controlledFactions;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.csi.ClientServerInterface.isServer;
import static bloodandmithril.persistence.ParameterPersistenceService.getParameters;
import static bloodandmithril.world.WorldState.currentEpoch;
import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static bloodandmithril.world.topography.Topography.convertToWorldCoord;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ArtificialIntelligence;
import bloodandmithril.character.ai.task.Attack;
import bloodandmithril.character.ai.task.GoToLocation;
import bloodandmithril.character.ai.task.Idle;
import bloodandmithril.character.ai.task.TradeWith;
import bloodandmithril.character.individuals.Boar;
import bloodandmithril.character.individuals.Elf;
import bloodandmithril.character.skill.Skills;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.item.Container;
import bloodandmithril.item.Equipable;
import bloodandmithril.item.Equipper;
import bloodandmithril.item.EquipperImpl;
import bloodandmithril.item.Item;
import bloodandmithril.item.equipment.OneHandedWeapon;
import bloodandmithril.item.equipment.Weapon;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.IndividualInfoWindow;
import bloodandmithril.ui.components.window.IndividualStatusWindow;
import bloodandmithril.ui.components.window.InventoryWindow;
import bloodandmithril.ui.components.window.SelectedIndividualsControlWindow;
import bloodandmithril.ui.components.window.TextInputWindow;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.SpacialConfiguration;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.util.datastructure.Commands;
import bloodandmithril.world.Domain;
import bloodandmithril.world.Epoch;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Sets;

/**
 * Class representing a character, PC or NPC.
 *
 * @author Matt
 */
public abstract class Individual implements Equipper, Serializable {
	private static final long serialVersionUID = 2821835360311044658L;

	/** Timestamp, used for synchronizing with server */
	private long timeStamp;

	/** Identifier of this character */
	private IndividualIdentifier id;

	/** {@link World} id of this {@link Individual} */
	private int worldId;

	/** State of this character */
	private IndividualState state;

	/** Which actions are currently active */
	private Commands activeCommands = new Commands();

	/** The AI responsible for this character */
	protected ArtificialIntelligence ai;

	/** The faction this {@link Individual} belongs to */
	protected int factionId;

	/** Width and Height of the individual */
	private int width, height;

	/** The box defining the region where this {@link Individual} can interact with entities */
	private Box interactionBox;

	/** For animation frame timing */
	protected float animationTimer;

	/** Time between sending AI to be processed by the AI processing thread */
	private float aITaskDelay;

	/** The 'reaction' time of the AI instance controlling this {@link Individual} */
	private float aiReactionTimer;

	/** Coordinates of the tile to jump off (ignored by ground detection) */
	private Vector2 jumpOff = null;

	/** Used for platform jump-off processing */
	private boolean jumpedOff = false;

	/** Height at which it's deemed unsafe to fall to the ground */
	private int safetyHeight;

	/** True if this {@link Individual} is walking */
	private boolean walking = true;

	/** True if this {@link Individual} is currently stepping up */
	private boolean steppingUp;

	/** Part of the step-up processing */
	private int steps = 0;

	/** {@link Skills}s of this {@link Individual} */
	private Skills skills = new Skills();

	/** Whether or not this {@link Individual} is attacking */
	//TODO reset when attack animation finishes
	private boolean attacking;

	/** WHich client number this {@link Individual} is selected by */
	private Set<Integer> selectedByClient = Sets.newHashSet();

	/** Holds state of the equipment and inventory */
	private EquipperImpl equipper;

	/**
	 * Constructor
	 */
	protected Individual(
			IndividualIdentifier id,
			IndividualState state,
			int factionId,
			float aiDelay,
			float inventoryMassCapacity,
			int width,
			int height,
			int safetyHeight,
			Box interactionBox,
			int worldId) {
		this.equipper = new EquipperImpl(inventoryMassCapacity);
		this.id = id;
		this.state = state;
		this.factionId = factionId;
		this.aITaskDelay = aiDelay;
		this.width = width;
		this.height = height;
		this.safetyHeight = safetyHeight;
		this.interactionBox = interactionBox;
		this.setWorldId(worldId);
	}


	/**
	 * Copies all fields onto this individual from another
	 */
	public synchronized void copyFrom(Individual other) {
		this.ai = other.ai;
		this.setWorldId(other.getWorldId());
		this.selectedByClient = other.selectedByClient;
		this.aiReactionTimer = other.aiReactionTimer;
		this.aITaskDelay = other.aITaskDelay;
		this.animationTimer = other.animationTimer;
		this.activeCommands = other.activeCommands;
		this.factionId = other.factionId;
		this.height =  other.getHeight();
		this.id = other.id;
		this.interactionBox = other.getInteractionBox();
		this.jumpedOff = other.jumpedOff;
		this.safetyHeight = other.safetyHeight;
		this.state = other.state;
		this.steppingUp = other.steppingUp;
		this.steps = other.steps;
		this.walking = other.walking;
		this.width = other.width;
		this.timeStamp = other.timeStamp;
		this.selectedByClient = other.selectedByClient;
		this.skills = other.skills;
		synchronizeContainer(other.equipper);
		synchronizeEquipper(other.equipper);

		internalCopyFrom(other);
	}


	/** Implementation-specific copy method of this {@link Individual} */
	public abstract Individual copy();


	/** Returns the {@link ArtificialIntelligence} implementation of this {@link Individual} */
	public ArtificialIntelligence getAI() {
		return ai;
	}

	/** Returns the {@link IndividualState} of this {@link Individual} */
	public IndividualState getState() {
		return state;
	}

	/** Returns the {@link Skills} of this {@link Individual} */
	public Skills getSkills() {
		return skills;
	}

	/** The {@link #timeStamp} is used for client-server synchronization, if the received timeStamp is older than the current, it will be rejected */
	public synchronized long getTimeStamp() {
		return timeStamp;
	}

	/** See {@link #getTimeStamp()} */
	public synchronized void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	/** True if this {@link Individual} is currently {@link #walking} */
	public boolean isWalking() {
		return walking;
	}

	/** See {@link #isWalking()} */
	public synchronized void setWalking(boolean walking) {
		this.walking = walking;
	}

	/** See {@link #copy()} */
	protected abstract void internalCopyFrom(Individual other);


	/** Determines whether this {@link Individual} is controllable */
	public boolean isControllable() {
		return controlledFactions.contains(factionId);
	}


	/** Setups up all individual resources */
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
			spriteBatch.setShader(Shaders.filter);

			Shaders.filter.setUniformf("color",
				(float)sin(PI * (1f - state.health/state.maxHealth) / 2),
				(float)cos(PI * (1f - state.health/state.maxHealth) / 2),
				0f,
				1f
			);

			Shaders.filter.setUniformMatrix("u_projTrans", cam.combined);
			spriteBatch.draw(UserInterface.currentArrow, state.position.x - 5, state.position.y + getHeight());
		}
	}


	/** Select this {@link Individual} */
	public void select(int clientId) {
		ai.setToManual();
		selectedByClient.add(clientId);

		if (ClientServerInterface.isClient()) {
			UserInterface.addLayeredComponentUnique(
				new SelectedIndividualsControlWindow(
					BloodAndMithrilClient.WIDTH - 170,
					300,
					150,
					250,
					"Actions",
					true
				)
			);
		}
	}


	/** Deselect this {@link Individual} */
	public void deselect(boolean clearTask, int id) {
		selectedByClient.remove(id);

		if (selectedByClient.isEmpty()) {
			ai.setToAuto(clearTask);
		}
	}


	/** How old this {@link Individual} is */
	public int getAge() {
		int age = currentEpoch.year - id.birthday.year;

		if (age == 0) {
			return 0;
		}

		if (currentEpoch.monthOfYear < id.birthday.monthOfYear) {
			age--;
		} else if (currentEpoch.monthOfYear == id.birthday.monthOfYear && currentEpoch.dayOfMonth < id.birthday.dayOfMonth) {
			age--;
		}

		return age;
	}


	/** Clears all commands */
	public synchronized void clearCommands() {
		activeCommands.clear();
	}


	/** Handles commands */
	public synchronized void sendCommand(int keyCode, boolean val) {
		if (val) {
			activeCommands.activate(Integer.toString(keyCode));
		} else {
			activeCommands.deactivate(Integer.toString(keyCode));
		}
	}


	/**
	 * No longer jumping off, set {@link #jumpOff} to null, to signify that this individual is not trying to jump off any tile
	 */
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
			Domain.getWorld(worldId).getTopography().getTile(state.position, true);
		} catch (NullPointerException e) {
			return;
		}

		// Update interaction box location
		getInteractionBox().position.x = state.position.x;
		getInteractionBox().position.y = state.position.y + getHeight() / 2;

		aiReactionTimer += delta;
		if (aiReactionTimer >= aITaskDelay) {
			ai.update(delta);
			aiReactionTimer = 0f;
		}

		animationTimer += delta;

		internalUpdate(delta);

		respondToCommands();

		kinetics(delta, Domain.getWorld(getWorldId()));

		if (isServer()) {
			conditions(delta);
		}
	}


	/**
	 * Update how this {@link Individual} is affected by its {@link Condition}s
	 */
	private void conditions(float delta) {
		for (Condition condition : newArrayList(state.currentConditions)) {
			if (condition.isExpired()) {
				condition.uponExpiry();
				state.currentConditions.remove(condition);
			} else {
				condition.affect(this, delta);
			}
		}
	}


	/** Is this individual the current one? */
	public boolean isSelected() {
		return Domain.getSelectedIndividuals().contains(this);
	}


	/**
	 * Handles standard kinematics
	 */
	protected void kinetics(float delta, World world) {
		Topography topography = Domain.getWorld(getWorldId()).getTopography();
		jumpOffLogic(topography);

		//Stepping up
		if (steppingUp) {
			if (steps >= TILE_SIZE) {
				steppingUp = false;
				state.position.y += TILE_SIZE - steps;
			} else {
				state.position.y = state.position.y + 3f;
				steps += 3f;
			}
		}

		//Calculate position
		state.position.add(state.velocity.cpy().mul(delta));

		//Calculate velocity based on acceleration, including gravity
		if (abs((state.velocity.y - world.getGravity() * delta) * delta) < TILE_SIZE/2) {
			state.velocity.y = state.velocity.y - (steppingUp ? 0 : delta * world.getGravity());
		} else {
			state.velocity.y = state.velocity.y * 0.8f;
		}
		state.velocity.add(state.acceleration.cpy().mul(delta));

		//Ground detection
		//If the position is not on an empty tile and is not a platform tile run the ground detection routine
		//If the position is on a platform tile and if the tile below current position is not an empty tile, run ground detection routine
		//If position below is a platform tile and the next waypoint is directly below current position, skip ground detection
		if (groundDetectionCriteriaMet(topography) && !steppingUp) {
			state.velocity.y = 0f;

			if (state.position.y >= 0f) {
				if ((int)state.position.y % TILE_SIZE == 0) {
					state.position.y = (int)state.position.y / TILE_SIZE * TILE_SIZE;
				} else {
					state.position.y = (int)state.position.y / TILE_SIZE * TILE_SIZE + TILE_SIZE;
				}
			} else {
				state.position.y = (int)state.position.y / TILE_SIZE * TILE_SIZE;
			}
		} else if (state.position.y == 0f && !(topography.getTile(state.position.x, state.position.y - 1, true) instanceof Tile.EmptyTile)) {
			state.velocity.y = 0f;
		} else {
			state.velocity.x = state.velocity.x * 0.9f;
		}

		//Wall check routine, only perform this if we're moving
		if (state.velocity.x != 0 && obstructed(0, topography)) {
			if (canStepUp(0, topography)) {
				if (!steppingUp) {
					steppingUp = true;
					steps = 0;
				}
			} else if (!steppingUp) {
				boolean check = false;
				while (obstructed(0, topography)) {
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
	protected boolean groundDetectionCriteriaMet(Topography topography) {
		Tile currentTile = topography.getTile(state.position.x, state.position.y, true);
		Tile tileBelow = topography.getTile(state.position.x, state.position.y - TILE_SIZE/2, true);
		return (!(currentTile instanceof Tile.EmptyTile) && !currentTile.isPlatformTile || currentTile.isPlatformTile && !(tileBelow instanceof EmptyTile)) &&
			    !isToBeIgnored(state.position);
	}


	/**
	 * Sets {@link #jumpOff} to null if we've passed it
	 */
	private void jumpOffLogic(Topography topography) {
		AITask currentTask = ai.getCurrentTask();
		if (currentTask instanceof GoToLocation) {
			if (((GoToLocation) currentTask).isAboveNext(state.position)) {
				jumpOff(topography);
				jumpedOff = false;
			}
		}

		if (jumpOff != null) {
			if (jumpedOff && !convertToWorldCoord(state.position, false).equals(jumpOff)) {
				jumpedOff = false;
				jumpOff = null;
			} else if (Math.abs(convertToWorldCoord(state.position, false).cpy().sub(jumpOff).len()) > 2 * TILE_SIZE) {
				jumpedOff = true;
			}
		}
	}


	/**
	 * @return true if {@link Tile} at location is to be ignored, according to {@link #jumpOff}
	 */
	private boolean isToBeIgnored(Vector2 location) {
		if (jumpOff != null) {
			return convertToWorldCoord(location, false).equals(jumpOff) || convertToWorldCoord(location.x, location.y - 1, false).equals(jumpOff);
		}
		return false;
	}


	/**
	 * Jump off the tile this {@link Individual} is currently standing on, as long as its a platform
	 */
	public void jumpOff(Topography topography) {
		if (topography.getTile(state.position.x, state.position.y - TILE_SIZE/2, true).isPlatformTile) {
			jumpOff = convertToWorldCoord(state.position.x, state.position.y - TILE_SIZE/2, false);
		}
	}


	/**
	 * Determines during {@link #kinetics(float)} whether we can step up
	 */
	protected boolean canStepUp(int offsetX, Topography topography) {
		int blockspan = getHeight()/TILE_SIZE + (getHeight() % TILE_SIZE == 0 ? 0 : 1);

		for (int block = 1; block != blockspan + 1; block++) {
			if (!isPassable(state.position.x + offsetX, state.position.y + TILE_SIZE*block + TILE_SIZE/2, topography)) {
				return false;
			}
		}
		return !isPassable(state.position.x + offsetX, state.position.y + TILE_SIZE/2, topography);
	}


	/** Whether this {@link Individual} is obstructed by {@link Tile}s */
	protected boolean obstructed(int offsetX, Topography topography) {
		int blockspan = getHeight()/TILE_SIZE + (getHeight() % TILE_SIZE == 0 ? 0 : 1);
		for (int block = 0; block != blockspan; block++) {
			if (!isPassable(state.position.x + offsetX, state.position.y + TILE_SIZE/2 + TILE_SIZE * block, topography)) {
				return true;
			}
		}
		return false;
	}


	/** Calculates the distance between this individual and the Vector2 parameter */
	public float getDistanceFrom(Vector2 position) {
		try {
			return state.position.cpy().sub(position).len();
		} catch (Throwable a) {
			a.printStackTrace();
			throw new RuntimeException(a);
		}
	}


	/**
	 * True if a {@link Tile#isPassable()}, taking into account the path
	 */
	protected boolean isPassable(float x, float y, Topography topography) {
		AITask current = ai.getCurrentTask();
		Tile tile = topography.getTile(x, y, true);

		if (convertToWorldCoord(x, y, false).equals(jumpOff)) {
			return true;
		}

		//If we're on an empty tile it's obviously passable
		if (tile instanceof EmptyTile) {
			return true;
		}

		//If we're on a platform and we're GoingToLocation, then check to see if the tile above is part of the path, if it is, then not passable, otherwise passable
		if (tile.isPlatformTile) {
			if (current instanceof GoToLocation) {
				return !((GoToLocation)current).isPartOfPath(new Vector2(x, y + TILE_SIZE));
			} else {
				return true;
			}
		}

		//By this point we're not empty, and we're not a platform, not passable
		return false;
	}


	/** Constructs a {@link ContextMenu} */
	public ContextMenu getContextMenu() {
		ContextMenu contextMenuToReturn = new ContextMenu(0, 0);

		final Individual thisIndividual = this;

		MenuItem controlOrReleaseMenuItem = Domain.getSelectedIndividuals().contains(thisIndividual) ?
		new MenuItem(
			"Deselect",
			() -> {
				if (isServer()) {
					thisIndividual.deselect(false, 0);
					Domain.getSelectedIndividuals().remove(thisIndividual);
					clearCommands();
				} else {
					ClientServerInterface.SendRequest.sendIndividualSelectionRequest(thisIndividual.id.id, false);
				}
			},
			Color.WHITE,
			getToolTipTextColor(),
			Color.GRAY,
			null
		) :

		new MenuItem(
			"Select",
			() -> {
				if (isServer()) {
					Domain.getSelectedIndividuals().add(thisIndividual);
					thisIndividual.select(0);
				} else {
					ClientServerInterface.SendRequest.sendIndividualSelectionRequest(thisIndividual.id.id, true);
				}
			},
			Color.WHITE,
			getToolTipTextColor(),
			Color.GRAY,
			null
		);

		MenuItem showInfoMenuItem = new MenuItem(
			"Show info",
			() -> {
				IndividualInfoWindow individualInfoWindow = new IndividualInfoWindow(
					thisIndividual,
					WIDTH/2 - 150,
					HEIGHT/2 + 160,
					300,
					320,
					id.getSimpleName() + " - Info",
					true,
					250, 200
				);
				UserInterface.addLayeredComponentUnique(individualInfoWindow);
			},
			Color.WHITE,
			getToolTipTextColor(),
			Color.GRAY,
			null
		);


		final ContextMenu secondaryMenu = new ContextMenu(0, 0,
			new MenuItem(
				"Change nickname",
				() -> {
					UserInterface.addLayeredComponent(
						new TextInputWindow(
							WIDTH / 2 - 125,
							HEIGHT/2 + 50,
							250,
							100,
							"Change nickname",
							250,
							100,
							args -> {
								if (isServer()) {
									thisIndividual.id.nickName = args[0].toString();
								} else {
									ClientServerInterface.SendRequest.sendChangeNickNameRequest(thisIndividual.id.id, args[0].toString());
								}
							},
							"Confirm",
							true,
							thisIndividual.id.nickName
						)
					);
				},
				Color.WHITE,
				getToolTipTextColor(),
				Color.GRAY,
				null
			),
			new MenuItem(
				"Update biography",
				() -> {
					UserInterface.addLayeredComponent(
						new TextInputWindow(
							WIDTH / 2 - 125,
							HEIGHT/2 + 50,
							250,
							100,
							"Change biography",
							250,
							100,
							args -> {
								if (isServer()) {
									thisIndividual.updateDescription(args[0].toString());
								} else {
									// TODO
								}
							},
							"Confirm",
							true,
							thisIndividual.getDescription()
						)
					);
				},
				Color.WHITE,
				getToolTipTextColor(),
				Color.GRAY,
				null
			)
		);

		MenuItem editMenuItem = new MenuItem(
			"Edit",
			() -> {
				secondaryMenu.x = getMouseScreenX();
				secondaryMenu.y = getMouseScreenY();
			},
			Color.WHITE,
			getToolTipTextColor(),
			Color.GRAY,
			secondaryMenu
		);

		MenuItem inventoryMenuItem = new MenuItem(
			"Inventory",
			() -> {
				InventoryWindow inventoryWindow = new InventoryWindow(
					thisIndividual,
					WIDTH/2 - ((id.getSimpleName() + " - Inventory").length() * 10 + 50)/2,
					HEIGHT/2 + 150,
					(id.getSimpleName() + " - Inventory").length() * 10 + 50,
					300,
					id.getSimpleName() + " - Inventory",
					true,
					150, 300
				);
				UserInterface.addLayeredComponentUnique(inventoryWindow);
			},
			Color.WHITE,
			getToolTipTextColor(),
			Color.GRAY,
			null
		);

		MenuItem tradeMenuItem = new MenuItem(
			"Trade with",
			() -> {
				for (Individual indi : Domain.getSelectedIndividuals()) {
					if (isServer()) {
						if (indi != thisIndividual) {
							indi.ai.setCurrentTask(
								new TradeWith(indi, thisIndividual)
							);
						}
					} else {
						ClientServerInterface.SendRequest.sendTradeWithIndividualRequest(indi, thisIndividual);
					}
				}
			},
			Color.WHITE,
			getToolTipTextColor(),
			Color.GRAY,
			null
		);

		MenuItem showStatusWindowItem = new MenuItem(
			"Status",
			() -> {
				UserInterface.addLayeredComponent(
					new IndividualStatusWindow(
						thisIndividual,
						WIDTH/2 - 200,
						HEIGHT/2 + 200,
						400,
						400,
						id.getSimpleName() + " - Status",
						true
					)
				);
			},
			Color.WHITE,
			getToolTipTextColor(),
			Color.GRAY,
			null
		);

		MenuItem attackMenuItem = new MenuItem(
			"Attack",
			() -> {
				for (Individual indi : Domain.getSelectedIndividuals()) {
					if (isServer()) {
						if (indi != thisIndividual) {
							indi.ai.setCurrentTask(
								new Attack(indi, thisIndividual)
							);
						}
					} else {
						ClientServerInterface.SendRequest.sendAttackRequest(indi, thisIndividual);
					}
				}
			},
			Color.RED,
			getToolTipTextColor(),
			Color.GRAY,
			null
		);

		if (isControllable()) {
			contextMenuToReturn.addMenuItem(controlOrReleaseMenuItem);
		}

		if (!Domain.getSelectedIndividuals().isEmpty() &&
			!Domain.getSelectedIndividuals().contains(thisIndividual)) {
			contextMenuToReturn.addMenuItem(attackMenuItem);
		}

		contextMenuToReturn.addMenuItem(showInfoMenuItem);
		contextMenuToReturn.addMenuItem(showStatusWindowItem);

		if (isControllable()) {
			contextMenuToReturn.addMenuItem(inventoryMenuItem);
			contextMenuToReturn.addMenuItem(editMenuItem);
		}


		if (!Domain.getSelectedIndividuals().isEmpty() &&
			!(Domain.getSelectedIndividuals().size() == 1 && Domain.getSelectedIndividuals().contains(thisIndividual))) {
			contextMenuToReturn.addMenuItem(tradeMenuItem);

			if (Domain.getSelectedIndividuals().size() > 1) {
				final ContextMenu contextMenu = new ContextMenu(0, 0,
					new MenuItem(
						"You have multiple individuals selected",
						() -> {},
						Colors.UI_GRAY,
						Colors.UI_GRAY,
						Colors.UI_GRAY,
						null
					)
				);
				tradeMenuItem.menu = contextMenu;
				tradeMenuItem.button.setTask(() -> {
					contextMenu.x = getMouseScreenX();
					contextMenu.y = getMouseScreenY();
				});
				tradeMenuItem.button.setIdleColor(Colors.UI_GRAY);
				tradeMenuItem.button.setOverColor(Colors.UI_GRAY);
				tradeMenuItem.button.setDownColor(Colors.UI_GRAY);
			}
		}

		for (MenuItem item : internalGetContextMenuItems()) {
			contextMenuToReturn.addMenuItem(item);
		}

		return contextMenuToReturn;
	}


	/**
	 * A condition that applies to a character
	 *
	 * @author Matt
	 */
	public static abstract class Condition implements Comparable<Condition>, Serializable {
		private static final long serialVersionUID = -1125485475556985426L;

		/** Affect the character suffering from this condition */
		public abstract void affect(Individual affected, float delta);

		/** Infect another character */
		public abstract void infect(Individual infected, float delta);

		/** Whether this condition can be removed */
		public abstract boolean isExpired();

		/** Called when expired */
		public abstract void uponExpiry();

		/** Called when the condition is added to an individual who already has this condition */
		public abstract void stack(Condition condition);

		/** Whether this condition is detrimental to the individual */
		public abstract boolean isNegative();

		/** Gets the help text describing this condition */
		public abstract String getHelpText();

		/** The severity of this condition */
		public abstract String getName();

		@Override
		public int compareTo(Condition other) {
			return getClass().getSimpleName().compareTo(other.getClass().getSimpleName());
		}
	}


	/**
	 * @return true if a command is currently active
	 */
	public synchronized boolean isCommandActive(int keycode) {
		return activeCommands.isActive(Integer.toString(keycode));
	}


	public synchronized void decreaseThirst(float amount) {
		if (state.thirst - amount <= 0f) {
			state.thirst = 0f;
		} else {
			state.thirst = state.thirst - amount;
		}
	}


	public synchronized void increaseThirst(float amount) {
		if (state.thirst + amount > 1f) {
			state.thirst = 1f;
		} else {
			state.thirst = state.thirst + amount;
		}
	}


	public synchronized void damage(float amount) {
		if (state.health - amount <= 0f) {
			state.health = 0f;
		} else {
			state.health = state.health - amount;
		}
	}


	public synchronized void heal(float amount) {
		if (state.health + amount > state.maxHealth) {
			state.health = state.maxHealth;
		} else {
			state.health = state.health + amount;
		}
	}


	public synchronized void increaseHunger(float amount) {
		if (state.hunger + amount >= 1f) {
			state.hunger = 1f;
		} else {
			state.hunger = state.hunger + amount;
		}
	}


	public synchronized void decreaseHunger(float amount) {
		if (state.hunger - amount <= 0f) {
			state.hunger = 0f;
		} else {
			state.hunger = state.hunger - amount;
		}
	}


	public synchronized void increaseStamina(float amount) {
		if (state.stamina + amount >= 1f) {
			state.stamina = 1f;
		} else {
			state.stamina = state.stamina + amount;
		}
	}


	public synchronized void decreaseStamina(float amount) {
		if (state.stamina - amount <= 0f) {
			state.stamina = 0f;
		} else {
			state.stamina = state.stamina - amount;
		}
	}


	/**
	 * Add a {@link Condition} to this {@link Individual}, if there is already an existing {@link Condition}
	 * that is of the same class as the condition trying to be added, stack them by calling {@link Condition#stack(Condition)}
	 */
	public synchronized void addCondition(Condition condition) {
		for (Condition existing : newHashSet(state.currentConditions)) {
			if (condition.getClass().equals(existing.getClass())) {
				existing.stack(condition);
				return;
			}
		}
		state.currentConditions.add(condition);
	}


	public synchronized void changeStaminaRegen(float newValue) {
		state.staminaRegen = newValue;
	}


	public synchronized void changeHealthRegen(float newValue) {
		state.healthRegen = newValue;
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
	protected abstract List<MenuItem> internalGetContextMenuItems();


	/** Gets the description for this {@link Individual} */
	public abstract String getDescription();


	/** Updates the description for this {@link Individual} */
	public abstract void updateDescription(String updated);


	/** Returns the {@link SpacialConfiguration} where {@link OneHandedWeapon} will be rendered */
	protected abstract SpacialConfiguration getOneHandedWeaponSpacialConfigration();


	/** The attack range of this individual, dependent on the weapons and any other variables */
	public abstract float getCurrentAttackRange();


	/** Attack. */
	public void attack(Individual victim) {
		for (Item item : equipper.getEquipped().keySet()) {
			if (item instanceof Weapon) {
				((Weapon) item).affect(victim);
				attacking = true;
			}
		}
	}


	/**
	 * True if currently attacking
	 */
	public boolean isAttacking() {
		return attacking;
	}


	@Override
	public boolean unlock(Item with) {
		// Do nothing
		return false;
	}


	@Override
	public boolean lock(Item with) {
		// Do nothing
		return false;
	}


	@Override
	public int has(Item item) {
		return equipper.has(item);
	}


	@Override
	public boolean isLocked() {
		return false;
	}


	@Override
	public boolean isLockable() {
		return false;
	}


	@Override
	public void synchronizeContainer(Container other) {
		equipper.synchronizeContainer(other);
	}


	@Override
	public void giveItem(Item item) {
		equipper.giveItem(item);
	}


	@Override
	public int takeItem(Item item) {
		return equipper.takeItem(item);
	}


	@Override
	public Map<Item, Integer> getInventory() {
		return equipper.getInventory();
	}


	@Override
	public float getMaxCapacity() {
		return equipper.getMaxCapacity();
	}


	@Override
	public float getCurrentLoad() {
		return equipper.getCurrentLoad();
	}


	@Override
	public boolean canExceedCapacity() {
		return equipper.canExceedCapacity();
	}


	@Override
	public Map<EquipmentSlot, Boolean> getAvailableEquipmentSlots() {
		return equipper.getAvailableEquipmentSlots();
	}


	@Override
	public HashMap<Item, Integer> getEquipped() {
		return equipper.getEquipped();
	}


	@Override
	public void equip(Equipable item) {
		equipper.equip(item);
	}


	@Override
	public void unequip(Equipable item) {
		equipper.unequip(item);
	}


	@Override
	public void synchronizeEquipper(Equipper other) {
		equipper.synchronizeEquipper(other);
	}


	/**
	 * The current state of the character
	 *
	 * @author Matt
	 */
	public static class IndividualState implements Serializable {
		private static final long serialVersionUID = 3678630824613212498L;

		public float health, maxHealth, healthRegen, stamina, staminaRegen, hunger, thirst;
		public Vector2 position;
		public Vector2 velocity;
		public Vector2 acceleration;
		public Set<Condition> currentConditions = newHashSet();

		/**
		 * Constructor
		 */
		public IndividualState(float health, float maxHealth, float healthRegen, float stamina, float staminaRegen, float hunger, float thirst) {
			this.health = health;
			this.maxHealth = maxHealth;
			this.healthRegen = healthRegen;
			this.stamina = stamina;
			this.staminaRegen = staminaRegen;
			this.hunger = hunger;
			this.thirst = thirst;
		}
	}


	/**
	 * Returns the {@link IndividualIdentifier} of this {@link Individual}
	 */
	public IndividualIdentifier getId() {
		return id;
	}


	/**
	 * Returns a {@link HashSet} containing client ID's of all clients that have this
	 * {@link Individual} selected
	 */
	public Set<Integer> getSelectedByClient() {
		return selectedByClient;
	}


	public int getSafetyHeight() {
		return safetyHeight;
	}


	public Box getInteractionBox() {
		return interactionBox;
	}


	public int getWidth() {
		return width;
	}


	public int getHeight() {
		return height;
	}


	public int getWorldId() {
		return worldId;
	}


	public void setWorldId(int worldId) {
		this.worldId = worldId;
	}


	/**
	 * Uniquely identifies a character.
	 *
	 * @author Matt
	 */
	public static class IndividualIdentifier implements Serializable {
		private static final long serialVersionUID = 468971814825676707L;

		private final String firstName, lastName;
		private String nickName;
		private final Epoch birthday;
		private final int id;

		/**
		 * Constructor
		 */
		public IndividualIdentifier(String firstName, String lastName, Epoch birthday) {
			this.firstName = firstName;
			this.lastName = lastName;
			this.birthday = birthday;
			this.id = getParameters().getNextIndividualId();
		}


		/** Gets the simple first name + last name representation of this {@link IndividualIdentifier} */
		public String getSimpleName() {
			if (getLastName().equals("")) {
				return getFirstName();
			}
			return getFirstName() + " " + getLastName();
		}


		public int getId() {
			return id;
		}


		public String getNickName() {
			return nickName;
		}


		public void setNickName(String nickName) {
			this.nickName = nickName;
		}


		public String getFirstName() {
			return firstName;
		}


		public String getLastName() {
			return lastName;
		}
	}
}