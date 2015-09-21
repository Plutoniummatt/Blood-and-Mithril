package bloodandmithril.character.individuals;

import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_ONE_HANDED_WEAPON;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_ONE_HANDED_WEAPON_MINE;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_ONE_HANDED_WEAPON_STAB;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_SPEAR;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_TWO_HANDED_WEAPON;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_UNARMED;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_ONE_HANDED_WEAPON;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_ONE_HANDED_WEAPON_MINE;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_ONE_HANDED_WEAPON_STAB;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_SPEAR;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_TWO_HANDED_WEAPON;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_UNARMED;
import static bloodandmithril.character.individuals.Individual.Action.STAND_LEFT;
import static bloodandmithril.core.BloodAndMithrilClient.controlledFactions;
import static bloodandmithril.core.BloodAndMithrilClient.getGraphics;
import static bloodandmithril.core.BloodAndMithrilClient.getKeyMappings;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;
import static bloodandmithril.core.BloodAndMithrilClient.worldToScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.worldToScreenY;
import static bloodandmithril.item.items.equipment.weapon.RangedWeapon.rangeControl;
import static bloodandmithril.ui.UserInterface.shapeRenderer;
import static bloodandmithril.util.ComparisonUtil.obj;
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

import bloodandmithril.character.ai.AIProcessor.ReturnIndividualPosition;
import bloodandmithril.character.ai.ArtificialIntelligence;
import bloodandmithril.character.ai.task.Travel;
import bloodandmithril.character.combat.CombatService;
import bloodandmithril.character.conditions.Condition;
import bloodandmithril.character.proficiency.Proficiencies;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.FireLighter;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.item.items.equipment.Equipper;
import bloodandmithril.item.items.equipment.EquipperImpl;
import bloodandmithril.item.items.equipment.offhand.Torch;
import bloodandmithril.item.items.equipment.weapon.OneHandedMeleeWeapon;
import bloodandmithril.item.items.equipment.weapon.RangedWeapon;
import bloodandmithril.item.items.equipment.weapon.TwoHandedMeleeWeapon;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.SelectedIndividualsControlWindow;
import bloodandmithril.util.AnimationHelper.AnimationSwitcher;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.ParameterizedTask;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.SpacialConfiguration;
import bloodandmithril.util.Task;
import bloodandmithril.util.Util;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.util.datastructure.Commands;
import bloodandmithril.util.datastructure.TwoInts;
import bloodandmithril.util.datastructure.WrapperForTwo;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Class representing a character, PC or NPC.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class Individual implements Equipper, Serializable, Kinematics {
	private static final long serialVersionUID = 2821835360311044658L;

	/** The current action of this individual */
	private Action currentAction = STAND_LEFT;

	/** Which client number this {@link Individual} is selected by */
	private Set<Integer> selectedByClient = Sets.newHashSet();

	/** {@link Proficiencies}s of this {@link Individual} */
	private Proficiencies skills = new Proficiencies();

	/** Identifier of this character */
	private IndividualIdentifier id;

	/** State of this character */
	private IndividualState state;

	/** Which actions are currently active */
	private Commands activeCommands = new Commands();

	/** The AI responsible for this character */
	private ArtificialIntelligence ai;

	/** Holds state of the equipment and inventory */
	private EquipperImpl equipperImpl;

	/** Data used for {@link Kinematics} */
	private IndividualKineticsProcessingData kinematicsData = new IndividualKineticsProcessingData();

	/** The defined area such that the individual can interact with objects */
	private Box interactionBox;

	/** The hitbox defining the region where this {@link Individual} can be hit */
	private Box hitBox;

	/** The set of {@link Individual}s currently being attacked by this {@link Individual} */
	private Set<Integer> individualsToBeAttacked = Sets.newHashSet();

	/** The set of {@link Individual}s currently being attacked by this {@link Individual} */
	private TwoInts tileToBeMined;

	/** Used to obey attacking periods */
	private float attackTimer;

	/** For animation frame timing */
	private float animationTimer;

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
	private int factionId;

	/** True if this {@link Individual} is dead */
	private boolean dead;

	/** When this timer reaches 0, this {@link Individual} will no longer be in combat stance */
	private float combatTimer;

	/** The timer that controls the {@link Travel} icon */
	private float travelIconTimer;

	/** If this is not zero, this individual can not speak */
	private float speakTimer;

	/** IDs of individuals that are currently attacking this one, along with a timer */
	private Map<Integer, Long> beingAttackedBy = Maps.newHashMap();

	private int maxConcurrentAttackers = 3;

	/** Whether this individual is allowed to speak */
	private boolean shutup = false;

	/** These variables are needed to prevent duplicate executions of action frames */
	private Action previousActionFrameAction;
	private int previousActionFrame;

	/** Whether or not AI is suppressed */
	private boolean supressAI;

	/**
	 * Constructor
	 */
	protected Individual(
			IndividualIdentifier id,
			IndividualState state,
			int factionId,
			float inventoryMassCapacity,
			int inventoryVolumeCapacity,
			int maxRings,
			int width,
			int height,
			int safetyHeight,
			Box interactionBox,
			int worldId,
			int maximumConcurrentMeleeAttackers) {
		this.equipperImpl = new EquipperImpl(inventoryMassCapacity, inventoryVolumeCapacity, maxRings);
		this.id = id;
		this.state = state;
		this.setFactionId(factionId);
		this.safetyHeight = safetyHeight;
		this.interactionBox = interactionBox;
		this.setWorldId(worldId);
		this.hitBox = new Box(new Vector2(state.position), width, height);
	}


	/**
	 * Copies all fields onto this individual from another, this is used in multi-player when synchronising server/client individual data
	 */
	public synchronized void copyFrom(Individual other) {
		this.currentAction = other.currentAction;
		this.selectedByClient = other.selectedByClient;
		this.skills = other.skills;
		this.id = other.id;
		this.state = other.state;
		this.activeCommands = other.activeCommands;
		this.setAi(other.getAI().copy());
		synchronizeEquipper(other.equipperImpl);
		synchronizeContainer(other.equipperImpl);
		this.kinematicsData = other.kinematicsData;
		this.interactionBox = other.interactionBox;
		this.hitBox = other.hitBox;
		this.individualsToBeAttacked = other.individualsToBeAttacked;
		this.tileToBeMined = other.tileToBeMined;
		this.attackTimer = other.attackTimer;
		this.animationTimer = other.animationTimer;
		this.setAiReactionTimer(other.getAiReactionTimer());
		this.combatStance = other.combatStance;
		this.walking = other.walking;
		this.safetyHeight = other.safetyHeight;
		this.timeStamp = other.timeStamp;
		this.worldId = other.worldId;
		this.factionId = other.factionId;
		this.dead = other.dead;
		this.setCombatTimer(other.getCombatTimer());
		this.travelIconTimer = other.travelIconTimer;
		this.setSpeakTimer(other.getSpeakTimer());
		this.setBeingAttackedBy(other.getBeingAttackedBy());
		this.maxConcurrentAttackers = other.maxConcurrentAttackers;
		this.shutup = other.shutup;
		this.setPreviousActionFrameAction(other.getPreviousActionFrameAction());
		this.setPreviousActionFrame(other.getPreviousActionFrame());
		this.supressAI = other.supressAI;

		internalCopyFrom(other);
	}

	/**
	 * @return the default (unarmed) attack period of this individual
	 */
	public abstract float getDefaultAttackPeriod();

	/**
	 * @return the position at which items are discarded from inventory, as well as bleeding
	 */
	public abstract Vector2 getEmissionPosition();

	/**
	 * @return the minimum damage dealt when unarmed
	 */
	public abstract float getUnarmedMinDamage();

	/**
	 * @return the maximum damage dealt when unarmed
	 */
	public abstract float getUnarmedMaxDamage();

	/**
	 * @return the {@link Box} that will be used to calculate overlaps with other hitboxes, when no weapon-specific hitboxes are found
	 */
	public abstract Box getDefaultAttackingHitBox();

	/**
	 * Called during the update routine when the currentAction is attacking
	 */
	protected abstract void respondToAttackCommand();

	/**
	 * @return the map that maps from an {@link Action} to a map that maps action frames to their respective {@link Task}s
	 */
	protected abstract Map<Action, Map<Integer, ParameterizedTask<Individual>>> getActionFrames();

	/**
	 * @return the current {@link AnimationSwitcher} of this {@link Individual}
	 */
	public abstract List<WrapperForTwo<AnimationSwitcher, ShaderProgram>> getCurrentAnimation();

	/**
	 * @return Implementation-specific copy method of this {@link Individual}
	 */
	public abstract Individual copy();

	/**
	 * Adds a floating text at close proximity to this individual
	 */
	public void addFloatingText(String text, Color color) {
		UserInterface.addFloatingText(
			text,
			color,
			getState().position.cpy().add(0f, getHeight()).add(new Vector2(0, 15f).rotate(Util.getRandom().nextFloat() * 360f)),
			false
		);
	}


	/** Returns the {@link ArtificialIntelligence} implementation of this {@link Individual} */
	public ArtificialIntelligence getAI() {
		return this.ai;
	}


	/** Returns the {@link IndividualState} of this {@link Individual} */
	public IndividualState getState() {
		return state;
	}


	/** Returns the {@link Proficiencies} of this {@link Individual} */
	public Proficiencies getProficiencies() {
		return skills;
	}


	/** The {@link #timeStamp} is used for client-server synchronisation, if the received timeStamp is older than the current, it will be rejected */
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
		return controlledFactions.contains(getFactionId());
	}


	/** Setups up all individual resources */
	public static void setup() {
	}


	/** Renders any decorations for UI */
	public void renderUIDecorations() {
		if (isSelected()) {
			getGraphics().getSpriteBatch().setShader(Shaders.filter);

			Shaders.filter.setUniformf("color",
				(float)sin(PI * (1f - state.health/state.maxHealth) / 2),
				(float)cos(PI * (1f - state.health/state.maxHealth) / 2),
				0f,
				1f
			);

			Shaders.filter.setUniformMatrix("u_projTrans", UserInterface.UICameraTrackingCam.combined);
			getGraphics().getSpriteBatch().draw(UserInterface.currentArrow, state.position.x - 5, state.position.y + getHeight() + 10);
		}

		if (UserInterface.DEBUG) {
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(Color.ORANGE);
			shapeRenderer.rect(
				worldToScreenX(interactionBox.position.x - interactionBox.width / 2),
				worldToScreenY(interactionBox.position.y - interactionBox.height / 2),
				interactionBox.width,
				interactionBox.height
			);
			shapeRenderer.setColor(Color.RED);
			shapeRenderer.rect(
				worldToScreenX(getHitBox().position.x - getHitBox().width / 2),
				worldToScreenY(getHitBox().position.y - getHitBox().height / 2),
				getHitBox().width,
				getHitBox().height
			);
			shapeRenderer.end();
		}

		if (isAlive() && isMouseOver() && Gdx.input.isKeyPressed(getKeyMappings().attack.keyCode) && !Gdx.input.isKeyPressed(getKeyMappings().rangedAttack.keyCode)) {
			if (Domain.getSelectedIndividuals().size() > 0 && (!Domain.isIndividualSelected(this) || Domain.getSelectedIndividuals().size() > 1)) {
				getGraphics().getSpriteBatch().setShader(Shaders.filter);
				Shaders.filter.setUniformMatrix("u_projTrans", UserInterface.UICamera.combined);
				Shaders.filter.setUniformf("color", Color.BLACK);
				Fonts.defaultFont.draw(getGraphics().getSpriteBatch(), "Attack Melee", getMouseScreenX() + 14, getMouseScreenY() - 26);
				getGraphics().getSpriteBatch().flush();
				Shaders.filter.setUniformf("color", Color.ORANGE);
				Fonts.defaultFont.draw(getGraphics().getSpriteBatch(), "Attack Melee", getMouseScreenX() + 15, getMouseScreenY() - 25);
			}
		}
	}


	/** Select this {@link Individual} */
	public void select(int clientId) {
		if (!isAlive()) {
			return;
		}

		Domain.addSelectedIndividual(this);
		getAI().setToManual();
		selectedByClient.add(clientId);

		if (ClientServerInterface.isClient()) {
			UserInterface.addLayeredComponentUnique(
				new SelectedIndividualsControlWindow(
					getGraphics().getWidth() - 170,
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
			getAI().setToAuto(clearTask);
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


	/** The amount of hunger drained, per update tick (1/60) of a second, max hunger is 1 */
	protected abstract float hungerDrain();

	/** The amount of thirst drained, per update tick (1/60) of a second, max thirst is 1 */
	protected abstract float thirstDrain();

	/** The amount of stamina drained, per update tick (1/60) of a second, max stamina is 1 */
	protected abstract float staminaDrain();


	/** Is this individual the current one? */
	public boolean isSelected() {
		return Domain.isIndividualSelected(this);
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
	 * @param Revive from the dead with specified health
	 */
	public void revive(float health) {
		if (dead) {
			dead = false;
			heal(health);
			// TODO revival
		}
	}


	public boolean isAlive() {
		return !dead;
	}


	/** Kills this {@link Individual} */
	private void kill() {
		dead = true;
		if (ClientServerInterface.isClient()) {
			Domain.removeSelectedIndividual(this);
		}
		getEquipped().keySet().forEach(eq -> {
			Individual.this.unequip((Equipable ) eq);
		});
		getState().currentConditions.clear();
		deselect(true, 0);
		clearCommands();
		selectedByClient.clear();
		internalKill();
	}

	protected abstract void internalKill();

	public boolean isAISuppressed() {
		return supressAI;
	}


	public void setAISuppression(boolean suppress) {
		this.supressAI = suppress;
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
		if (state.health == 0f) {
			return;
		}

		if (state.health - amount <= 0f) {
			state.health = 0f;
			kill();
		} else {
			state.health = state.health - amount;
		}
	}


	/**
	 * Play a moaning sound, indicating being hurt
	 */
	public abstract void moan();


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


	/** Updates this character */
	protected abstract void internalUpdate(float delta);

	public abstract float getWalkSpeed();

	public abstract float getRunSpeed();

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

	/** Returns the set of {@link Construction}s able to be constructed by this {@link Individual} */
	public abstract Set<Construction> getConstructables();

	/** Returns the {@link SpacialConfiguration} where {@link OneHandedMeleeWeapon} will be rendered */
	public abstract SpacialConfiguration getOneHandedWeaponSpatialConfigration();

	/** Returns the {@link SpacialConfiguration} where {@link TwoHandedMeleeWeapon} will be rendered */
	public abstract SpacialConfiguration getTwoHandedWeaponSpatialConfigration();

	/** Returns the {@link SpacialConfiguration} for off-hand equippables */
	public abstract SpacialConfiguration getOffHandSpatialConfigration();


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
	public boolean isEmpty() {
		return equipperImpl.isEmpty();
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
		return round(getHitBox().width);
	}


	/**
	 * Height of the individual
	 */
	public int getHeight() {
		return round(getHitBox().height);
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


	public void setCombatStance(boolean combatStance) {
		if (combatStance) {
			setCombatTimer(5f);
		}
		this.combatStance = combatStance;
	}


	public Action getCurrentAction() {
		return currentAction;
	}


	public void setCurrentAction(Action currentAction) {
		this.currentAction = currentAction;
	}


	public float getAnimationTimer() {
		return animationTimer;
	}


	public void setAnimationTimer(float animationTimer) {
		this.animationTimer = animationTimer;
	}


	public int getFactionId() {
		return factionId;
	}


	public void setFactionId(int factionId) {
		this.factionId = factionId;
	}


	public void setAi(ArtificialIntelligence ai) {
		this.ai = ai;
	}


	public Set<Integer> getIndividualsToBeAttacked() {
		return individualsToBeAttacked;
	}


	public void setIndividualsToBeAttacked(Set<Integer> individualsToBeAttacked) {
		this.individualsToBeAttacked = individualsToBeAttacked;
	}


	public Box getHitBox() {
		return hitBox;
	}


	public boolean canBeAttacked(Individual by) {
		synchronized (getBeingAttackedBy()) {
			if (getBeingAttackedBy().containsKey(by.getId().getId())) {
				return true;
			} else {
				int totalConcurrentAttackNumber = getBeingAttackedBy().keySet().stream().mapToInt(i -> {
					return Domain.getIndividual(i).getConcurrentAttackNumber();
				}).sum();

				return totalConcurrentAttackNumber + by.getConcurrentAttackNumber() <= this.maxConcurrentAttackers;
			}
		}
	}


	public void addAttacker(Individual attacker) {
		if (canBeAttacked(attacker)) {
			this.getBeingAttackedBy().put(attacker.getId().getId(), System.currentTimeMillis());
		}
	}


	public abstract int getConcurrentAttackNumber();


	public boolean attacking() {
		return obj(getCurrentAction()).oneOf(
			ATTACK_LEFT_ONE_HANDED_WEAPON_STAB,
			ATTACK_RIGHT_ONE_HANDED_WEAPON_STAB,
			ATTACK_LEFT_ONE_HANDED_WEAPON,
			ATTACK_RIGHT_ONE_HANDED_WEAPON,
			ATTACK_LEFT_SPEAR,
			ATTACK_RIGHT_SPEAR,
			ATTACK_LEFT_TWO_HANDED_WEAPON,
			ATTACK_RIGHT_TWO_HANDED_WEAPON,
			ATTACK_LEFT_UNARMED,
			ATTACK_RIGHT_UNARMED,
			ATTACK_LEFT_ONE_HANDED_WEAPON_MINE,
			ATTACK_RIGHT_ONE_HANDED_WEAPON_MINE
		);
	}


	public float getAttackTimer() {
		return attackTimer;
	}


	public void setAttackTimer(float timer) {
		this.attackTimer = timer;
	}


	public TwoInts getTileToBeMined() {
		return tileToBeMined;
	}


	public boolean canAttackRanged() {
		return getEquipped().keySet().stream().filter(item -> {return item instanceof RangedWeapon;}).count() != 0;
	}


	public void attackRanged(Vector2 target) {
		RangedWeapon rangedWeapon = (RangedWeapon) getEquipped().keySet().stream().filter(item -> {return item instanceof RangedWeapon;}).findAny().get();
		if (rangedWeapon != null) {
			Vector2 emissionPosition = getEmissionPosition();
			Vector2 firingVector = target.cpy().sub(emissionPosition);

			boolean hasAmmo = false;
			Item ammo = rangedWeapon.getAmmo();

			if (ammo == null) {
				addFloatingText("No ammo selected", Color.ORANGE);
				return;
			}

			for (Item item : Lists.newArrayList(getInventory().keySet())) {
				if (ammo.sameAs(item)) {
					hasAmmo = true;
					takeItem(item);
					UserInterface.refreshRefreshableWindows();
				}
			}

			if (hasAmmo) {
				Projectile fired = rangedWeapon.fire(
					emissionPosition,
					firingVector.cpy().nor().scl(
						Math.min(
							1f,
							firingVector.len() / rangeControl
						)
					)
				);

				if (fired == null) {
					addFloatingText("No ammo selected", Color.ORANGE);
				} else {
					fired.preFireDecorate(this);
					fired.ignoreIndividual(this);
					Domain.getWorld(getWorldId()).projectiles().addProjectile(fired);
				}

				if (has(ammo) == 0) {
					rangedWeapon.setAmmo(null);
					UserInterface.refreshRefreshableWindows();
				}
			} else {
				addFloatingText("Out of ammo", Color.ORANGE);
				rangedWeapon.setAmmo(null);
				UserInterface.refreshRefreshableWindows();
			}
		}
	}


	public float getTravelIconTimer() {
		return travelIconTimer;
	}


	public void setTravelIconTimer(float travelIconTimer) {
		this.travelIconTimer = travelIconTimer;
	}


	public boolean isShutUp() {
		return shutup;
	}


	public void setShutUp(boolean shutup) {
		this.shutup = shutup;
	}


	public void speak(String text, long duration) {
		if (dead || shutup || getSpeakTimer() > 0f) {
			return;
		}

		if (ClientServerInterface.isServer()) {
			UserInterface.addTextBubble(
				text,
				new ReturnIndividualPosition(this),
				duration,
				0,
				(int) (getHeight() * 1.3f)
			);
			setSpeakTimer(duration / 1000f);
		}
	}


	public void sayStuck() {}


	public int getRenderPriority() {
		for (Item equipped : getEquipped().keySet()) {
			if (equipped instanceof Torch) {
				return 2;
			}
		}

		return isControllable() ? 1 : 0;
	}


	public boolean canBeUsedAsFireSource() {
		for (Item item : getEquipped().keySet()) {
			if (item instanceof FireLighter) {
				if (((FireLighter) item).canLightFire()) {
					return true;
				}
			}
		}

		return false;
	}


	public FireLighter getFireLighter() {
		for (Item item : getEquipped().keySet()) {
			if (item instanceof FireLighter) {
				if (((FireLighter) item).canLightFire()) {
					return (FireLighter) item;
				}
			}
		}

		for (Item item : getInventory().keySet()) {
			if (item instanceof FireLighter) {
				if (((FireLighter) item).canLightFire()) {
					return (FireLighter) item;
				}
			}
		}

		return null;
	}


	public float getSpeakTimer() {
		return speakTimer;
	}


	public void setSpeakTimer(float speakTimer) {
		this.speakTimer = speakTimer;
	}


	public float getCombatTimer() {
		return combatTimer;
	}


	public void setCombatTimer(float combatTimer) {
		this.combatTimer = combatTimer;
	}


	public float getAiReactionTimer() {
		return aiReactionTimer;
	}


	public void setAiReactionTimer(float aiReactionTimer) {
		this.aiReactionTimer = aiReactionTimer;
	}


	public Map<Integer, Long> getBeingAttackedBy() {
		return beingAttackedBy;
	}


	public void setBeingAttackedBy(Map<Integer, Long> beingAttackedBy) {
		this.beingAttackedBy = beingAttackedBy;
	}


	public Action getPreviousActionFrameAction() {
		return previousActionFrameAction;
	}


	public void setPreviousActionFrameAction(Action previousActionFrameAction) {
		this.previousActionFrameAction = previousActionFrameAction;
	}


	public int getPreviousActionFrame() {
		return previousActionFrame;
	}


	public void setPreviousActionFrame(int previousActionFrame) {
		this.previousActionFrame = previousActionFrame;
	}


	public CombatService combat() {
		return new CombatService();
	}


	public enum Action implements Serializable {
		DEAD(true),
		JUMP_LEFT(true),
		JUMP_RIGHT(false),
		STAND_LEFT(true),
		STAND_RIGHT(false),
		STAND_LEFT_COMBAT_ONE_HANDED(true),
		STAND_RIGHT_COMBAT_ONE_HANDED(false),
		WALK_LEFT(true),
		WALK_RIGHT(false),
		RUN_LEFT(true),
		RUN_RIGHT(false),
		ATTACK_LEFT_UNARMED(true),
		ATTACK_RIGHT_UNARMED(false),
		ATTACK_LEFT_ONE_HANDED_WEAPON(true),
		ATTACK_RIGHT_ONE_HANDED_WEAPON(false),
		ATTACK_LEFT_ONE_HANDED_WEAPON_STAB(true),
		ATTACK_RIGHT_ONE_HANDED_WEAPON_STAB(false),
		ATTACK_LEFT_TWO_HANDED_WEAPON(true),
		ATTACK_RIGHT_TWO_HANDED_WEAPON(false),
		ATTACK_LEFT_ONE_HANDED_WEAPON_MINE(true),
		ATTACK_RIGHT_ONE_HANDED_WEAPON_MINE(false),
		ATTACK_LEFT_SPEAR(true),
		ATTACK_RIGHT_SPEAR(false);

		private boolean left;

		private Action(boolean left) {
			this.left = left;
		}

		public boolean left() {
			return left;
		}
	}
}