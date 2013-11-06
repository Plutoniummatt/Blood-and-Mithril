package bloodandmithril.character.individuals;

import java.util.HashMap;

import bloodandmithril.Fortress;
import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.implementations.ElfAI;
import bloodandmithril.character.ai.task.Idle;
import bloodandmithril.item.Item;
import bloodandmithril.item.equipment.Equipable;
import bloodandmithril.item.equipment.OneHandedWeapon;
import bloodandmithril.ui.KeyMappings;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.ContextMenuItem;
import bloodandmithril.ui.components.window.IndividualInfoWindow;
import bloodandmithril.ui.components.window.InventoryWindow;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.util.AnimationHelper;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.SpacialConfiguration;
import bloodandmithril.util.Task;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.util.datastructure.DualKeyHashMap;
import bloodandmithril.world.GameWorld;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Vector2;

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
	private static final String STANDING_RIGHT = "standingRight";
	private static final String STANDING_LEFT = "standingLeft";

	private static final long serialVersionUID = -5566954059579973505L;

	/** Hair color of this {@link Elf} */
	private final float hairColorR, hairColorG, hairColorB;

	/** Eye color of this {@link Elf} */
	private final float eyeColorR, eyeColorG, eyeColorB;

	/** Stylish */
	private final int hairStyle;

	/** True if female */
	private final boolean female;

	/** Animations */
	private static DualKeyHashMap<String, Integer, Animation> hairAnimations = new DualKeyHashMap<String, Integer, Animation>();
	private static HashMap<String, Animation> animations = new HashMap<String, Animation>();

	/** Current animations */
	private String current, currentHair;

	/** Biography of this Elf */
	private String biography = "Elves are cool";

	/**
	 * Constructor
	 */
	public Elf(IndividualIdentifier id, IndividualState state, boolean controllable, boolean female, Color hairColor, Color eyeColor, int hairStyle, float capacity) {
		super(id, state, controllable, 0.05f, capacity, 32, 75, 30, new Box(new Vector2(state.position.x, state.position.y), 120, 120));
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
			hairAnimations.put(STANDING_RIGHT_HAIR + "F", i, AnimationHelper.makeAnimation(GameWorld.individualTexture, 448 + i * 64, 0, 64, 128, 1, 1));
			hairAnimations.put(STANDING_LEFT_HAIR + "F", i, AnimationHelper.makeAnimation(GameWorld.individualTexture, 448 + i * 64, 128, 64, 128, 1, 1));
			hairAnimations.put(WALKING_RIGHT_HAIR + "F", i, AnimationHelper.makeAnimation(GameWorld.individualTexture, 448 + i * 64, 0, 64, 128, 1, 1));
			hairAnimations.put(WALKING_LEFT_HAIR + "F", i, AnimationHelper.makeAnimation(GameWorld.individualTexture, 448 + i * 64, 128, 64, 128, 1, 1));
			hairAnimations.put(RUNNING_RIGHT_HAIR + "F", i, AnimationHelper.makeAnimation(GameWorld.individualTexture, 448 + i * 64, 256, 64, 128, 1, 1));
			hairAnimations.put(RUNNING_LEFT_HAIR + "F", i, AnimationHelper.makeAnimation(GameWorld.individualTexture, 448 + i * 64, 384, 64, 128, 1, 1));
		}

		animations.put(STANDING_LEFT + "F", AnimationHelper.makeAnimation(GameWorld.individualTexture, 0, 128, 64, 128, 1, 1));
		animations.put(STANDING_RIGHT + "F", AnimationHelper.makeAnimation(GameWorld.individualTexture, 0, 0, 64, 128, 1, 1));
		animations.put(WALKING_LEFT + "F", AnimationHelper.makeAnimation(GameWorld.individualTexture, 64, 128, 64, 128, 6, 0.17f));
		animations.put(WALKING_RIGHT + "F", AnimationHelper.makeAnimation(GameWorld.individualTexture, 64, 0, 64, 128, 6, 0.17f));
		animations.put(RUNNING_LEFT + "F", AnimationHelper.makeAnimation(GameWorld.individualTexture, 0, 384, 64, 128, 7, 0.14f));
		animations.put(RUNNING_RIGHT + "F", AnimationHelper.makeAnimation(GameWorld.individualTexture, 0, 256, 64, 128, 7, 0.14f));
	}


	@Override
	protected void internalRender() {
		Fortress.spriteBatch.begin();

		// Determine which shader we're using, normal, or highlighted
		if (isMouseOver()) {

			Shaders.elfHighLight.begin();
			Shaders.elfHighLight.setUniformi("hair", 0);
			Shaders.elfHighLight.setUniformf("eyeColor", eyeColorR, eyeColorG, eyeColorB);
			Shaders.elfHighLight.setUniformf("alpha", 1f);
			Shaders.elfHighLight.setUniformf("hairColor", hairColorR, hairColorG, hairColorB);
			Shaders.elfHighLight.end();
			Fortress.spriteBatch.setShader(Shaders.elfHighLight);
			Shaders.elfHighLight.setUniformMatrix("u_projTrans", Fortress.cam.combined);
		} else {

			Shaders.elfDayLight.begin();
			Shaders.elfDayLight.setUniformi("hair", 0);
			Shaders.elfDayLight.setUniformf("eyeColor", eyeColorR, eyeColorG, eyeColorB);
			Shaders.elfDayLight.setUniformf("alpha", 1f);
			Shaders.elfDayLight.setUniformf("hairColor", hairColorR, hairColorG, hairColorB);
			Shaders.elfDayLight.end();
			Fortress.spriteBatch.setShader(Shaders.elfDayLight);
			Shaders.elfDayLight.setUniformMatrix("u_projTrans", Fortress.cam.combined);
		}

		// Draw the body
		Fortress.spriteBatch.draw(
			animations.get(current + (female ? "F" : "M")).getKeyFrame(animationTimer, true),
			state.position.x - animations.get(current + (female ? "F" : "M")).getKeyFrame(0f).getRegionWidth()/2,
			state.position.y
		);
		Fortress.spriteBatch.end();

		// Change draw mode to hair
		Fortress.spriteBatch.begin();
		Shaders.elfHighLight.setUniformi("hair", 1);
		Shaders.elfDayLight.setUniformi("hair", 1);
		Shaders.elfDayLight.setUniformMatrix("u_projTrans", Fortress.cam.combined);
		Shaders.elfHighLight.setUniformMatrix("u_projTrans", Fortress.cam.combined);

		// Draw the hair
		Fortress.spriteBatch.draw(
			hairAnimations.get(currentHair + (female ? "F" : "M"), hairStyle).getKeyFrame(animationTimer, true),
			state.position.x - hairAnimations.get(currentHair + (female ? "F" : "M"), hairStyle).getKeyFrame(0f).getRegionWidth()/2,
			state.position.y
		);

		// Render equipped items
		for (Item equipped : equippedItems.keySet()) {
			Equipable toRender = (Equipable) equipped;

			if (equipped instanceof OneHandedWeapon) {
				SpacialConfiguration config = getOneHandedWeaponSpacialConfigration();
				if (config != null) {
					toRender.render(config.position.add(state.position), config.orientation, config.flipX);
				}
			}
		}

		Fortress.spriteBatch.flush();
		Fortress.spriteBatch.end();
	}


	/** What animation should we use? */
	private void updateAnimation() {

		// If we're moving to the right
		if (state.velocity.x > 0) {
			// If walking, and current animatin is not walking right, then set animations to walking right
			if (isCommandActive(KeyMappings.walk) && !current.equals(WALKING_RIGHT)) {
				current = WALKING_RIGHT;
				currentHair = WALKING_RIGHT_HAIR;
				animationTimer = 0f;
			} else if (!isCommandActive(KeyMappings.walk) && !current.equals(RUNNING_RIGHT)) {
				// Otherwise if running, and current animatin is not running right, then set animations to running right
				current = RUNNING_RIGHT;
				currentHair = RUNNING_RIGHT_HAIR;
				animationTimer = 0f;
			}

		// Same for if we're moving left
		} else if (state.velocity.x < 0) {
			if (isCommandActive(KeyMappings.walk) && !current.equals(WALKING_LEFT)) {
				current = WALKING_LEFT;
				currentHair = WALKING_LEFT_HAIR;
				animationTimer = 0f;
			} else if (!isCommandActive(KeyMappings.walk) && !current.equals(RUNNING_LEFT)) {
				current = RUNNING_LEFT;
				currentHair = RUNNING_LEFT_HAIR;
				animationTimer = 0f;
			}

		// Otherwise we're standing still, if current animation is not standing left or right, then set current to standing left/right depending on which direction we were facing before.
		} else if (state.velocity.x == 0 && !current.equals(STANDING_LEFT) && !current.equals(STANDING_RIGHT)) {
			current = current.equals(WALKING_RIGHT) || current.equals(RUNNING_RIGHT) ? STANDING_RIGHT : STANDING_LEFT;
			currentHair = currentHair.equals(WALKING_RIGHT_HAIR) || currentHair.equals(RUNNING_RIGHT_HAIR) ? STANDING_RIGHT_HAIR : STANDING_LEFT_HAIR;
		}
	}


	@Override
	protected void internalUpdate(float delta) {
		updateAnimation();
	}


	@Override
	protected void respondToCommands() {
		//Horizontal movement
		if (Math.abs(state.velocity.y) < 5f) {
			if (isCommandActive(KeyMappings.moveLeft) && (canStepUp(-2) || !obstructed(-2))) {
				if (isCommandActive(KeyMappings.walk)) {
					state.velocity.x = -30f;
				} else {
					state.velocity.x = -80f;
				}
			} else if (isCommandActive(KeyMappings.moveRight) && (canStepUp(2) || !obstructed(2))) {
				if (isCommandActive(KeyMappings.walk)) {
					state.velocity.x = 30f;
				} else {
					state.velocity.x = 80f;
				}
			} else {
				state.velocity.x = 0f;
				state.acceleration.x = 0f;

				int offset = isCommandActive(KeyMappings.moveRight) ? 2 : isCommandActive(KeyMappings.moveLeft) ? -2 : 0;
				if (obstructed(offset) && !canStepUp(offset) && !(ai.getCurrentTask() instanceof Idle)) {
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
		float x = Fortress.getMouseWorldX();
		float y = Fortress.getMouseWorldY();

		boolean ans = x >= state.position.x - width/2 && x <= state.position.x + width/2 && y >= state.position.y && y <= state.position.y + height;
		return ans;
	}


	@Override
	public Color getToolTipTextColor() {
		return new Color(hairColorR, hairColorG, hairColorB, 1f);
	}


	@Override
	public ContextMenu getContextMenu() {
		final Individual thisElf = this;

		ContextMenuItem controlOrReleaseMenuItem = thisElf.selected ?
			new ContextMenuItem(
				"Deselect",
				new Task() {
					@Override
					public void execute() {
						thisElf.selected = false;
						GameWorld.selectedIndividuals.remove(thisElf);
						clearCommands();
						ai.setToAuto(false);
					}
				},
				Color.WHITE,
				getToolTipTextColor(),
				Color.GRAY,
				null
			) :

			new ContextMenuItem(
				"Select",
				new Task() {
					@Override
					public void execute() {
						thisElf.selected = true;
						GameWorld.selectedIndividuals.add(thisElf);
						ai.setToManual();
					}
				},
				Color.WHITE,
				getToolTipTextColor(),
				Color.GRAY,
				null
			);

		ContextMenuItem showInfoMenuItem = new ContextMenuItem(
			"Show info",
			new Task() {
				@Override
				public void execute() {
					IndividualInfoWindow individualInfoWindow = new IndividualInfoWindow(
						thisElf,
						Fortress.getMouseScreenX(),
						Fortress.getMouseScreenY(),
						300,
						320,
						id.getSimpleName() + " - Info",
						true,
						250, 200
					);
					UserInterface.addLayeredComponentUnique(individualInfoWindow, id.getSimpleName() + " - Info");
				}
			},
			Color.WHITE,
			getToolTipTextColor(),
			Color.GRAY,
			null
		);

		ContextMenuItem inventoryMenuItem = new ContextMenuItem(
			"Inventory",
			new Task() {
				@Override
				public void execute() {
					InventoryWindow inventoryWindow = new InventoryWindow(
						thisElf,
						Fortress.getMouseScreenX(),
						Fortress.getMouseScreenY(),
						(id.getSimpleName() + " - Inventory").length() * 10 + 50,
						200,
						id.getSimpleName() + " - Inventory",
						true,
						150, 150
					);
					UserInterface.addLayeredComponentUnique(inventoryWindow, id.getSimpleName() + " - Inventory");
				}
			},
			Color.WHITE,
			getToolTipTextColor(),
			Color.GRAY,
			null
		);

		ContextMenuItem tradeMenuItem = new ContextMenuItem(
			"Trade with",
			new Task() {
				@Override
				public void execute() {
					for (Individual indi : GameWorld.selectedIndividuals) {
						if (indi != thisElf) {
							UserInterface.addLayeredComponent(
								new MessageWindow(
									"This is the trade window",
									Color.CYAN,
									Fortress.WIDTH/2 - 150,
									Fortress.HEIGHT/2 + 100,
									300,
									200,
									"Trade between " + indi.id.getSimpleName() + " and " + thisElf.id.getSimpleName(),
									true,
									300,
									200
								)
							);
						}
					}
				}
			},
			Color.WHITE,
			getToolTipTextColor(),
			Color.GRAY,
			null
		);

		ContextMenu contextMenuToReturn = new ContextMenu(0, 0);

		if (controllable) {
			contextMenuToReturn.addMenuItem(controlOrReleaseMenuItem);
		}
		contextMenuToReturn.addMenuItem(showInfoMenuItem);
		contextMenuToReturn.addMenuItem(inventoryMenuItem);
		if (!GameWorld.selectedIndividuals.isEmpty() &&
			!(GameWorld.selectedIndividuals.size() == 1 && GameWorld.selectedIndividuals.contains(thisElf))) {
			contextMenuToReturn.addMenuItem(tradeMenuItem);
		}

		return contextMenuToReturn;
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
		}
		return null;
	}
}