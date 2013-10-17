package spritestar.ui.components.window;

import static spritestar.util.Fonts.defaultFont;

import java.util.Deque;
import java.util.List;

import spritestar.Fortress;
import spritestar.persistence.GameSaver;
import spritestar.ui.UserInterface.UIRef;
import spritestar.ui.components.Button;
import spritestar.ui.components.Component;
import spritestar.ui.components.ContextMenu;
import spritestar.util.Task;

import com.badlogic.gdx.graphics.Color;

/**
 * @author Matt
 */
public class MainMenuWindow extends Window {

	private Button saveGame, options, saveAndExit;

	/**
	 * Constructor
	 */
	@Deprecated
	public MainMenuWindow(int x, int y, int length, int height, Color borderColor, Color backGroundColor, String title, boolean active, int minLength, int minHeight, boolean minimizable) {
		super(x, y, length, height, borderColor, backGroundColor, title, active, minLength, minHeight, minimizable);
		loadButtons();
	}


	/**
	 * Overloaded constructor - uses default colors
	 */
	public MainMenuWindow(int x, int y, int length, int height, String title, boolean active, int minLength, int minHeight, boolean minimizable) {
		super(x, y, length, height, title, active, minLength, minHeight, minimizable);
		loadButtons();
	}


	/**
	 * @see spritestar.ui.components.window.Window#internalWindowRender()
	 */
	@Override
	protected void internalWindowRender() {
		saveGame.render(length/2 + x, y - 26, active, alpha);
		options.render(length/2 + x, y - 46, active, alpha);
		saveAndExit.render(length/2 + x, y - 66, active, alpha);
	}


	/**
	 * @see spritestar.ui.components.window.Window#internalLeftClick(java.util.List)
	 */
	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		saveGame.click();
		saveAndExit.click();

		if (options.click()) {
			this.active = false;
			windowsCopy.addLast(
				new MessageWindow(
					"This is the options window",
					Color.ORANGE,
					Fortress.WIDTH/2 - 150,
					Fortress.HEIGHT/2 + 100,
					300,
					200,
					"Options",
					true,
					300,
					200
				)
			);
		}
	}


	/**
	 * Loads all buttons
	 */
	private void loadButtons() {
		saveGame = new Button(
			"Save",
			defaultFont,
			0,
			0,
			40,
			16,
			new Task() {
				@Override
				public void execute() {
					GameSaver.save(false);
				}
			},
			Color.ORANGE,
			Color.GREEN,
			Color.GRAY,
			UIRef.BL
		);

		options = new Button(
			"Options",
			defaultFont,
			0,
			0,
			70,
			16,
			new Task() {
				@Override
				public void execute() {
				}
			},
			Color.ORANGE,
			Color.GREEN,
			Color.GRAY,
			UIRef.BL
		);


		saveAndExit = new Button(
			"Save and Exit",
			defaultFont,
			0,
			0,
			130,
			16,
			new Task() {
				@Override
				public void execute() {
					GameSaver.save(true);
			}
			},
			Color.ORANGE,
			Color.GREEN,
			Color.GRAY,
			UIRef.BL
		);
	}
}