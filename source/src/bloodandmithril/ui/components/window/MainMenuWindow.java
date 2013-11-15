package bloodandmithril.ui.components.window;

import static bloodandmithril.persistence.GameSaver.isSaving;
import static bloodandmithril.persistence.GameSaver.save;
import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Deque;
import java.util.List;

import bloodandmithril.Fortress;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.util.Task;

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
	 * @see bloodandmithril.ui.components.window.Window#internalWindowRender()
	 */
	@Override
	protected void internalWindowRender() {
		saveGame.render(length/2 + x, y - 26, active && !isSaving(), alpha);
		options.render(length/2 + x, y - 46, active && !isSaving(), alpha);
		saveAndExit.render(length/2 + x, y - 66, active && !isSaving(), alpha);
	}


	/**
	 * @see bloodandmithril.ui.components.window.Window#internalLeftClick(java.util.List)
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


	@Override
	public void leftClickReleased() {
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
					save(false);
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
					save(true);
			}
			},
			Color.ORANGE,
			Color.GREEN,
			Color.GRAY,
			UIRef.BL
		);
	}


	@Override
	protected void uponClose() {
	}
}