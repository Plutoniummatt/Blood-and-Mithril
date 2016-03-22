package bloodandmithril.ui.components.bar;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.graphics.Graphics.getGdxHeight;
import static bloodandmithril.graphics.Graphics.getGdxWidth;
import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.ChatWindow;
import bloodandmithril.ui.components.window.FactionsWindow;
import bloodandmithril.ui.components.window.MainMenuWindow;
import bloodandmithril.ui.components.window.MissionsWindow;
import bloodandmithril.ui.components.window.Window;

/**
 * The bottom bar, or task bar
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class BottomBar extends Component {


	private final Button mainMenu = new Button(UserInterface.uiTexture, 25, 16, 53, 0, 50, 32,
		() -> {},
		UIRef.BL
	);

	private final Button windows = new Button(UserInterface.uiTexture, 85, 16, 103, 0, 50, 32,
		() -> {},
		UIRef.BL
	);

	private final Button chat = new Button(UserInterface.uiTexture, 145, 16, 153, 0, 50, 32,
		() -> {},
		UIRef.BL
	);

	private final Button factions = new Button(UserInterface.uiTexture, 205, 16, 203, 0, 50, 32,
		() -> {},
		UIRef.BL
	);

	private final Button missions = new Button(UserInterface.uiTexture, 265, 16, 53, 96, 50, 32,
		() -> {},
		UIRef.BL
	);


	/** Called upon left click */
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

		if (chat.click()) {
			chatClicked(copy);
			return true;
		}

		if (factions.click()) {
			factionsClicked();
			return true;
		}

		if (missions.click()) {
			missionsClicked();
			return true;
		}

		if (isActive() && isWithin()) {
			return true;
		} else if (isWithin()) {
			windowsCopy.remove(this);
			windowsCopy.addLast(this);
			setActive(true);
			return true;
		} else {
			setActive(false);
			return false;
		}
	}


	private void missionsClicked() {
		UserInterface.addLayeredComponentUnique(
			new MissionsWindow()
		);
	}


	/** Called when the factions button is clicked */
	private void factionsClicked() {
		for (Component component : newArrayList(UserInterface.getLayeredComponents())) {
			if (component instanceof FactionsWindow) {
				((FactionsWindow) component).x = getGdxWidth()/2 - ((FactionsWindow) component).width/2;
				((FactionsWindow) component).y = getGdxHeight()/2 + ((FactionsWindow) component).height/2;
				((FactionsWindow) component).minimized = false;
				((FactionsWindow) component).setActive(true);
				return;
			}
		}

		UserInterface.addLayeredComponentUnique(
			new FactionsWindow(
				250,
				300,
				true,
				250,
				300
			)
		);
	}


	/** Called when the chat button is clicked */
	private void chatClicked(List<ContextMenu> copy) {

		ContextMenu contextMenu = new ContextMenu(
			getMouseScreenX(),
			getMouseScreenY() + 44,
			true,
			new MenuItem(
				"Show logs",
				() -> {},
				Color.ORANGE,
				Color.GREEN,
				Color.GRAY,
				null
			)
		);

		if (ClientServerInterface.isClient() && !ClientServerInterface.isServer())
		contextMenu.addMenuItem(
			new MenuItem(
				"Chat",
				() -> {
					for (Component component : newArrayList(UserInterface.getLayeredComponents())) {
						if (component instanceof ChatWindow) {
							((ChatWindow) component).x = getGdxWidth()/2 - ((ChatWindow) component).width/2;
							((ChatWindow) component).y = getGdxHeight()/2 + ((ChatWindow) component).height/2;
							((ChatWindow) component).minimized = false;
							((ChatWindow) component).setActive(true);
							break;
						}

						UserInterface.addLayeredComponent(
							new ChatWindow(
								500,
								300,
								true,
								300,
								250
							)
						);
					}
				},
				Color.ORANGE,
				Color.GREEN,
				Color.GRAY,
				null
			)
		);

		copy.add(
			contextMenu
		);
	}



	/** Called when the windows button is clicked */
	private void windowsClicked(List<ContextMenu> copy, final Deque<Component> windowsCopy) {
		copy.clear();
		int size = 0;
		ArrayList<MenuItem> items = new ArrayList<>();
		for (final Component component : UserInterface.getLayeredComponents()) {
			if (component instanceof Window) {
				if (((Window) component).minimized) {
					size++;
					items.add(new MenuItem(
						((Window) component).title,
						() -> {
							((Window) component).minimized = false;
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
			getMouseScreenX(),
			getMouseScreenY() + (size + 3) * 22,
			true
		);

		for (MenuItem item : items) {
			newMenu.addMenuItem(item);
		}

		newMenu.addMenuItem(
			new MenuItem(
				"Show all",
				() -> {
					for (final Component component : windowsCopy) {
						if (component instanceof Window) {
							if (((Window) component).minimized) {
								((Window) component).minimized = false;
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
			new MenuItem(
				"Minimize all",
				() -> {
					for (final Component component : windowsCopy) {
						if (component instanceof Window) {
							if (((Window) component).minimizable) {
								((Window) component).minimized = true;
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
			new MenuItem(
				"Close all",
				() -> {
					for (final Component component : windowsCopy) {
						if (component instanceof Window) {
							component.setClosing(true);
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


	/** Called when the {@link #mainMenu} button is clicked */
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
				new MainMenuWindow(true)
			);
		} else {
			windowsCopy.remove(existing);
			windowsCopy.add(existing);
			existing.setActive(true);
		}
	}


	/** True if mouse coords are inside the {@link BottomBar} */
	private boolean isWithin() {
		int x = getMouseScreenX();
		int y = getMouseScreenY();
		return x >= 0 && x <= getGdxWidth() && y >= 0 && y <= 50;
	}


	/** Renders this {@link BottomBar} */
	@Override
	protected void internalComponentRender(Graphics graphics) {
		graphics.getSpriteBatch().begin();
		renderRectangle(0, 34, getGdxWidth(), 34, true, Color.BLACK);
		renderBox(-left.getRegionWidth(), 32, getGdxWidth(), 34, true, Color.DARK_GRAY, graphics);
		mainMenu.render(!BloodAndMithrilClient.paused.get() && !GameSaver.isSaving(), 1f, graphics);
		windows.render(!BloodAndMithrilClient.paused.get() && !GameSaver.isSaving(), 1f, graphics);
		chat.render(!BloodAndMithrilClient.paused.get() && !GameSaver.isSaving(), 1f, graphics);
		factions.render(!BloodAndMithrilClient.paused.get() && !GameSaver.isSaving(), 1f, graphics);
		missions.render(!BloodAndMithrilClient.paused.get() && !GameSaver.isSaving(), 1f, graphics);
		graphics.getSpriteBatch().end();
	}


	@Override
	public void leftClickReleased() {
	}


	@Override
	public boolean keyPressed(int keyCode) {
		return false;
	}
}
