package bloodandmithril.ui.components.bar;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.ContextMenuItem;
import bloodandmithril.ui.components.window.ChatWindow;
import bloodandmithril.ui.components.window.FactionsWindow;
import bloodandmithril.ui.components.window.MainMenuWindow;
import bloodandmithril.ui.components.window.MessageWindow;
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

	private final Button chat = new Button(UserInterface.uiTexture, 145, 16, 153, 0, 50, 32,
		new Task() {
			@Override
			public void execute() {
			}
		},
		UIRef.BL
	);

	private final Button factions = new Button(UserInterface.uiTexture, 205, 16, 203, 0, 50, 32,
		new Task() {
			@Override
			public void execute() {
			}
		},
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
			chatClicked();
			return true;
		}

		if (factions.click()) {
			factionsClicked();
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


	/** Called when the factions button is clicked */
	private void factionsClicked() {
		for (Component component : UserInterface.layeredComponents) {
			if (component instanceof FactionsWindow) {
				((FactionsWindow) component).x = BloodAndMithrilClient.WIDTH/2 - ((FactionsWindow) component).width/2;
				((FactionsWindow) component).y = BloodAndMithrilClient.HEIGHT/2 + ((FactionsWindow) component).height/2;
				((FactionsWindow) component).minimized = false;
				((FactionsWindow) component).setActive(true);
				return;
			}
		}

		UserInterface.addLayeredComponent(
			new FactionsWindow(
				BloodAndMithrilClient.WIDTH/2 - 125,
				BloodAndMithrilClient.HEIGHT/2 + 150,
				250,
				300,
				true,
				250,
				300
			)
		);
	}


	/** Called when the chat button is clicked */
	private void chatClicked() {
		if (ClientServerInterface.isServer() && ClientServerInterface.isClient()) {
			UserInterface.addLayeredComponent(
			new MessageWindow(
					"Chat is unavailable during single player",
					Color.ORANGE,
					BloodAndMithrilClient.WIDTH/2 - 175,
					BloodAndMithrilClient.HEIGHT/2 + 100,
					350,
					200,
					"Chat",
					true,
					100,
					100
				)
			);
		} else {
			for (Component component : UserInterface.layeredComponents) {
				if (component instanceof ChatWindow) {
					((ChatWindow) component).x = BloodAndMithrilClient.WIDTH/2 - ((ChatWindow) component).width/2;
					((ChatWindow) component).y = BloodAndMithrilClient.HEIGHT/2 + ((ChatWindow) component).height/2;
					((ChatWindow) component).minimized = false;
					((ChatWindow) component).setActive(true);
					return;
				}
			}
	
			UserInterface.addLayeredComponent(
				new ChatWindow(
					BloodAndMithrilClient.WIDTH/2 - 250,
					BloodAndMithrilClient.HEIGHT/2 + 150,
					500,
					300,
					true,
					300,
					250
				)
			);
		}
	}



	/** Called when the windows button is clicked */
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
								component.setClosing(true);
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
			existing.setActive(true);
		}
	}


	/** True if mouse coords are inside the {@link BottomBar} */
	private boolean isWithin() {
		int x = BloodAndMithrilClient.getMouseScreenX();
		int y = BloodAndMithrilClient.getMouseScreenY();
		return x >= 0 && x <= Gdx.graphics.getWidth() && y >= 0 && y <= 50;
	}


	/** Renders this {@link BottomBar} */
	@Override
	protected void internalComponentRender() {
		BloodAndMithrilClient.spriteBatch.begin();
		renderRectangle(0, 34, Gdx.graphics.getWidth(), 34, true, Color.BLACK);
		renderBox(-left.getRegionWidth(), 32, Gdx.graphics.getWidth(), 34, true, Color.DARK_GRAY);
		mainMenu.render(!BloodAndMithrilClient.paused && !GameSaver.isSaving(), 1f);
		windows.render(!BloodAndMithrilClient.paused && !GameSaver.isSaving(), 1f);
		chat.render(!BloodAndMithrilClient.paused && !GameSaver.isSaving(), 1f);
		factions.render(!BloodAndMithrilClient.paused && !GameSaver.isSaving(), 1f);
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
