package bloodandmithril.ui.components.bar;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.ContextMenuItem;
import bloodandmithril.ui.components.window.MainMenuWindow;
import bloodandmithril.ui.components.window.Window;
import bloodandmithril.util.Task;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

/**
 * The bottom bar, or task bar
 *
 * @author Matt
 */
public class BottomBar extends Component {


	private final Button mainMenu = new Button(UserInterface.uiTexture, 25, 16, 53, 0, 50, 32,
		new Task() {
			@Override
			public void execute() {
			}
		}, UIRef.BL
	);


	private final Button windows = new Button(UserInterface.uiTexture, 85, 16, 103, 0, 50, 32,
		new Task() {
			@Override
			public void execute() {
			}
		}, UIRef.BL
	);


	/**
	 * Called upon left click
	 */
	@Override
	public boolean leftClick(List<ContextMenu> copy, final Deque<Component> windowsCopy) {

		if (mainMenu.click()) {
			mainMenuClicked(windowsCopy);
			return true;
		}

		if (windows.click()) {
			windowsClicked(copy, windowsCopy);
			return true;
		}

		if (active && isWithin()) {
			return true;
		} else if (isWithin()) {
			windowsCopy.remove(this);
			windowsCopy.addLast(this);
			active = true;
			return true;
		} else {
			active = false;
			return false;
		}
	}



	/**
	 * Called when the windows button is clicked
	 */
	private void windowsClicked(List<ContextMenu> copy, final Deque<Component> windowsCopy) {
		copy.clear();
		int size = 0;
		ArrayList<ContextMenuItem> items = new ArrayList<>();
		for (final Component component : UserInterface.layeredComponents) {
			if (component instanceof Window) {
				if (((Window) component).minimized) {
					size++;
					items.add(new ContextMenuItem(
						((Window) component).title,
						new Task() {
							@Override
							public void execute() {
								((Window) component).minimized = false;
							}
						},
						Color.WHITE,
						Color.GREEN,
						Color.GRAY,
						null)
					);
				}
			}
		}

		ContextMenu newMenu = new ContextMenu(
			BloodAndMithrilClient.getMouseScreenX(),
			BloodAndMithrilClient.getMouseScreenY() + (size + 3) * 22
		);

		for (ContextMenuItem item : items) {
			newMenu.addMenuItem(item);
		}

		newMenu.addMenuItem(
			new ContextMenuItem(
				"Show all",
				new Task() {
					@Override
					public void execute() {
						for (final Component component : windowsCopy) {
							if (component instanceof Window) {
								if (((Window) component).minimized) {
									((Window) component).minimized = false;
								}
							}
						}
					}
				},
				Color.ORANGE,
				Color.GREEN,
				Color.GRAY,
				null
			)
		);

		newMenu.addMenuItem(
			new ContextMenuItem(
				"Minimize all",
				new Task() {
					@Override
						public void execute() {
							for (final Component component : windowsCopy) {
								if (component instanceof Window) {
									if (((Window) component).minimizable) {
										((Window) component).minimized = true;
									}
								}
							}
						}
					},
				Color.ORANGE,
				Color.GREEN,
				Color.GRAY,
				null
			)
		);

		newMenu.addMenuItem(
			new ContextMenuItem(
				"Close all",
				new Task() {
					@Override
					public void execute() {
						for (final Component component : windowsCopy) {
							if (component instanceof Window) {
								component.closing = true;
							}
						}
					}
				},
				Color.ORANGE,
				Color.GREEN,
				Color.GRAY,
				null
			)
		);

		copy.add(newMenu);
	}



	/**
	 * Called when the {@link #mainMenu} button is clicked
	 */
	private void mainMenuClicked(final Deque<Component> windowsCopy) {
		// Check if the main menu is already open
		Component existing = null;
		for (Component component : windowsCopy) {
			if (component instanceof Window && ((Window) component).title.equals("Main menu")) {
				existing = component;
				break;
			}
		}

		// If not already open, open it, otherwise make the existing the active window
		if (existing == null) {
			windowsCopy.add(
				new MainMenuWindow(
					BloodAndMithrilClient.WIDTH/2 - 100,
					BloodAndMithrilClient.HEIGHT/2 + 55,
					200,
					110,
					"Main menu",
					true,
					200,
					110,
					false
				)
			);
		} else {
			windowsCopy.remove(existing);
			windowsCopy.add(existing);
			existing.active = true;
		}
	}


	/**
	 * True if mouse coords are inside the {@link BottomBar}
	 */
	private boolean isWithin() {
		int x = BloodAndMithrilClient.getMouseScreenX();
		int y = BloodAndMithrilClient.getMouseScreenY();
		return x >= 0 && x <= Gdx.graphics.getWidth() && y >= 0 && y <= 50;
	}


	/**
	 * Renders this {@link BottomBar}
	 */
	@Override
	protected void internalComponentRender() {
		BloodAndMithrilClient.spriteBatch.begin();
		renderRectangle(0, 34, Gdx.graphics.getWidth(), 34, true, Color.BLACK);
		renderBox(-left.getRegionWidth(), 32, Gdx.graphics.getWidth(), 34, true, Color.DARK_GRAY);
		mainMenu.render(!BloodAndMithrilClient.paused && !GameSaver.isSaving(), 1f);
		windows.render(!BloodAndMithrilClient.paused && !GameSaver.isSaving(), 1f);
		BloodAndMithrilClient.spriteBatch.end();
	}


	@Override
	public void leftClickReleased() {
	}


	@Override
	public boolean keyPressed(int keyCode) {
		return false;
	}
}
