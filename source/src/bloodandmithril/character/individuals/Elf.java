package bloodandmithril.character.individuals;

import java.util.HashMap;


import bloodandmithril.Fortress;
import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.implementations.ElfAI;
import bloodandmithril.character.ai.task.Idle;
import bloodandmithril.ui.KeyMappings;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.ContextMenuItem;
import bloodandmithril.ui.components.window.IndividualInfoWindow;
import bloodandmithril.ui.components.window.InventoryWindow;
import bloodandmithril.util.AnimationHelper;
import bloodandmithril.util.Shaders;
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
	private static DualKeyHashMap<String, Integer, Animation> hairAnimation = new DualKeyHashMap<String, Integer, Animation>();
	private static HashMap<String, Animation> animation = new HashMap<String, Animation>();

	/** Current animations */
	private String current, currentHair;


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

		current = "standingRight";
		currentHair = "standingRightHair";
	}


	/**
	 * Loads animations
	 */
	public static void loadAnimations() {
		for (int i = 0; i <=3; i++) {
			hairAnimation.put("standingRightHair", i, AnimationHelper.makeAnimation(GameWorld.individualTexture, 448 + i * 64, 0, 64, 128, 1, 1));
			hairAnimation.put("standingLeftHair", i, AnimationHelper.makeAnimation(GameWorld.individualTexture, 448 + i * 64, 128, 64, 128, 1, 1));
			hairAnimation.put("walkingRightHair", i, AnimationHelper.makeAnimation(GameWorld.individualTexture, 448 + i * 64, 0, 64, 128, 1, 1));
			hairAnimation.put("walkingLeftHair", i, AnimationHelper.makeAnimation(GameWorld.individualTexture, 448 + i * 64, 128, 64, 128, 1, 1));
			hairAnimation.put("runningRightHair", i, AnimationHelper.makeAnimation(GameWorld.individualTexture, 448 + i * 64, 256, 64, 128, 1, 1));
			hairAnimation.put("runningLeftHair", i, AnimationHelper.makeAnimation(GameWorld.individualTexture, 448 + i * 64, 384, 64, 128, 1, 1));
		}

		animation.put("standingLeft", AnimationHelper.makeAnimation(GameWorld.individualTexture, 0, 128, 64, 128, 1, 1));
		animation.put("standingRight", AnimationHelper.makeAnimation(GameWorld.individualTexture, 0, 0, 64, 128, 1, 1));
		animation.put("walkingLeft", AnimationHelper.makeAnimation(GameWorld.individualTexture, 64, 128, 64, 128, 6, 0.17f));
		animation.put("walkingRight", AnimationHelper.makeAnimation(GameWorld.individualTexture, 64, 0, 64, 128, 6, 0.17f));
		animation.put("runningLeft", AnimationHelper.makeAnimation(GameWorld.individualTexture, 0, 384, 64, 128, 7, 0.14f));
		animation.put("runningRight", AnimationHelper.makeAnimation(GameWorld.individualTexture, 0, 256, 64, 128, 7, 0.14f));
	}


	@Override
	protected void internalRender() {
		Fortress.spriteBatch.begin();
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
		try {
		Fortress.spriteBatch.draw(animation.get(current).getKeyFrame(animationTimer, true),
				state.position.x - animation.get(current).getKeyFrame(0f).getRegionWidth()/2,
				state.position.y);
		Fortress.spriteBatch.end();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Fortress.spriteBatch.begin();
		Shaders.elfHighLight.setUniformi("hair", 1);
		Shaders.elfDayLight.setUniformi("hair", 1);
		Shaders.elfDayLight.setUniformMatrix("u_projTrans", Fortress.cam.combined);
		Shaders.elfHighLight.setUniformMatrix("u_projTrans", Fortress.cam.combined);
		Fortress.spriteBatch.draw(hairAnimation.get(currentHair, hairStyle).getKeyFrame(animationTimer, true),
				state.position.x - hairAnimation.get(currentHair, hairStyle).getKeyFrame(0f).getRegionWidth()/2,
				state.position.y);
		Fortress.spriteBatch.flush();

		Fortress.spriteBatch.end();
	}


	/** What animation should we use? */
	private void updateAnimation() {
		if (state.velocity.x > 0) {
			if (isCommandActive(KeyMappings.walk) && !current.equals("walkingRight")) {
				current = "walkingRight";
				currentHair = "walkingRightHair";
				animationTimer = 0f;
			} else if (!isCommandActive(KeyMappings.walk) && !current.equals("runningRight")) {
				current = "runningRight";
				currentHair = "runningRightHair";
				animationTimer = 0f;
			}
		} else if (state.velocity.x < 0) {
			if (isCommandActive(KeyMappings.walk) && !current.equals("walkingLeft")) {
				current = "walkingLeft";
				currentHair = "walkingLeftHair";
				animationTimer = 0f;
			} else if (!isCommandActive(KeyMappings.walk) && !current.equals("runningLeft")) {
				current = "runningLeft";
				currentHair = "runningLeftHair";
				animationTimer = 0f;
			}
		} else if (state.velocity.x == 0 && !current.equals("standingLeft") && !current.equals("standingRight")) {
			current = current.equals("walkingRight") || current.equals("runningRight") ? "standingRight" : "standingLeft";
			currentHair = currentHair.equals("walkingRightHair") || currentHair.equals("runningRightHair") ? "standingRightHair" : "standingLeftHair";
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
	protected String getToolTipText() {
		String gender = female ? "Female" : "Male";

		return 	id.firstName + " " + id.lastName + "\n" +
				"Elf, " + gender + "\n" +
				"Birthday: " + id.birthday.getDateString() + "\n" +
				ai.getCurrentTask().getDescription();
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

		ContextMenuItem runOrWalkContextMenuItem = thisElf.walking ?
			new ContextMenuItem(
				"Run",
				new Task() {
					@Override
					public void execute() {
						thisElf.walking = false;
					}
				},
				Color.WHITE,
				getToolTipTextColor(),
				Color.GRAY,
				null
				) :

			new ContextMenuItem(
				"Walk",
				new Task() {
					@Override
					public void execute() {
						thisElf.walking = true;
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
						(id.getSimpleName() + " - Info").length() * 10 + 50,
						200,
						id.getSimpleName() + " - Info",
						true,
						100, 120
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

		ContextMenu contextMenuToReturn = new ContextMenu(0, 0);

		if (controllable) {
			contextMenuToReturn.addMenuItem(controlOrReleaseMenuItem);
		}
		contextMenuToReturn.addMenuItem(runOrWalkContextMenuItem);
		contextMenuToReturn.addMenuItem(showInfoMenuItem);
		contextMenuToReturn.addMenuItem(inventoryMenuItem);

		return contextMenuToReturn;
	}
}