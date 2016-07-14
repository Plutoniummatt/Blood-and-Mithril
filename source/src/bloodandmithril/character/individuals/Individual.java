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
import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.control.InputUtilities.getMouseWorldX;
import static bloodandmithril.control.InputUtilities.getMouseWorldY;
import static bloodandmithril.control.InputUtilities.isKeyPressed;
import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;
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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import bloodandmithril.character.ai.AIProcessor.ReturnIndividualPosition;
import bloodandmithril.character.ai.ArtificialIntelligence;
import bloodandmithril.character.ai.perception.Visible;
import bloodandmithril.character.ai.task.Travel;
import bloodandmithril.character.combat.CombatService;
import bloodandmithril.character.conditions.Condition;
import bloodandmithril.character.faction.Faction;
import bloodandmithril.character.individuals.characters.Elf;
import bloodandmithril.character.individuals.characters.Hare;
import bloodandmithril.character.individuals.characters.Wolf;
import bloodandmithril.character.proficiency.Proficiencies;
import bloodandmithril.control.BloodAndMithrilClientInputProcessor;
import bloodandmithril.control.Controls;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.MouseOverable;
import bloodandmithril.core.Name;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.item.FireLighter;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.item.items.container.ContainerImpl;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.item.items.equipment.Equipper;
import bloodandmithril.item.items.equipment.EquipperImpl;
import bloodandmithril.item.items.equipment.weapon.OneHandedMeleeWeapon;
import bloodandmithril.item.items.equipment.weapon.RangedWeapon;
import bloodandmithril.item.items.equipment.weapon.TwoHandedMeleeWeapon;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.AnimationHelper.AnimationSwitcher;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.ParameterizedTask;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.SpacialConfiguration;
import bloodandmithril.util.Task;
import bloodandmithril.util.Util;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.util.datastructure.Commands;
import bloodandmithril.util.datastructure.WrapperForTwo;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;

/**
 * Class representing a character, PC or NPC.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@Name(name = "Individuals")
public abstract class Individual implements Equipper, Serializable, Kinematics, Visible, MouseOverable {
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

	/** The {@link Behaviour} of this {@link Individual} */
	private Behaviour naturalBehaviour;

	/**
	 * Constructor
	 */
	protected Individual(
			final IndividualIdentifier id,
			final IndividualState state,
			final int factionId,
			final Behaviour naturalBehaviour,
			final float inventoryMassCapacity,
			final int inventoryVolumeCapacity,
			final int maxRings,
			final int width,
			final int height,
			final int safetyHeight,
			final Box interactionBox,
			final int worldId,
			final int maximumConcurrentMeleeAttackers) {
		this.naturalBehaviour = naturalBehaviour;
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
	public synchronized void copyFrom(final Individual other) {
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
		this.naturalBehaviour = other.naturalBehaviour;

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
	public void addFloatingText(final String text, final Color color) {
		UserInterface.addFloatingText(
			text,
			color,
			getState().position.cpy().add(0f, getHeight()).add(new Vector2(0, 15f).rotate(Util.getRandom().nextFloat() * 360f)),
			false,
			getWorldId()
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
	public synchronized final long getTimeStamp() {
		return timeStamp;
	}


	/** See {@link #getTimeStamp()} */
	public synchronized final void setTimeStamp(final long timeStamp) {
		this.timeStamp = timeStamp;
	}


	/** True if this {@link Individual} is currently {@link #walking} */
	public final boolean isWalking() {
		return walking;
	}


	/** See {@link #isWalking()} */
	public final synchronized void setWalking(final boolean walking) {
		this.walking = walking;
	}


	/** See {@link #copy()} */
	protected abstract void internalCopyFrom(Individual other);


	/** Setups up all individual resources */
	public static final void setup() {
	}


	/** Renders any decorations for UI */
	public final void renderUIDecorations(final Graphics graphics) {
		final SpriteBatch batch = graphics.getSpriteBatch();

		if (Wiring.injector().getInstance(GameClientStateTracker.class).isIndividualSelected(this)) {
			batch.setShader(Shaders.filter);

			Shaders.filter.setUniformf("color",
				(float)sin(PI * (1f - state.health/state.maxHealth) / 2),
				(float)cos(PI * (1f - state.health/state.maxHealth) / 2),
				0f,
				1f
			);

			Shaders.filter.setUniformMatrix("u_projTrans", graphics.getUi().getUITrackingCamera().combined);
			batch.draw(UserInterface.currentArrow, state.position.x - 5, state.position.y + getHeight() + 10);
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

		final Controls controls = Wiring.injector().getInstance(Controls.class);
		final GameClientStateTracker gameClientStateTracker = Wiring.injector().getInstance(GameClientStateTracker.class);

		if (isAlive() && isMouseOver() && isKeyPressed(controls.attack.keyCode) && !isKeyPressed(controls.rangedAttack.keyCode)) {
			if (gameClientStateTracker.getSelectedIndividuals().size() > 0 && (!gameClientStateTracker.isIndividualSelected(this) || gameClientStateTracker.getSelectedIndividuals().size() > 1)) {
				batch.setShader(Shaders.filter);
				Shaders.filter.setUniformMatrix("u_projTrans", graphics.getUi().getUICamera().combined);
				Shaders.filter.setUniformf("color", Color.BLACK);
				Fonts.defaultFont.draw(batch, "Attack Melee", getMouseScreenX() + 14, getMouseScreenY() - 26);
				batch.flush();
				Shaders.filter.setUniformf("color", Color.ORANGE);
				Fonts.defaultFont.draw(batch, "Attack Melee", getMouseScreenX() + 15, getMouseScreenY() - 25);
			}
		}
	}


	/** Deselect this {@link Individual} */
	public final void deselect(final boolean clearTask, final int id) {
		selectedByClient.remove(id);

		if (selectedByClient.isEmpty()) {
			getAI().setToAuto(clearTask);
		}
	}


	/** Clears all commands */
	public final synchronized void clearCommands() {
		activeCommands.clear();
	}


	/** Handles commands */
	public final synchronized void sendCommand(final int keyCode, final boolean val) {
		if (val) {
			activeCommands.activate(Integer.toString(keyCode));
		} else {
			activeCommands.deactivate(Integer.toString(keyCode));
		}
	}


	/**
	 * No longer jumping off, set {@link #jumpOff} to null, to signify that this individual is not trying to jump off any tile
	 */
	public final synchronized void setJumpOffToNull() {
		getKinematicsData().jumpOff = null;
		getKinematicsData().jumpedOff = false;
	}


	/** The amount of hunger drained, per update tick (1/60) of a second, max hunger is 1 */
	protected abstract float hungerDrain();

	/** The amount of thirst drained, per update tick (1/60) of a second, max thirst is 1 */
	protected abstract float thirstDrain();

	/** The amount of stamina drained, per update tick (1/60) of a second, max stamina is 1 */
	protected abstract float staminaDrain();


	/** Calculates the distance between this individual and the Vector2 parameter */
	public final float getDistanceFrom(final Vector2 position) {
		try {
			return state.position.cpy().sub(position).len();
		} catch (final Throwable a) {
			a.printStackTrace();
			throw new RuntimeException(a);
		}
	}


	public final boolean isAlive() {
		return !dead;
	}


	/** Kills this {@link Individual} */
	private final void kill() {
		dead = true;
		if (ClientServerInterface.isClient()) {
			Wiring.injector().getInstance(GameClientStateTracker.class).removeSelectedIndividual(this);
		}
		getEquipped().keySet().forEach(eq -> {
			Individual.this.unequip((Equipable ) eq);
		});
		getInventory().entrySet().forEach(entry -> {
			ContainerImpl.discard(Individual.this, entry.getKey(), entry.getValue());
		});
		getState().currentConditions.clear();
		deselect(true, 0);
		clearCommands();
		selectedByClient.clear();
		internalKill();
		UserInterface.refreshRefreshableWindows();
	}

	protected abstract void internalKill();

	public final boolean isAISuppressed() {
		return supressAI;
	}


	public final void setAISuppression(final boolean suppress) {
		this.supressAI = suppress;
	}


	/**
	 * @return true if a command is currently active
	 */
	public final synchronized boolean isCommandActive(final int keycode) {
		return activeCommands.isActive(Integer.toString(keycode));
	}


	public final synchronized void decreaseThirst(final float amount) {
		if (state.thirst - amount <= 0f) {
			state.thirst = 0f;
		} else {
			state.thirst = state.thirst - amount;
		}
	}


	public final synchronized void increaseThirst(final float amount) {
		if (state.thirst + amount > 1f) {
			state.thirst = 1f;
		} else {
			state.thirst = state.thirst + amount;
		}
	}


	public final synchronized void damage(final float amount) {
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


	public synchronized void heal(final float amount) {
		if (state.health + amount > state.maxHealth) {
			state.health = state.maxHealth;
		} else {
			state.health = state.health + amount;
		}
	}


	public final synchronized void increaseHunger(final float amount) {
		if (state.hunger + amount >= 1f) {
			state.hunger = 1f;
		} else {
			state.hunger = state.hunger + amount;
		}
	}


	public final synchronized void decreaseHunger(final float amount) {
		if (state.hunger - amount <= 0f) {
			state.hunger = 0f;
		} else {
			state.hunger = state.hunger - amount;
		}
	}


	public final synchronized void increaseMana(final float amount) {
		if (state.mana + amount >= state.maxMana) {
			state.mana = state.maxMana;
		} else {
			state.mana = state.hunger + amount;
		}
	}


	public final synchronized void decreaseMana(final float amount) {
		if (state.mana - amount <= 0f) {
			state.mana = 0f;
		} else {
			state.mana = state.mana - amount;
		}
	}


	public final synchronized void increaseStamina(final float amount) {
		if (state.stamina + amount >= 1f) {
			state.stamina = 1f;
		} else {
			state.stamina = state.stamina + amount;
		}
	}


	public final synchronized void decreaseStamina(final float amount) {
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
	public final synchronized void addCondition(final Condition condition) {
		for (final Condition existing : newHashSet(state.currentConditions)) {
			if (condition.getClass().equals(existing.getClass())) {
				existing.stack(condition);
				return;
			}
		}
		state.currentConditions.add(condition);
	}


	public final synchronized void changeStaminaRegen(final float newValue) {
		state.staminaRegen = newValue;
	}


	public final synchronized void changeHealthRegen(final float newValue) {
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
	@Override
	public final boolean isMouseOver() {
		final float x = getMouseWorldX();
		final float y = getMouseWorldY();

		final boolean ans = x >= getState().position.x - getWidth()/2 && x <= getState().position.x + getWidth()/2 && y >= getState().position.y && y <= getState().position.y + getHeight();
		return ans;
	}


	@Override
	public String getMenuTitle() {
		return getId().getSimpleName();
	}


	@Override
	public final boolean unlock(final Item with) {
		// Do nothing
		return false;
	}


	@Override
	public final boolean lock(final Item with) {
		// Do nothing
		return false;
	}


	@Override
	public final boolean isLocked() {
		return false;
	}


	@Override
	public final boolean isLockable() {
		return false;
	}


	@Override
	public final boolean isEmpty() {
		return equipperImpl.isEmpty();
	}


	@Override
	public final Container getContainerImpl() {
		return equipperImpl;
	}


	@Override
	public final Equipper getEquipperImpl() {
		return equipperImpl;
	}


	/**
	 * Returns the {@link IndividualIdentifier} of this {@link Individual}
	 */
	public final IndividualIdentifier getId() {
		return id;
	}


	/**
	 * Returns a {@link HashSet} containing client ID's of all clients that have this
	 * {@link Individual} selected
	 */
	public final Set<Integer> getSelectedByClient() {
		return selectedByClient;
	}


	/**
	 * Height at which it's deemed unsafe to fall to the ground
	 */
	public final int getSafetyHeight() {
		return safetyHeight;
	}


	/**
	 * The box defining the region where this {@link Individual} can interact with entities
	 */
	public final Box getInteractionBox() {
		return interactionBox;
	}


	/**
	 * Width of the individual
	 */
	public final int getWidth() {
		return round(getHitBox().width);
	}


	/**
	 * Height of the individual
	 */
	public final int getHeight() {
		return round(getHitBox().height);
	}


	/**
	 * {@link World} id of this {@link Individual}
	 */
	public final int getWorldId() {
		return worldId;
	}


	/**
	 * See {@link #getWorldId()}
	 */
	public final void setWorldId(final int worldId) {
		this.worldId = worldId;
	}


	/**
	 * The kinematics data, containing things needed for kinematics processing
	 */
	public final IndividualKineticsProcessingData getKinematicsData() {
		return kinematicsData;
	}


	/**
	 * @return Whether or not this {@link Individual} is in combat stance
	 */
	public final boolean inCombatStance() {
		return combatStance;
	}


	public final void setCombatStance(final boolean combatStance) {
		if (combatStance) {
			setCombatTimer(5f);
		}
		this.combatStance = combatStance;
	}


	public final Action getCurrentAction() {
		return currentAction;
	}


	public final void setCurrentAction(final Action currentAction) {
		this.currentAction = currentAction;
	}


	public final float getAnimationTimer() {
		return animationTimer;
	}


	public void setAnimationTimer(final float animationTimer) {
		this.animationTimer = animationTimer;
	}


	public final int getFactionId() {
		return factionId;
	}


	public final void setFactionId(final int factionId) {
		this.factionId = factionId;
	}


	public final void setAi(final ArtificialIntelligence ai) {
		this.ai = ai;
	}


	public final Set<Integer> getIndividualsToBeAttacked() {
		return individualsToBeAttacked;
	}


	public final void setIndividualsToBeAttacked(final Set<Integer> individualsToBeAttacked) {
		this.individualsToBeAttacked = individualsToBeAttacked;
	}


	public final Box getHitBox() {
		return hitBox;
	}


	public final boolean canBeAttacked(final Individual by) {
		synchronized (getBeingAttackedBy()) {
			if (getBeingAttackedBy().containsKey(by.getId().getId())) {
				return true;
			} else {
				final int totalConcurrentAttackNumber = getBeingAttackedBy().keySet().stream().mapToInt(i -> {
					return Domain.getIndividual(i).getConcurrentAttackNumber();
				}).sum();

				return totalConcurrentAttackNumber + by.getConcurrentAttackNumber() <= this.maxConcurrentAttackers;
			}
		}
	}


	public final void addAttacker(final Individual attacker) {
		if (canBeAttacked(attacker)) {
			this.getBeingAttackedBy().put(attacker.getId().getId(), System.currentTimeMillis());
		}
	}


	public abstract int getConcurrentAttackNumber();


	public final boolean attacking() {
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


	public final float getAttackTimer() {
		return attackTimer;
	}


	public final void setAttackTimer(final float timer) {
		this.attackTimer = timer;
	}


	public final boolean canAttackRanged() {
		return getEquipped().keySet().stream().filter(item -> {return item instanceof RangedWeapon;}).count() != 0;
	}


	public final void attackRanged(final Vector2 target) {
		final RangedWeapon rangedWeapon = (RangedWeapon) getEquipped().keySet().stream().filter(item -> {return item instanceof RangedWeapon;}).findAny().get();
		if (rangedWeapon != null) {
			final Vector2 emissionPosition = getEmissionPosition();
			final Vector2 firingVector = target.cpy().sub(emissionPosition);

			boolean hasAmmo = false;
			final Item ammo = rangedWeapon.getAmmo();

			if (ammo == null) {
				addFloatingText("No ammo selected", Color.ORANGE);
				return;
			}

			for (final Item item : Lists.newArrayList(getInventory().keySet())) {
				if (ammo.sameAs(item)) {
					hasAmmo = true;
					takeItem(item);
					UserInterface.refreshRefreshableWindows();
				}
			}

			if (hasAmmo) {
				final Projectile fired = rangedWeapon.fire(
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


	public final float getTravelIconTimer() {
		return travelIconTimer;
	}


	public final void setTravelIconTimer(final float travelIconTimer) {
		this.travelIconTimer = travelIconTimer;
	}


	public final boolean isShutUp() {
		return shutup;
	}


	public final void setShutUp(final boolean shutup) {
		this.shutup = shutup;
	}


	/**
	 * Plays the audio that signals an "affirmative"
	 */
	public abstract void playAffirmativeSound();


	public final void speak(final String text, final long duration) {
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


	public final boolean canBeUsedAsFireSource() {
		for (final Item item : getEquipped().keySet()) {
			if (item instanceof FireLighter) {
				if (((FireLighter) item).canLightFire()) {
					return true;
				}
			}
		}

		return false;
	}


	public final FireLighter getFireLighter() {
		for (final Item item : getEquipped().keySet()) {
			if (item instanceof FireLighter) {
				if (((FireLighter) item).canLightFire()) {
					return (FireLighter) item;
				}
			}
		}

		for (final Item item : getInventory().keySet()) {
			if (item instanceof FireLighter) {
				if (((FireLighter) item).canLightFire()) {
					return (FireLighter) item;
				}
			}
		}

		return null;
	}


	public final float getSpeakTimer() {
		return speakTimer;
	}


	public final void setSpeakTimer(final float speakTimer) {
		this.speakTimer = speakTimer;
	}


	public final float getCombatTimer() {
		return combatTimer;
	}


	public final void setCombatTimer(final float combatTimer) {
		this.combatTimer = combatTimer;
	}


	public final float getAiReactionTimer() {
		return aiReactionTimer;
	}


	public final void setAiReactionTimer(final float aiReactionTimer) {
		this.aiReactionTimer = aiReactionTimer;
	}


	public final Map<Integer, Long> getBeingAttackedBy() {
		return beingAttackedBy;
	}


	public final void setBeingAttackedBy(final Map<Integer, Long> beingAttackedBy) {
		this.beingAttackedBy = beingAttackedBy;
	}


	public final Action getPreviousActionFrameAction() {
		return previousActionFrameAction;
	}


	public final void setPreviousActionFrameAction(final Action previousActionFrameAction) {
		this.previousActionFrameAction = previousActionFrameAction;
	}


	public final int getPreviousActionFrame() {
		return previousActionFrame;
	}


	public final void setPreviousActionFrame(final int previousActionFrame) {
		this.previousActionFrame = previousActionFrame;
	}


	public final CombatService combat() {
		return new CombatService();
	}


	public final Behaviour getNaturalBehaviour() {
		return naturalBehaviour;
	}


	@SuppressWarnings("unchecked")
	public static final Map<Class<? extends Visible>, List<Class<? extends Individual>>> getAllIndividualClasses() {
		final Map<Class<? extends Visible>, List<Class<? extends Individual>>> map = Maps.newHashMap();

		map.put(Humanoid.class, Lists.newArrayList(Elf.class));
		map.put(Animal.class, Lists.newArrayList(Wolf.class, Hare.class));

		return map;
	}


	public final Behaviour deriveBehaviourTowards(final Individual indi) {
		if (indi.factionId == Faction.getNature().factionId) {
			return indi.naturalBehaviour;
		} else {
			if (factionId == indi.factionId) {
				return Behaviour.FRIENDLY;
			} else {
				// TODO diplomacy
				return naturalBehaviour;
			}
		}
	}


	public enum Behaviour {
		FRIENDLY("Friendly"), NEUTRAL("Neutral"), HOSTILE("Hostile");
		public String description;
		private Behaviour(final String s) {
			this.description = s;
		}
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
		AIM_LEFT(true),
		AIM_RIGHT(false),
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

		private Action(final boolean left) {
			this.left = left;
		}

		public boolean left() {
			return left;
		}
	}


	public void followCam() {
		Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).setCamFollowFunction(() -> {
			return getState().position.cpy().add(
				0,
				Gdx.graphics.getHeight() / 5
			);
		});
	}
}