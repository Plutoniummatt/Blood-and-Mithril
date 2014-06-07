package bloodandmithril.character.individuals;

import static bloodandmithril.character.individuals.Individual.Action.STAND_LEFT;
import static bloodandmithril.core.BloodAndMithrilClient.HEIGHT;
import static bloodandmithril.core.BloodAndMithrilClient.WIDTH;
import static bloodandmithril.core.BloodAndMithrilClient.controlledFactions;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.csi.ClientServerInterface.isServer;
import static bloodandmithril.ui.UserInterface.shapeRenderer;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.round;
import static java.lang.Math.sin;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bloodandmithril.character.ai.ArtificialIntelligence;
import bloodandmithril.character.ai.task.TradeWith;
import bloodandmithril.character.conditions.Condition;
import bloodandmithril.character.skill.Skills;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.item.items.equipment.Equipper;
import bloodandmithril.item.items.equipment.EquipperImpl;
import bloodandmithril.item.items.equipment.weapon.OneHandedWeapon;
import bloodandmithril.item.items.equipment.weapon.Weapon;
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
import bloodandmithril.util.Task;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.util.datastructure.Commands;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * Class representing a character, PC or NPC.
 *
 * @author Matt
 */
public abstract class Individual implements Equipper, Serializable, Kinematics {
	private static final long serialVersionUID = 2821835360311044658L;

	public enum Action implements Serializable {
		STAND_LEFT(true),
		STAND_RIGHT(false),
		WALK_LEFT(true),
		WALK_RIGHT(false),
		RUN_LEFT(true),
		RUN_RIGHT(false),
		ATTACK_LEFT_UNARMED(true),
		ATTACK_RIGHT_UNARMED(false),
		ATTACK_LEFT_ONE_HANDED_WEAPON(true),
		ATTACK_RIGHT_ONE_HANDED_WEAPON(false),
		ATTACK_LEFT_TWO_HANDED_WEAPON(true),
		ATTACK_RIGHT_TWO_HANDED_WEAPON(false),
		ATTACK_LEFT_SPEAR(true),
		ATTACK_RIGHT_SPEAR(false);

		private boolean flipXAnimation;

		private Action(boolean flipXAnimation) {
			this.flipXAnimation = flipXAnimation;
		}

		public boolean flipXAnimation() {
			return flipXAnimation;
		}
	}

	/** The current action of this individual */
	protected Action currentAction = STAND_LEFT;

	/** Which client number this {@link Individual} is selected by */
	private Set<Integer> selectedByClient = Sets.newHashSet();

	/** {@link Skills}s of this {@link Individual} */
	private Skills skills = new Skills();

	/** Identifier of this character */
	private IndividualIdentifier id;

	/** State of this character */
	private IndividualState state;

	/** Which actions are currently active */
	private Commands activeCommands = new Commands();

	/** The AI responsible for this character */
	protected ArtificialIntelligence ai;

	/** Holds state of the equipment and inventory */
	private EquipperImpl equipperImpl;

	/** Data used for {@link Kinematics} */
	private IndividualKineticsProcessingData kinematicsData = new IndividualKineticsProcessingData();

	/** The defined area such that the individual can interact with objects */
	private Box interactionBox;

	/** The hitbox defining the region where this {@link Individual} can be hit */
	private Box hitBox;

	/** The set of {@link Individual}s currently being attacked by this {@link Individual} */
	private Set<Integer> individualsToBeAttacked;

	/** For animation frame timing */
	protected float animationTimer;

	/** The 'reaction' time of the AI instance controlling this {@link Individual} */
	private float aiReactionTimer;

	/** Whether this {@link Individual} is in combat stance */
	private boolean combatStance;

	/** True if this {@link Individual} is walking */
	private boolean walking = true;

	/** Height at which it's deemed unsafe to fall to the ground */
	private int safetyHeight;

	/** Timestamp, used for synchronizing with server */
	private long timeStamp;

	/** World ID, indicating which {@link World} this {@link Individual} exists on */
	private int worldId;

	/** The faction this {@link Individual} belongs to */
	protected int factionId;


	/**
	 * Constructor
	 */
	protected Individual(
			IndividualIdentifier id,
			IndividualState state,
			int factionId,
			float inventoryMassCapacity,
			int maxRings,
			int width,
			int height,
			int safetyHeight,
			Box interactionBox,
			int worldId,
			int maximumConcurrentMeleeAttackers) {
		this.equipperImpl = new EquipperImpl(inventoryMassCapacity, maxRings);
		this.id = id;
		this.state = state;
		this.factionId = factionId;
		this.safetyHeight = safetyHeight;
		this.interactionBox = interactionBox;
		this.setWorldId(worldId);
		this.hitBox = new Box(new Vector2(state.position), width, height);
	}


	/**
	 * Copies all fields onto this individual from another
	 */
	public synchronized void copyFrom(Individual other) {
		this.ai = other.ai;
		this.setWorldId(other.getWorldId());
		this.selectedByClient = other.selectedByClient;
		this.individualsToBeAttacked = other.individualsToBeAttacked;
		this.aiReactionTimer = other.aiReactionTimer;
		this.animationTimer = other.animationTimer;
		this.activeCommands = other.activeCommands;
		this.factionId = other.factionId;
		this.id = other.id;
		this.interactionBox = other.getInteractionBox();
		this.kinematicsData = other.getKinematicsData();
		this.safetyHeight = other.safetyHeight;
		this.state = other.state;
		this.walking = other.walking;
		this.timeStamp = other.timeStamp;
		this.selectedByClient = other.selectedByClient;
		this.hitBox = other.hitBox;
		this.skills = other.skills;
		this.currentAction = other.currentAction;
		this.combatStance = other.combatStance;
		synchronizeContainer(other.equipperImpl);
		synchronizeEquipper(other.equipperImpl);

		internalCopyFrom(other);
	}


	/** Attacks a set of other {@link Individual}s, if the set is empty, it will hit everything */
	@SuppressWarnings("rawtypes")
	public synchronized void attack(Set<Integer> individuals) {
		this.individualsToBeAttacked = individuals;
		animationTimer = 0f;

		Optional<Item> weapon = Iterables.tryFind(getEquipped().keySet(), equipped -> {
			return equipped instanceof Weapon;
		});

		if (weapon.isPresent()) {
			if (currentAction.flipXAnimation) {
				currentAction = ((Weapon) weapon.get()).getAttackAction(false);
			} else {
				currentAction = ((Weapon) weapon.get()).getAttackAction(true);
			}
		}

		this.individualsToBeAttacked.clear();
		this.individualsToBeAttacked.addAll(individuals);
	}


	/** Called during the update routine when the currentAction is attacking */
	protected abstract void respondToAttackCommand();


	/** Returns the map that maps from an {@link Action} to a map that maps action frames to their respective {@link Task}s */
	protected abstract Map<Action, Map<Integer, Task>> getActionFrames();


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
	}


	/** Renders the character and any other sprites */
	public void render() {
		internalRender();
	}


	/** Renders any decorations for UI */
	public void renderUIDecorations() {
		if (isSelected()) {
			spriteBatch.setShader(Shaders.filter);

			Shaders.filter.setUniformf("color",
				(float)sin(PI * (1f - state.health/state.maxHealth) / 2),
				(float)cos(PI * (1f - state.health/state.maxHealth) / 2),
				0f,
				1f
			);

			Shaders.filter.setUniformMatrix("u_projTrans", UserInterface.UICameraTrackingCam.combined);
			spriteBatch.draw(UserInterface.currentArrow, state.position.x - 5, state.position.y + getHeight() + 10);
		}

		if (UserInterface.DEBUG) {
			shapeRenderer.begin(ShapeType.Rectangle);
			shapeRenderer.setProjectionMatrix(UserInterface.UICameraTrackingCam.combined);
			shapeRenderer.setColor(Color.ORANGE);
			shapeRenderer.rect(
				interactionBox.position.x - interactionBox.width / 2,
				interactionBox.position.y - interactionBox.height / 2,
				interactionBox.width,
				interactionBox.height
			);
			shapeRenderer.setColor(Color.RED);
			shapeRenderer.rect(
				hitBox.position.x - hitBox.width / 2,
				hitBox.position.y - hitBox.height / 2,
				hitBox.width,
				hitBox.height
			);
			shapeRenderer.end();
			shapeRenderer.setProjectionMatrix(UserInterface.UICamera.combined);
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
					150,
					150,
					100,
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
		getKinematicsData().jumpOff = null;
		getKinematicsData().jumpedOff = false;
	}


	/**
	 * Updates the individual
	 */
	public void update(float delta) {
		float aiTaskDelay = 0.05f;

		// If chunk has not yet been loaded, do not update
		try {
			Domain.getWorld(worldId).getTopography().getTile(state.position, true);
		} catch (NullPointerException e) {
			return;
		}

		// Update interaction box location
		interactionBox.position.x = state.position.x;
		interactionBox.position.y = state.position.y + getHeight() / 2;

		// Update hitbox location
		hitBox.position.x = state.position.x;
		hitBox.position.y = state.position.y + getHeight() / 2;

		aiReactionTimer += delta;
		if (aiReactionTimer >= aiTaskDelay) {
			ai.update(aiTaskDelay);
			aiReactionTimer = 0f;
		}

		animationTimer += delta;

		internalUpdate(delta);

		respondToCommands();
		respondToAttackCommand();

		Kinematics.kinetics(delta, Domain.getWorld(getWorldId()), this);

		if (isServer()) {
			updateConditions(delta);
		}
	}


	/**
	 * Update how this {@link Individual} is affected by its {@link Condition}s
	 */
	private void updateConditions(float delta) {
		// Reset regeneration values
		state.reset();

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


	/** Calculates the distance between this individual and the Vector2 parameter */
	public float getDistanceFrom(Vector2 position) {
		try {
			return state.position.cpy().sub(position).len();
		} catch (Throwable a) {
			a.printStackTrace();
			throw new RuntimeException(a);
		}
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
					ClientServerInterface.SendRequest.sendIndividualSelectionRequest(thisIndividual.id.getId(), false);
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
					ClientServerInterface.SendRequest.sendIndividualSelectionRequest(thisIndividual.id.getId(), true);
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

		MenuItem combatMode = new MenuItem(
			"Combat Mode",
				() -> {
					thisIndividual.combatStance = !thisIndividual.inCombatStance();
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
									thisIndividual.id.setNickName(args[0].toString());
								} else {
									ClientServerInterface.SendRequest.sendChangeNickNameRequest(thisIndividual.id.getId(), args[0].toString());
								}
							},
							"Confirm",
							true,
							thisIndividual.id.getNickName()
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
									ClientServerInterface.SendRequest.sendUpdateBiographyRequest(thisIndividual, args[0].toString());
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
				UserInterface.addLayeredComponentUnique(
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

		if (isControllable()) {
			contextMenuToReturn.addMenuItem(controlOrReleaseMenuItem);
			contextMenuToReturn.addMenuItem(combatMode);
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


	public synchronized void increaseMana(float amount) {
		if (state.mana + amount >= state.maxMana) {
			state.mana = state.maxMana;
		} else {
			state.mana = state.hunger + amount;
		}
	}


	public synchronized void decreaseMana(float amount) {
		if (state.mana - amount <= 0f) {
			state.mana = 0f;
		} else {
			state.mana = state.mana - amount;
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


	/** Returns the tooltip text color */
	public abstract Color getToolTipTextColor();


	/** Constructs a implementation-specific {@link ContextMenu} */
	protected abstract List<MenuItem> internalGetContextMenuItems();


	/** Gets the description for this {@link Individual} */
	public abstract String getDescription();


	/** Updates the description for this {@link Individual} */
	public abstract void updateDescription(String updated);


	/** Returns the {@link SpacialConfiguration} where {@link OneHandedWeapon} will be rendered */
	protected abstract SpacialConfiguration getOneHandedWeaponSpatialConfigration();


	/** True if mouse is over */
	public boolean isMouseOver() {
		float x = BloodAndMithrilClient.getMouseWorldX();
		float y = BloodAndMithrilClient.getMouseWorldY();

		boolean ans = x >= getState().position.x - getWidth()/2 && x <= getState().position.x + getWidth()/2 && y >= getState().position.y && y <= getState().position.y + getHeight();
		return ans;
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
	public boolean isLocked() {
		return false;
	}


	@Override
	public boolean isLockable() {
		return false;
	}


	@Override
	public Container getContainerImpl() {
		return equipperImpl;
	}


	@Override
	public Equipper getEquipperImpl() {
		return equipperImpl;
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


	/**
	 * Height at which it's deemed unsafe to fall to the ground
	 */
	public int getSafetyHeight() {
		return safetyHeight;
	}


	/**
	 * The box defining the region where this {@link Individual} can interact with entities
	 */
	public Box getInteractionBox() {
		return interactionBox;
	}


	/**
	 * Width of the individual
	 */
	public int getWidth() {
		return round(hitBox.width);
	}


	/**
	 * Height of the individual
	 */
	public int getHeight() {
		return round(hitBox.height);
	}


	/**
	 * {@link World} id of this {@link Individual}
	 */
	public int getWorldId() {
		return worldId;
	}


	/**
	 * See {@link #getWorldId()}
	 */
	public void setWorldId(int worldId) {
		this.worldId = worldId;
	}


	/**
	 * The kinematics data, containing things needed for kinematics processing
	 */
	public IndividualKineticsProcessingData getKinematicsData() {
		return kinematicsData;
	}


	/**
	 * @return Whether or not this {@link Individual} is in combat stance
	 */
	public boolean inCombatStance() {
		return combatStance;
	}
}