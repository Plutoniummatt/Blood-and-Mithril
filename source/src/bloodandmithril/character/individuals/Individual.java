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
import static bloodandmithril.core.BloodAndMithrilClient.HEIGHT;
import static bloodandmithril.core.BloodAndMithrilClient.WIDTH;
import static bloodandmithril.core.BloodAndMithrilClient.controlledFactions;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.networking.ClientServerInterface.isClient;
import static bloodandmithril.networking.ClientServerInterface.isServer;
import static bloodandmithril.ui.UserInterface.shapeRenderer;
import static bloodandmithril.util.ComparisonUtil.obj;
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

import org.apache.commons.lang3.StringUtils;

import bloodandmithril.character.ai.AIProcessor.ReturnIndividualPosition;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ArtificialIntelligence;
import bloodandmithril.character.ai.task.Attack;
import bloodandmithril.character.ai.task.Follow;
import bloodandmithril.character.ai.task.TradeWith;
import bloodandmithril.character.ai.task.Travel;
import bloodandmithril.character.combat.CombatChain;
import bloodandmithril.character.conditions.Condition;
import bloodandmithril.character.conditions.Exhaustion;
import bloodandmithril.character.conditions.Hunger;
import bloodandmithril.character.conditions.Thirst;
import bloodandmithril.character.skill.Skills;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.item.items.equipment.Equipper;
import bloodandmithril.item.items.equipment.EquipperImpl;
import bloodandmithril.item.items.equipment.armor.Armor;
import bloodandmithril.item.items.equipment.weapon.MeleeWeapon;
import bloodandmithril.item.items.equipment.weapon.OneHandedMeleeWeapon;
import bloodandmithril.item.items.equipment.weapon.RangedWeapon;
import bloodandmithril.item.items.equipment.weapon.Weapon;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.performance.PositionalIndexNode;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.ui.KeyMappings;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.BuildWindow;
import bloodandmithril.ui.components.window.IndividualInfoWindow;
import bloodandmithril.ui.components.window.IndividualStatusWindow;
import bloodandmithril.ui.components.window.InventoryWindow;
import bloodandmithril.ui.components.window.SelectedIndividualsControlWindow;
import bloodandmithril.ui.components.window.SkillsWindow;
import bloodandmithril.ui.components.window.TextInputWindow;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.ParameterizedTask;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.SpacialConfiguration;
import bloodandmithril.util.Task;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.util.datastructure.Commands;
import bloodandmithril.util.datastructure.TwoInts;
import bloodandmithril.util.datastructure.WrapperForTwo;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
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

	public enum Action implements Serializable {
		JUMP_LEFT(true),
		JUMP_RIGHT(false),
		STAND_LEFT(true),
		STAND_RIGHT(false),
		STAND_LEFT_COMBAT(true),
		STAND_RIGHT_COMBAT(false),
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

	/** The current action of this individual */
	private Action currentAction = STAND_LEFT;

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

	/** IDs of individuals that are currently attacking this one, along with a timer */
	private Map<Integer, Long> beingAttackedBy = Maps.newHashMap();

	private int maxConcurrentAttackers = 3;

	/** These variables are needed to prevent duplicate executions of action frames */
	private Action previousActionFrameAction;
	private int previousActionFrame;

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
	 * Copies all fields onto this individual from another
	 */
	public synchronized void copyFrom(Individual other) {
		this.setAi(other.getAI());
		this.setWorldId(other.getWorldId());
		this.selectedByClient = other.selectedByClient;
		this.setIndividualsToBeAttacked(other.getIndividualsToBeAttacked());
		this.aiReactionTimer = other.aiReactionTimer;
		this.setAnimationTimer(other.getAnimationTimer());
		this.activeCommands = other.activeCommands;
		this.setFactionId(other.getFactionId());
		this.id = other.id;
		this.interactionBox = other.getInteractionBox();
		this.kinematicsData = other.getKinematicsData();
		this.safetyHeight = other.safetyHeight;
		this.state = other.state;
		this.walking = other.walking;
		this.timeStamp = other.timeStamp;
		this.selectedByClient = other.selectedByClient;
		this.hitBox = other.getHitBox();
		this.skills = other.skills;
		this.setCurrentAction(other.getCurrentAction());
		this.combatStance = other.combatStance;
		this.attackTimer = other.attackTimer;
		this.currentAction = other.currentAction;
		this.dead = other.dead;
		this.individualsToBeAttacked = other.individualsToBeAttacked;
		this.combatTimer = other.combatTimer;
		this.travelIconTimer = other.travelIconTimer;
		synchronizeContainer(other.equipperImpl);
		synchronizeEquipper(other.equipperImpl);

		internalCopyFrom(other);
	}


	/**
	 * Attacks a set of other {@link Individual}s
	 */
	@SuppressWarnings("rawtypes")
	public synchronized boolean attack(Set<Integer> individuals) {
		if (attackTimer < getAttackPeriod()) {
			return false;
		}

		attackTimer = 0f;
		setAnimationTimer(0f);

		Optional<Item> weapon = Iterables.tryFind(getEquipped().keySet(), equipped -> {
			return equipped instanceof Weapon;
		});

		if (weapon.isPresent()) {
			if (getCurrentAction().left) {
				setCurrentAction(((Weapon) weapon.get()).getAttackAction(false));
			} else {
				setCurrentAction(((Weapon) weapon.get()).getAttackAction(true));
			}
		} else {
			if (getCurrentAction().left) {
				setCurrentAction(Action.ATTACK_LEFT_UNARMED);
			} else {
				setCurrentAction(Action.ATTACK_RIGHT_UNARMED);
			}
		}

		this.getIndividualsToBeAttacked().clear();
		this.getIndividualsToBeAttacked().addAll(individuals);

		return true;
	}


	@SuppressWarnings("rawtypes")
	public float getParryChance() {
		Optional<Item> weapon = Iterables.tryFind(getEquipped().keySet(), equipped -> {
			return equipped instanceof MeleeWeapon;
		});

		if (weapon.isPresent()) {
			return ((MeleeWeapon) weapon.get()).getParryChance();
		}

		return 0f;
	}


	@SuppressWarnings("rawtypes")
	public float getParryChanceIgnored() {
		Optional<Item> weapon = Iterables.tryFind(getEquipped().keySet(), equipped -> {
			return equipped instanceof MeleeWeapon;
		});

		if (weapon.isPresent()) {
			return ((MeleeWeapon) weapon.get()).getParryChanceIgnored();
		}

		return 0f;
	}


	@SuppressWarnings("rawtypes")
	public float getAttackPeriod() {
		float attackingPeriod = getDefaultAttackPeriod();
		Optional<Item> weapon = Iterables.tryFind(getEquipped().keySet(), equipped -> {
			return equipped instanceof Weapon;
		});

		if (weapon.isPresent()) {
			attackingPeriod = ((Weapon)weapon.get()).getBaseAttackPeriod();
		}

		return attackingPeriod;
	}

	protected abstract float getDefaultAttackPeriod();

	/**
	 * The actual attack, executed when the correct action frame's ParameterizedTask<Individual> is executed
	 */
	@SuppressWarnings("rawtypes")
	public void attack(boolean mine) {
		if (getIndividualsToBeAttacked().isEmpty()) {
			// Attack environmental objects... maybe?...Could be inefficient (must iterate through potentially lots of props), unless positional indexing is implemented....worth it?????
		} else {
			for (Integer individualId : getIndividualsToBeAttacked()) {
				Box attackingBox = getInteractionBox();
				Optional<Item> weapon = Iterables.tryFind(getEquipped().keySet(), equipped -> {
					return equipped instanceof Weapon;
				});

				Individual toBeAttacked = Domain.getIndividual(individualId);
				if (attackingBox.overlapsWith(toBeAttacked.getHitBox())) {
					if (weapon.isPresent()) {
						String floatingText = combat().target(toBeAttacked).withWeapon((Weapon) weapon.get()).execute();
						if (!StringUtils.isBlank(floatingText)) {
							toBeAttacked.addFloatingText(
								floatingText,
								Color.RED
							);
						}
					} else {
						String floatingText = combat().target(toBeAttacked).execute();
						if (!StringUtils.isBlank(floatingText)) {
							toBeAttacked.addFloatingText(
								floatingText,
								Color.RED
							);
						}
					}
				}
			}
		}
	}


	public void addFloatingText(String text, Color color) {
		UserInterface.addFloatingText(
			text,
			color,
			getState().position.cpy().add(0f, getHeight()).add(new Vector2(0, 15f).rotate(Util.getRandom().nextFloat() * 360f)),
			false
		);
	}


	private CombatChain combat() {
		return new CombatChain(this);
	}


	/**
	 * @return the position at which items are discarded from inventory, as well as bleeding
	 */
	public abstract Vector2 getEmissionPosition();


	@SuppressWarnings("rawtypes")
	public int getHitSound() {
		java.util.Optional<Item> meleeWeapon = getEquipped().keySet().stream().filter(item -> {return item instanceof MeleeWeapon;}).findFirst();

		if (meleeWeapon.isPresent()) {
			return ((MeleeWeapon) meleeWeapon.get()).getHitSound();
		}

		return 0;
	}


	@SuppressWarnings("rawtypes")
	public int getBlockSound() {
		java.util.Optional<Item> meleeWeapon = getEquipped().keySet().stream().filter(item -> {return item instanceof MeleeWeapon;}).findFirst();

		if (meleeWeapon.isPresent()) {
			return ((MeleeWeapon) meleeWeapon.get()).getBlockSound();
		}

		return 0;
	}


	@SuppressWarnings("rawtypes")
	public Box getAttackingHitBox() {
		Box attackingBox = null;
		Optional<Item> weapon = Iterables.tryFind(getEquipped().keySet(), equipped -> {
			return equipped instanceof Weapon;
		});

		if (weapon.isPresent()) {
			if (weapon.get() instanceof MeleeWeapon) {
				attackingBox = ((MeleeWeapon) weapon.get()).getActionFrameHitBox(this);
			}
		}

		if (attackingBox == null) {
			attackingBox = getDefaultAttackingHitBox();
		};

		return attackingBox;
	}


	/** Returns the damage dealt when attacking whilst not armed */
	public abstract float getUnarmedDamage();

	/** Returns the {@link Box} that will be used to calculate overlaps with other hitboxes, when no weapon-specific hitboxes are found */
	protected abstract Box getDefaultAttackingHitBox();

	/** Called during the update routine when the currentAction is attacking */
	protected abstract void respondToAttackCommand();

	/** Returns the map that maps from an {@link Action} to a map that maps action frames to their respective {@link Task}s */
	protected abstract Map<Action, Map<Integer, ParameterizedTask<Individual>>> getActionFrames();

	/** Returns the current {@link Animation} of this {@link Individual} */
	protected abstract List<WrapperForTwo<Animation, ShaderProgram>> getCurrentAnimation();

	/** Implementation-specific copy method of this {@link Individual} */
	public abstract Individual copy();


	/** Returns the {@link ArtificialIntelligence} implementation of this {@link Individual} */
	public ArtificialIntelligence getAI() {
		return this.ai;
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
		return controlledFactions.contains(getFactionId());
	}


	/** Setups up all individual resources */
	public static void setup() {
	}


	/** Renders the character and any other sprites */
	public void render() {
		int animationIndex = 0;

		// Draw the body, position is centre bottom of the frame
		List<WrapperForTwo<Animation, ShaderProgram>> currentAnimations = getCurrentAnimation();
		if (currentAnimations == null) {
			return;
		}

		spriteBatch.begin();
		for (WrapperForTwo<Animation, ShaderProgram> animation : currentAnimations) {

			// Render equipped items
			renderEquipment(animationIndex);

			spriteBatch.setShader(animation.b);
			animation.b.setUniformMatrix("u_projTrans", BloodAndMithrilClient.cam.combined);

			TextureRegion keyFrame = animation.a.getKeyFrame(getAnimationTimer(), true);
			spriteBatch.draw(
				keyFrame.getTexture(),
				getState().position.x - keyFrame.getRegionWidth()/2,
				getState().position.y,
				keyFrame.getRegionWidth(),
				keyFrame.getRegionHeight(),
				keyFrame.getRegionX(),
				keyFrame.getRegionY(),
				keyFrame.getRegionWidth(),
				keyFrame.getRegionHeight(),
				getCurrentAction().left(),
				false
			);

			animationIndex++;
		}
		Gdx.gl.glDisable(GL20.GL_BLEND);

		spriteBatch.end();
		spriteBatch.flush();
	}


	private void renderEquipment(int animationIndex) {
		for (Item equipped : getEquipped().keySet()) {
			if (((Equipable)equipped).getRenderingIndex(this) != animationIndex) {
				continue;
			}

			Equipable toRender = (Equipable) equipped;
			spriteBatch.setShader(Shaders.pass);
			if (equipped instanceof Weapon) {
				@SuppressWarnings({ "rawtypes", "unchecked" })
				WrapperForTwo<Animation, Vector2> attackAnimationEffects = ((Weapon) equipped).getAttackAnimationEffects(this);

				if (equipped instanceof OneHandedMeleeWeapon) {
					SpacialConfiguration config = getOneHandedWeaponSpatialConfigration();
					if (config != null) {
						Shaders.pass.setUniformMatrix("u_projTrans", BloodAndMithrilClient.cam.combined);
						Vector2 pos = config.position.add(getState().position);
						toRender.render(pos, config.orientation, config.flipX);
						if (config.flipX) {
							equipped.setPosition(pos.add(new Vector2(toRender.getTextureRegion().getRegionWidth()/2, 0).rotate(config.flipX ? config.orientation + 180f : -config.orientation)));
						} else {
							equipped.setPosition(pos.add(new Vector2(toRender.getTextureRegion().getRegionWidth()/2, 0).rotate(config.flipX ? -config.orientation : config.orientation)));
						}
					}
				}

				if (attackAnimationEffects != null) {
					TextureRegion keyFrame = attackAnimationEffects.a.getKeyFrame(getAnimationTimer());
					spriteBatch.draw(
						keyFrame.getTexture(),
						getState().position.x - keyFrame.getRegionWidth()/2 + (getCurrentAction().left() ? - attackAnimationEffects.b.x : attackAnimationEffects.b.x),
						getState().position.y  + attackAnimationEffects.b.y,
						keyFrame.getRegionWidth(),
						keyFrame.getRegionHeight(),
						keyFrame.getRegionX(),
						keyFrame.getRegionY(),
						keyFrame.getRegionWidth(),
						keyFrame.getRegionHeight(),
						getCurrentAction().left(),
						false
					);
				}
			} else if (equipped instanceof Armor) {

			}
		}
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
				getHitBox().position.x - getHitBox().width / 2,
				getHitBox().position.y - getHitBox().height / 2,
				getHitBox().width,
				getHitBox().height
			);
			shapeRenderer.end();
			shapeRenderer.setProjectionMatrix(UserInterface.UICamera.combined);
		}

		if (isMouseOver() && Gdx.input.isKeyPressed(KeyMappings.attack) && !Gdx.input.isKeyPressed(KeyMappings.rangedAttack)) {
			if (Domain.getSelectedIndividuals().size() > 0 && (!Domain.getSelectedIndividuals().contains(this) || Domain.getSelectedIndividuals().size() > 1)) {
				spriteBatch.setShader(Shaders.filter);
				Shaders.filter.setUniformMatrix("u_projTrans", UserInterface.UICamera.combined);
				Shaders.filter.setUniformf("color", Color.BLACK);
				Fonts.defaultFont.draw(spriteBatch, "Attack Melee", getMouseScreenX() + 14, getMouseScreenY() - 26);
				spriteBatch.flush();
				Shaders.filter.setUniformf("color", Color.ORANGE);
				Fonts.defaultFont.draw(spriteBatch, "Attack Melee", getMouseScreenX() + 15, getMouseScreenY() - 25);
			}
		}
	}


	/** Select this {@link Individual} */
	public void select(int clientId) {
		getAI().setToManual();
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


	/**
	 * Updates the individual
	 */
	public void update(float delta) {
		float aiTaskDelay = 0.05f;
		travelIconTimer += 0.15f;

		// If chunk has not yet been loaded, do not update
		try {
			Domain.getWorld(worldId).getTopography().getTile(state.position, true);
		} catch (NoTileFoundException e) {
			return;
		}

		// Update interaction box location
		interactionBox.position.x = state.position.x;
		interactionBox.position.y = state.position.y + getHeight() / 2;

		// Update hitbox location
		getHitBox().position.x = state.position.x;
		getHitBox().position.y = state.position.y + getHeight() / 2;

		if (combatTimer <= 0f) {
			setCombatStance(false);
		}

		if (inCombatStance() && !(getAI().getCurrentTask() instanceof Attack)) {
			combatTimer -= delta;
		}

		aiReactionTimer += delta;
		if (aiReactionTimer >= aiTaskDelay) {
			getAI().update(aiTaskDelay);
			aiReactionTimer = 0f;
		}

		setAnimationTimer(getAnimationTimer() + delta);
		attackTimer += delta;

		if (isAlive()) {
			updateVitals(delta);
		}

		synchronized (beingAttackedBy) {
			Sets.newHashSet(beingAttackedBy.keySet()).stream().forEach(i -> {
				Individual individual = Domain.getIndividual(i);
				if (beingAttackedBy.get(i) <= System.currentTimeMillis() - round(individual.getAttackPeriod() * 1000D) - 1000L) {
					beingAttackedBy.remove(i);
				} else {
					AITask currentTask = individual.getAI().getCurrentTask();
					if (currentTask instanceof Attack) {
						if (!((Attack) currentTask).getTargets().contains(getId().getId())) {
							beingAttackedBy.remove(i);
						}
					} else {
						beingAttackedBy.remove(i);
					}
				}
			});
		}

		internalUpdate(delta);

		try {
			executeActionFrames();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		respondToCommands();
		respondToAttackCommand();

		try {
			Kinematics.kinetics(delta, Domain.getWorld(getWorldId()), this);
		} catch (NoTileFoundException e) {}

		updateConditions(delta);
		updatePositionalIndex();

		for (Item equipped : getEquipped().keySet()) {
			equipped.affixEffects(this);
		}
	}


	public void updatePositionalIndex() {
		for (PositionalIndexNode node : Domain.getWorld(worldId).getPositionalIndexMap().getNearbyNodes(state.position.x, state.position.y)) {
			node.removeIndividual(id.getId());
		}

		Domain.getWorld(worldId).getPositionalIndexMap().get(state.position.x, state.position.y).addIndividual(id.getId());
	}


	/**
	 * Updates the vitals of this {@link Individual}
	 */
	private void updateVitals(float delta) {
		heal(delta * getState().healthRegen);

		decreaseHunger(hungerDrain());
		decreaseThirst(thirstDrain());

		if (isWalking()) {
			if (isCommandActive(KeyMappings.moveLeft) || isCommandActive(KeyMappings.moveRight)) {
				increaseStamina(delta * getState().staminaRegen / 2f);
			} else {
				increaseStamina(delta * getState().staminaRegen);
			}
		} else {
			if (isCommandActive(KeyMappings.moveLeft) || isCommandActive(KeyMappings.moveRight)) {
				decreaseStamina(staminaDrain());
			} else {
				increaseStamina(delta * getState().staminaRegen);
			}
		}

		if (getState().hunger < 0.75f) {
			addCondition(new Hunger(getId().getId()));
		}

		if (getState().thirst < 0.75f) {
			addCondition(new Thirst(getId().getId()));
		}

		if (getState().stamina < 0.75f) {
			addCondition(new Exhaustion(getId().getId()));
		}
	}

	/** The amount of hunger drained, per update tick (1/60) of a second, max hunger is 1 */
	protected abstract float hungerDrain();

	/** The amount of thirst drained, per update tick (1/60) of a second, max thirst is 1 */
	protected abstract float thirstDrain();

	/** The amount of stamina drained, per update tick (1/60) of a second, max stamina is 1 */
	protected abstract float staminaDrain();

	/**
	 * Performs the {@link Task} associated with the current frame of the animation of the current {@link Action}
	 */
	private void executeActionFrames() {
		ParameterizedTask<Individual> task = null;
		try {
			task = getActionFrames()
				.get(getCurrentAction())
				.get(getCurrentAnimation().get(0).a.getKeyFrameIndex(animationTimer));
		} catch (NullPointerException e) {
			// Do nothing
		}

		if (previousActionFrameAction == getCurrentAction() && previousActionFrame == getCurrentAnimation().get(0).a.getKeyFrameIndex(animationTimer)) {
			return;
		}

		if (task != null) {
			task.execute(this);
		}

		previousActionFrame = getCurrentAnimation().get(0).a.getKeyFrameIndex(animationTimer);
		previousActionFrameAction = getCurrentAction();
	}


	/**
	 * Update how this {@link Individual} is affected by its {@link Condition}s
	 */
	private void updateConditions(float delta) {
		// Reset regeneration values
		if (isServer()) {
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

		if (isClient()) {
			for (Condition condition : newArrayList(state.currentConditions)) {
				condition.clientSideEffects(this, delta);
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
		MenuItem showInfoMenuItem = showInfo();
		MenuItem showStatusWindowItem = showStatus();

		final ContextMenu controlMenu = actions();
		MenuItem actions = new MenuItem(
			"Actions",
			() -> {
				controlMenu.x = getMouseScreenX();
				controlMenu.y = getMouseScreenY();
			},
			Color.ORANGE,
			getToolTipTextColor(),
			Color.GRAY,
			controlMenu
		);

		final ContextMenu interactMenu = interactMenu();
		MenuItem interact = new MenuItem(
			"Interact",
			() -> {
				interactMenu.x = getMouseScreenX();
				interactMenu.y = getMouseScreenY();
			},
			Color.GREEN,
			Colors.UI_DARK_GREEN,
			Color.GRAY,
			interactMenu
		);

		final ContextMenu editMenu = editSubMenu();
		MenuItem edit = new MenuItem(
			"Edit",
			() -> {
				editMenu.x = getMouseScreenX();
				editMenu.y = getMouseScreenY();
			},
			Color.ORANGE,
			getToolTipTextColor(),
			Color.GRAY,
			editMenu
		);

		ContextMenu contextMenuToReturn = new ContextMenu(0, 0, true);
		if (!Domain.getSelectedIndividuals().isEmpty() && !(Domain.getSelectedIndividuals().size() == 1 && Domain.getSelectedIndividuals().contains(this))) {
			contextMenuToReturn.addMenuItem(interact);
		}

		contextMenuToReturn.addMenuItem(showInfoMenuItem);
		contextMenuToReturn.addMenuItem(showStatusWindowItem);

		if (isControllable()) {
			contextMenuToReturn.addMenuItem(actions);
			contextMenuToReturn.addMenuItem(edit);
		}

		for (MenuItem item : internalGetContextMenuItems()) {
			contextMenuToReturn.addMenuItem(item);
		}

		return contextMenuToReturn;
	}


	private ContextMenu actions() {
		return new ContextMenu(0, 0,
			true,
			selectDeselect(this),
			inventory(),
			skills(),
			build()
		);
	}


	private ContextMenu interactMenu() {
		return new ContextMenu(0, 0,
			true,
			trade(this),
			follow(this),
			attack(this)
		);
	}
	
	
	private MenuItem skills() {
		return new MenuItem(
		"Skills",
		() -> {
			UserInterface.addLayeredComponentUnique(
				new SkillsWindow(Individual.this)
			);
		},
		Color.WHITE,
		getToolTipTextColor(),
		Color.GRAY,
		null
	);
	}


	private MenuItem showStatus() {
		return new MenuItem(
			"Show status",
			() -> {
				UserInterface.addLayeredComponentUnique(
					new IndividualStatusWindow(
						Individual.this,
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
	}


	private MenuItem follow(final Individual thisIndividual) {
		return new MenuItem(
			"Follow",
			() -> {
				for (Individual indi : Domain.getSelectedIndividuals()) {
					if (indi != thisIndividual) {
						if (isServer()) {
							indi.getAI().setCurrentTask(
								new Follow(indi, thisIndividual, 10, null)
							);
						} else {
							ClientServerInterface.SendRequest.sendFollowRequest(indi, thisIndividual);
						}
					}
				}
			},
			Color.WHITE,
			getToolTipTextColor(),
			Color.GRAY,
			null
		);
	}


	private MenuItem attack(final Individual thisIndividual) {
		return new MenuItem(
			"Attack",
				() -> {
					for (Individual indi : Domain.getSelectedIndividuals()) {
						if (indi != thisIndividual) {
							if (isServer()) {
								indi.getAI().setCurrentTask(
									new Attack(indi, thisIndividual)
								);
							} else {
								ClientServerInterface.SendRequest.sendRequestAttack(indi, thisIndividual);
							}
						}
					}
				},
			Color.RED,
			getToolTipTextColor(),
			Colors.UI_DARK_ORANGE,
			null
		);
	}


	private MenuItem trade(final Individual thisIndividual) {
		return new MenuItem(
			"Trade with",
			() -> {
				if (Domain.getSelectedIndividuals().size() > 1) {
					return;
				}

				for (Individual indi : Domain.getSelectedIndividuals()) {
					if (isServer()) {
						if (indi != thisIndividual) {
							indi.getAI().setCurrentTask(
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
			new ContextMenu(0, 0,
				true,
				new MenuItem(
					"You have multiple individuals selected",
					() -> {},
					Colors.UI_GRAY,
					Colors.UI_GRAY,
					Colors.UI_GRAY,
					null
				)
			),
			() -> {
				return Domain.getSelectedIndividuals().size() > 1;
			}
		);
	}


	private MenuItem inventory() {
		final Individual thisIndividual = this;

		return new MenuItem(
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
	}


	private MenuItem build() {
		final Individual thisIndividual = this;

		return new MenuItem(
			"Build",
			() -> {
				BuildWindow window = new BuildWindow(
					WIDTH / 2 - 150,
					HEIGHT/2 + 100,
					thisIndividual,
					new Function<Construction, String>() {
						@Override
						public String apply(Construction input) {
							return input.getTitle();
						}
					},
					(c1, c2) -> {
						return c1.getTitle().compareTo(c2.getTitle());
					}
				);

				UserInterface.addLayeredComponentUnique(window);
			},
			Color.WHITE,
			getToolTipTextColor(),
			Color.GRAY,
			null
		);
	}


	private ContextMenu editSubMenu() {
		final Individual thisIndividual = this;

		return new ContextMenu(0, 0,
			true,
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
	}


	/**
	 * @return The show info {@link MenuItem} for this individual
	 */
	private MenuItem showInfo() {
		final Individual thisIndividual = this;

		return new MenuItem(
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
	}


	/**
	 * @return The {@link MenuItem} to select/deselect this individual
	 */
	private MenuItem selectDeselect(final Individual thisIndividual) {
		return Domain.getSelectedIndividuals().contains(thisIndividual) ?
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
	}


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
		// TODO Killing an individual
		dead = true;
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
			combatTimer = 5f;
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
		synchronized (beingAttackedBy) {
			if (beingAttackedBy.containsKey(by.getId().getId())) {
				return true;
			} else {
				int totalConcurrentAttackNumber = beingAttackedBy.keySet().stream().mapToInt(i -> {
					return Domain.getIndividual(i).getConcurrentAttackNumber();
				}).sum();

				return totalConcurrentAttackNumber + by.getConcurrentAttackNumber() <= this.maxConcurrentAttackers;
			}
		}
	}


	public void addAttacker(Individual attacker) {
		if (canBeAttacked(attacker)) {
			this.beingAttackedBy.put(attacker.getId().getId(), System.currentTimeMillis());
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
					firingVector.cpy().nor().mul(
						Math.min(
							1f,
							firingVector.len() / 150f
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


	public void speak(String text, long duration) {
		if (ClientServerInterface.isServer()) {
			UserInterface.addTextBubble(
				text,
				new ReturnIndividualPosition(this),
				duration,
				0,
				getHeight() + 40
			);
		}
	}
}