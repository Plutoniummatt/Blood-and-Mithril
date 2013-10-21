package bloodandmithril.character.individuals;

import java.util.HashMap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Vector2;

import bloodandmithril.Fortress;
import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.implementations.BoarAI;
import bloodandmithril.character.ai.task.Idle;
import bloodandmithril.ui.KeyMappings;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.ContextMenuItem;
import bloodandmithril.ui.components.window.IndividualInfoWindow;
import bloodandmithril.util.AnimationHelper;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.SpacialConfiguration;
import bloodandmithril.util.Task;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.world.GameWorld;

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
	public Boar(IndividualIdentifier id, IndividualState state) {
		super(id, state, false, 0.05f, 0f, 64, 32, 120, new Box(new Vector2(state.position.x, state.position.y), 120, 120));

		this.ai = new BoarAI(this);

		current = STANDING_RIGHT;
	}
	
	
	/**
	 * Loads animations
	 */
	public static void loadAnimations() {
		animations.put(STANDING_LEFT, AnimationHelper.makeAnimation(GameWorld.individualTexture, 768, 0, 64, 40, 1, 1));
		animations.put(STANDING_RIGHT, AnimationHelper.makeAnimation(GameWorld.individualTexture, 704, 0, 64, 40, 1, 1));
		animations.put(WALKING_LEFT, AnimationHelper.makeAnimation(GameWorld.individualTexture, 704, 80, 64, 40, 5, 0.17f));
		animations.put(WALKING_RIGHT, AnimationHelper.makeAnimation(GameWorld.individualTexture, 704, 40, 64, 40, 5, 0.17f));
	}


	@Override
	protected void internalRender() {
		Fortress.spriteBatch.begin();
		
		// Determine which shader we're using, normal, or highlighted
		if (isMouseOver()) {
			
			Shaders.elfHighLight.begin();
			Shaders.elfHighLight.setUniformi("hair", 0);
			Shaders.elfHighLight.setUniformf("alpha", 1f);
			Shaders.elfHighLight.end();
			Fortress.spriteBatch.setShader(Shaders.elfHighLight);
			Shaders.elfHighLight.setUniformMatrix("u_projTrans", Fortress.cam.combined);
		} else {
			
			Shaders.elfDayLight.begin();
			Shaders.elfDayLight.setUniformi("hair", 0);
			Shaders.elfDayLight.setUniformf("alpha", 1f);
			Shaders.elfDayLight.end();
			Fortress.spriteBatch.setShader(Shaders.elfDayLight);
			Shaders.elfDayLight.setUniformMatrix("u_projTrans", Fortress.cam.combined);
		}
		
		// Draw the body
		Fortress.spriteBatch.draw(
			animations.get(current).getKeyFrame(animationTimer, true),
			state.position.x - animations.get(current).getKeyFrame(0f).getRegionWidth()/2,
			state.position.y
		);
		
		Fortress.spriteBatch.flush();
		Fortress.spriteBatch.end();
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
				}
			} else if (isCommandActive(KeyMappings.moveRight) && (canStepUp(2) || !obstructed(2))) {
				if (isCommandActive(KeyMappings.walk)) {
					state.velocity.x = 30f;
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
	
	
	/** What animation should we use? */
	private void updateAnimation() {
		if (state.velocity.x > 0) {
			if (isCommandActive(KeyMappings.walk) && !current.equals(WALKING_RIGHT)) {
				current = WALKING_RIGHT;
				animationTimer = 0f;
			}
		} else if (state.velocity.x < 0) {
			if (isCommandActive(KeyMappings.walk) && !current.equals(WALKING_LEFT)) {
				current = WALKING_LEFT;
				animationTimer = 0f;
			}
		} else if (state.velocity.x == 0 && !current.equals(STANDING_LEFT) && !current.equals(STANDING_RIGHT)) {
			current = current.equals(WALKING_RIGHT) ? STANDING_RIGHT : STANDING_LEFT;
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
		return Color.GREEN;
	}

	
	@Override
	public ContextMenu getContextMenu() {
		final Individual thisBoar = this;
		
		ContextMenuItem attackItem = new ContextMenuItem(
			"Attack", 
			new Task() {
				@Override
				public void execute() {
					// TODO Attack....
				}
			}, 
			Color.WHITE,
			Color.GREEN,
			Color.GRAY, 
			null
		);
		
		
		ContextMenuItem showInfoMenuItem = new ContextMenuItem(
			"Show info",
			new Task() {
				@Override
				public void execute() {
					IndividualInfoWindow individualInfoWindow = new IndividualInfoWindow(
						thisBoar,
						Fortress.getMouseScreenX(),
						Fortress.getMouseScreenY(),
						300,
						320,
						thisBoar.getClass().getSimpleName() + " - Info",
						true,
						250, 200
					);
					UserInterface.addLayeredComponentUnique(individualInfoWindow, id.id + " - Info");
				}
			},
			Color.WHITE,
			getToolTipTextColor(),
			Color.GRAY,
			null
		);
		
		ContextMenu contextMenuToReturn = new ContextMenu(0, 0);
		
		contextMenuToReturn.addMenuItem(attackItem);
		contextMenuToReturn.addMenuItem(showInfoMenuItem);
		
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
		return null;
	}
}