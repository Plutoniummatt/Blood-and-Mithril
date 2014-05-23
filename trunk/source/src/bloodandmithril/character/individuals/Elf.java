package bloodandmithril.character.individuals;

import java.util.HashMap;
import java.util.List;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.implementations.ElfAI;
import bloodandmithril.character.ai.task.Idle;
import bloodandmithril.character.conditions.Exhaustion;
import bloodandmithril.character.conditions.Hunger;
import bloodandmithril.character.conditions.Thirst;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.item.items.equipment.weapon.OneHandedWeapon;
import bloodandmithril.ui.KeyMappings;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.util.AnimationHelper;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.SpacialConfiguration;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.util.datastructure.DualKeyHashMap;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Topography;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;

/**
 * Exceptional at:
 *
 * - Archery
 * - Walking/Running on water
 * - Infinite stamina
 * - Longer natural lifespan
 * - Better vision
 * - Natural wound healing
 * - Better at magic
 *
 * Disadvantages:
 *
 * - Low hitpoints
 * - Vegetarians
 * - Technophobes
 * - Can't carry much
 *
 * @author Matt
 */
public class Elf extends Individual {

	private static final String RUNNING_LEFT_HAIR = "runningLeftHair";
	private static final String RUNNING_RIGHT_HAIR = "runningRightHair";
	private static final String WALKING_LEFT_HAIR = "walkingLeftHair";
	private static final String WALKING_RIGHT_HAIR = "walkingRightHair";
	private static final String STANDING_LEFT_HAIR = "standingLeftHair";
	private static final String STANDING_RIGHT_HAIR = "standingRightHair";

	private static final String RUNNING_RIGHT = "runningRight";
	private static final String RUNNING_LEFT = "runningLeft";
	private static final String WALKING_RIGHT = "walkingRight";
	private static final String WALKING_LEFT = "walkingLeft";
	private static final String WALKING_RIGHT_COMBAT = "walkingRightCombat";
	private static final String WALKING_LEFT_COMBAT = "walkingLeftCombat";
	private static final String STANDING_RIGHT = "standingRight";
	private static final String STANDING_LEFT = "standingLeft";
	private static final String STANDING_RIGHT_COMBAT = "standingRightCombat";
	private static final String STANDING_LEFT_COMBAT = "standingLeftCombat";

	private static final long serialVersionUID = -5566954059579973505L;

	/** Hair color of this {@link Elf} */
	private float hairColorR, hairColorG, hairColorB;

	/** Eye color of this {@link Elf} */
	private float eyeColorR, eyeColorG, eyeColorB;

	/** Stylish */
	private int hairStyle;

	/** True if female */
	private boolean female;

	/** Animations */
	private static DualKeyHashMap<String, Integer, Animation> hairAnimations = new DualKeyHashMap<String, Integer, Animation>();
	private static HashMap<String, Animation> animations = new HashMap<String, Animation>();

	/** Current animations */
	private String current, currentHair;

	/** Biography of this Elf */
	private String biography = "";

	/**
	 * Constructor
	 */
	public Elf(IndividualIdentifier id, IndividualState state, int factionId, boolean female, Color hairColor, Color eyeColor, int hairStyle, float capacity, World world) {
		super(id, state, factionId, capacity, 10, 32, 75, 30, new Box(new Vector2(state.position.x, state.position.y), 120, 120), world.getWorldId(), 2);
		this.female = female;
		this.hairColorR = hairColor.r;
		this.hairColorG = hairColor.g;
		this.hairColorB = hairColor.b;
		this.eyeColorR = eyeColor.r;
		this.eyeColorG = eyeColor.g;
		this.eyeColorB = eyeColor.b;
		this.hairStyle = hairStyle;

		this.ai = new ElfAI(this);

		current = STANDING_RIGHT;
		currentHair = STANDING_RIGHT_HAIR;
	}


	/**
	 * Constructor
	 */
	private Elf(IndividualIdentifier id, IndividualState state, int factionId, boolean female, Color hairColor, Color eyeColor, int hairStyle, float capacity, int worldId) {
		super(id, state, factionId, capacity, 10, 32, 75, 30, new Box(new Vector2(state.position.x, state.position.y), 120, 120), worldId, 2);
		this.female = female;
		this.hairColorR = hairColor.r;
		this.hairColorG = hairColor.g;
		this.hairColorB = hairColor.b;
		this.eyeColorR = eyeColor.r;
		this.eyeColorG = eyeColor.g;
		this.eyeColorB = eyeColor.b;
		this.hairStyle = hairStyle;

		this.ai = new ElfAI(this);

		current = STANDING_RIGHT;
		currentHair = STANDING_RIGHT_HAIR;
	}


	/**
	 * Loads animations
	 */
	public static void loadAnimations() {
		for (int i = 0; i <=3; i++) {
			hairAnimations.put(STANDING_RIGHT_HAIR + "F", i, AnimationHelper.makeAnimation(Domain.individualTexture, 336 + i * 48, 0, 48, 80, 1, 1));
			hairAnimations.put(STANDING_LEFT_HAIR + "F", i, AnimationHelper.makeAnimation(Domain.individualTexture, 336 + i * 48, 80, 48, 80, 1, 1));
			hairAnimations.put(WALKING_RIGHT_HAIR + "F", i, AnimationHelper.makeAnimation(Domain.individualTexture, 336 + i * 48, 0, 48, 80, 1, 1));
			hairAnimations.put(WALKING_LEFT_HAIR + "F", i, AnimationHelper.makeAnimation(Domain.individualTexture, 336 + i * 48, 80, 48, 80, 1, 1));
			hairAnimations.put(RUNNING_RIGHT_HAIR + "F", i, AnimationHelper.makeAnimation(Domain.individualTexture, 336+ i * 48, 0, 48, 80, 1, 1));
			hairAnimations.put(RUNNING_LEFT_HAIR + "F", i, AnimationHelper.makeAnimation(Domain.individualTexture, 336 + i * 48, 80, 48, 80, 1, 1));
		}

		animations.put(STANDING_LEFT_COMBAT + "F", AnimationHelper.makeAnimation(Domain.individualTexture, 0, 400, 48, 80, 1, 1));
		animations.put(STANDING_RIGHT_COMBAT + "F", AnimationHelper.makeAnimation(Domain.individualTexture, 0, 320, 48, 80, 1, 1));
		animations.put(WALKING_LEFT_COMBAT + "F", AnimationHelper.makeAnimation(Domain.individualTexture, 48, 400, 48, 80, 6, 0.17f));
		animations.put(WALKING_RIGHT_COMBAT + "F", AnimationHelper.makeAnimation(Domain.individualTexture, 48, 320, 48, 80, 6, 0.17f));
		animations.put(STANDING_LEFT + "F", AnimationHelper.makeAnimation(Domain.individualTexture, 0, 80, 48, 80, 1, 1));
		animations.put(STANDING_RIGHT + "F", AnimationHelper.makeAnimation(Domain.individualTexture, 0, 0, 48, 80, 1, 1));
		animations.put(WALKING_LEFT + "F", AnimationHelper.makeAnimation(Domain.individualTexture, 48, 80, 48, 80, 6, 0.17f));
		animations.put(WALKING_RIGHT + "F", AnimationHelper.makeAnimation(Domain.individualTexture, 48, 0, 48, 80, 6, 0.17f));
		animations.put(RUNNING_LEFT + "F", AnimationHelper.makeAnimation(Domain.individualTexture, 0, 240, 48, 80, 7, 0.14f));
		animations.put(RUNNING_RIGHT + "F", AnimationHelper.makeAnimation(Domain.individualTexture, 0, 160, 48, 80, 7, 0.14f));
	}


	@Override
	protected void internalRender() {
		BloodAndMithrilClient.spriteBatch.begin();

		// Determine which shader we're using, normal, or highlighted
		if (isMouseOver()) {

			Shaders.elfHighLight.begin();
			Shaders.elfHighLight.setUniformi("hair", 0);
			Shaders.elfHighLight.setUniformf("eyeColor", eyeColorR, eyeColorG, eyeColorB);
			Shaders.elfHighLight.setUniformf("alpha", 1f);
			Shaders.elfHighLight.setUniformf("hairColor", hairColorR, hairColorG, hairColorB);
			Shaders.elfHighLight.end();
			BloodAndMithrilClient.spriteBatch.setShader(Shaders.elfHighLight);
			Shaders.elfHighLight.setUniformMatrix("u_projTrans", BloodAndMithrilClient.cam.combined);
		} else {

			Shaders.elfDayLight.begin();
			Shaders.elfDayLight.setUniformi("hair", 0);
			Shaders.elfDayLight.setUniformf("eyeColor", eyeColorR, eyeColorG, eyeColorB);
			Shaders.elfDayLight.setUniformf("alpha", 1f);
			Shaders.elfDayLight.setUniformf("hairColor", hairColorR, hairColorG, hairColorB);
			Shaders.elfDayLight.end();
			BloodAndMithrilClient.spriteBatch.setShader(Shaders.elfDayLight);
			Shaders.elfDayLight.setUniformMatrix("u_projTrans", BloodAndMithrilClient.cam.combined);
		}

		// Draw the body
		BloodAndMithrilClient.spriteBatch.draw(
			animations.get(current + (female ? "F" : "M")).getKeyFrame(animationTimer, true),
			getState().position.x - animations.get(current + (female ? "F" : "M")).getKeyFrame(0f).getRegionWidth()/2,
			getState().position.y
		);
		BloodAndMithrilClient.spriteBatch.end();

		// Change draw mode to hair
		BloodAndMithrilClient.spriteBatch.begin();
		Shaders.elfHighLight.setUniformi("hair", 1);
		Shaders.elfDayLight.setUniformi("hair", 1);
		Shaders.elfDayLight.setUniformMatrix("u_projTrans", BloodAndMithrilClient.cam.combined);
		Shaders.elfHighLight.setUniformMatrix("u_projTrans", BloodAndMithrilClient.cam.combined);

		// Draw the hair
		BloodAndMithrilClient.spriteBatch.draw(
			hairAnimations.get(currentHair + (female ? "F" : "M"), hairStyle).getKeyFrame(animationTimer, true),
			getState().position.x - hairAnimations.get(currentHair + (female ? "F" : "M"), hairStyle).getKeyFrame(0f).getRegionWidth()/2,
			getState().position.y
		);
		BloodAndMithrilClient.spriteBatch.end();


		// Render equipped items
		BloodAndMithrilClient.spriteBatch.begin();
		Shaders.elfHighLight.setUniformi("hair", 0);
		Shaders.elfDayLight.setUniformi("hair", 0);
		Shaders.elfDayLight.setUniformMatrix("u_projTrans", BloodAndMithrilClient.cam.combined);
		Shaders.elfHighLight.setUniformMatrix("u_projTrans", BloodAndMithrilClient.cam.combined);
		for (Item equipped : getEquipped().keySet()) {
			Equipable toRender = (Equipable) equipped;

			if (equipped instanceof OneHandedWeapon) {
				SpacialConfiguration config = getOneHandedWeaponSpacialConfigration();
				if (config != null) {
					toRender.render(config.position.add(getState().position), config.orientation, config.flipX);
				}
			}
		}

		BloodAndMithrilClient.spriteBatch.end();
		BloodAndMithrilClient.spriteBatch.flush();
	}


	/** What animation should we use? */
	private void updateAnimation(String walkingAnimationLeft, String walkingAnimationRight, String standingLeft, String standingRight) {
		// If we're moving to the right
		if (getState().velocity.x > 0) {
			// If walking, and current animation is not walking right, then set animations to walking right
			if (isCommandActive(KeyMappings.walk) && !current.equals(walkingAnimationRight)) {
				current = walkingAnimationRight;
				currentHair = WALKING_RIGHT_HAIR;
				animationTimer = 0f;
			} else if (!isCommandActive(KeyMappings.walk) && !current.equals(RUNNING_RIGHT)) {
				// Otherwise if running, and current animation is not running right, then set animations to running right
				current = RUNNING_RIGHT;
				currentHair = RUNNING_RIGHT_HAIR;
				animationTimer = 0f;
			}

		// Same for if we're moving left
		} else if (getState().velocity.x < 0) {
			if (isCommandActive(KeyMappings.walk) && !current.equals(walkingAnimationLeft)) {
				current = walkingAnimationLeft;
				currentHair = WALKING_LEFT_HAIR;
				animationTimer = 0f;
			} else if (!isCommandActive(KeyMappings.walk) && !current.equals(RUNNING_LEFT)) {
				current = RUNNING_LEFT;
				currentHair = RUNNING_LEFT_HAIR;
				animationTimer = 0f;
			}

		// Otherwise we're standing still, if current animation is not standing left or right, then set current to standing left/right depending on which direction we were facing before.
		} else if (getState().velocity.x == 0 && !current.equals(standingLeft) && !current.equals(standingRight)) {
			current = current.equals(walkingAnimationRight) || current.equals(RUNNING_RIGHT) || current.equals(STANDING_RIGHT) || current.equals(STANDING_RIGHT_COMBAT) ? standingRight : standingLeft;
			currentHair = currentHair.equals(WALKING_RIGHT_HAIR) || currentHair.equals(RUNNING_RIGHT_HAIR) || current.equals(STANDING_RIGHT) || current.equals(STANDING_RIGHT_COMBAT) ? STANDING_RIGHT_HAIR : STANDING_LEFT_HAIR;
		}
	}


	@Override
	protected void internalUpdate(float delta) {
		if (combatStance) {
			updateAnimation(WALKING_LEFT_COMBAT, WALKING_RIGHT_COMBAT, STANDING_LEFT_COMBAT, STANDING_RIGHT_COMBAT);
		} else {
			updateAnimation(WALKING_LEFT, WALKING_RIGHT, STANDING_LEFT, STANDING_RIGHT);
		}

		if (ClientServerInterface.isServer()) {
			updateVitals(delta);
		}
	}


	private void updateVitals(float delta) {
		heal(delta * getState().healthRegen);

		decreaseHunger(0.000001f);
		decreaseThirst(0.000003f);

		if (!isWalking()) {
			if (isCommandActive(KeyMappings.moveLeft) || isCommandActive(KeyMappings.moveRight)) {
				decreaseStamina(0.0005f);
			} else {
				increaseStamina(delta * getState().staminaRegen);
			}
		} else {
			if (isCommandActive(KeyMappings.moveLeft) || isCommandActive(KeyMappings.moveRight)) {
				increaseStamina(delta * getState().staminaRegen / 2);
			} else {
				increaseStamina(0.001f);
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


	@Override
	protected void respondToCommands() {
		//Horizontal movement
		Topography topography = Domain.getWorld(getWorldId()).getTopography();
		if (Math.abs(getState().velocity.y) < 5f) {
			if (isCommandActive(KeyMappings.moveLeft) && (canStepUp(-2, topography) || !obstructed(-2, topography))) {
				if (isCommandActive(KeyMappings.walk)) {
					getState().velocity.x = -30f;
				} else {
					getState().velocity.x = -80f;
				}
			} else if (isCommandActive(KeyMappings.moveRight) && (canStepUp(2, topography) || !obstructed(2, topography))) {
				if (isCommandActive(KeyMappings.walk)) {
					getState().velocity.x = 30f;
				} else {
					getState().velocity.x = 80f;
				}
			} else {
				getState().velocity.x = 0f;
				getState().acceleration.x = 0f;

				int offset = isCommandActive(KeyMappings.moveRight) ? 2 : isCommandActive(KeyMappings.moveLeft) ? -2 : 0;
				if (obstructed(offset, topography) && !canStepUp(offset, topography) && !(ai.getCurrentTask() instanceof Idle)) {
					ai.setCurrentTask(new Idle());
				}

				sendCommand(KeyMappings.moveRight, false);
				sendCommand(KeyMappings.moveLeft, false);
				sendCommand(KeyMappings.walk, false);
			}
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
		return new Color(hairColorR, hairColorG, hairColorB, 1f);
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
		int keyFrameIndex = animations.get(current + (female ? "F" : "M")).getKeyFrameIndex(animationTimer);

		switch (current) {
			case WALKING_LEFT:
				switch (keyFrameIndex) {
					case 0:
						return new SpacialConfiguration(new Vector2(5, 21), 0f, true);
					case 1:
						return new SpacialConfiguration(new Vector2(7, 19), 15f, true);
					case 2:
						return new SpacialConfiguration(new Vector2(3, 21), 0f, true);
					case 3:
						return new SpacialConfiguration(new Vector2(-6, 24), -20f, true);
					case 4:
						return new SpacialConfiguration(new Vector2(-11, 31), -45f, true);
					case 5:
						return new SpacialConfiguration(new Vector2(-6, 24), -20f, true);
					default:
						throw new RuntimeException("Unexpected keyframe index");
				}

			case WALKING_RIGHT:
				switch (keyFrameIndex) {
					case 0:
						return new SpacialConfiguration(new Vector2(-5, 21), 0f, false);
					case 1:
						return new SpacialConfiguration(new Vector2(-7, 19), -15f, false);
					case 2:
						return new SpacialConfiguration(new Vector2(-3, 21), 0f, false);
					case 3:
						return new SpacialConfiguration(new Vector2(6, 24), 20f, false);
					case 4:
						return new SpacialConfiguration(new Vector2(11, 31), 45f, false);
					case 5:
						return new SpacialConfiguration(new Vector2(6, 24), 20f, false);
					default:
						throw new RuntimeException("Unexpected keyframe index");
				}

			case STANDING_LEFT:
				switch (keyFrameIndex) {
				case 0:
					return new SpacialConfiguration(new Vector2(1, 20), 0f, true);
				default:
					throw new RuntimeException("Unexpected keyframe index");
				}

			case STANDING_RIGHT:
				switch (keyFrameIndex) {
				case 0:
					return new SpacialConfiguration(new Vector2(1, 20), 0f, false);
				default:
					throw new RuntimeException("Unexpected keyframe index");
				}

			case STANDING_RIGHT_COMBAT:
			case WALKING_RIGHT_COMBAT:
				switch (keyFrameIndex) {
				default:
					return new SpacialConfiguration(new Vector2(-13, 29), -60f, true);
				}

			case STANDING_LEFT_COMBAT:
			case WALKING_LEFT_COMBAT:
				switch (keyFrameIndex) {
				default:
					return new SpacialConfiguration(new Vector2(13, 29), 60f, false);
				}
		}
		return null;
	}


	@Override
	protected void internalCopyFrom(Individual other) {
		if (!(other instanceof Elf)) {
			throw new RuntimeException("Cannot cast " + other.getClass().getSimpleName() + " to Elf.");
		}

		this.hairColorB = ((Elf) other).hairColorB;
		this.hairColorG = ((Elf) other).hairColorG;
		this.hairColorR = ((Elf) other).hairColorR;
		this.eyeColorB = ((Elf) other).eyeColorB;
		this.eyeColorG = ((Elf) other).eyeColorG;
		this.eyeColorR = ((Elf) other).eyeColorR;
		this.hairStyle = ((Elf) other).hairStyle;
		this.female = ((Elf) other).female;
		this.current = ((Elf) other).current;
		this.currentHair = ((Elf) other).currentHair;
		this.biography = ((Elf) other).biography;
	}


	@Override
	public Individual copy() {
		Elf elf = new Elf(getId(), getState(), factionId, female, new Color(hairColorR, hairColorG, hairColorB, 1f), new Color(eyeColorR, eyeColorG, eyeColorB, 1f), hairStyle, getMaxCapacity(), getWorldId());
		elf.copyFrom(this);
		return elf;
	}
}