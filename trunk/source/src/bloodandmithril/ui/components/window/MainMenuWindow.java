package bloodandmithril.ui.components.window;

import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;
import static bloodandmithril.persistence.GameSaver.isSaving;
import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Deque;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

/**
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class MainMenuWindow extends Window {

	private Button singlePlayer, multiPlayer, options, exit;

	/**
	 * Overloaded constructor - uses default colors
	 */
	public MainMenuWindow(int x, int y, int length, int height, String title, boolean active, int minLength, int minHeight, boolean minimizable, boolean closeable) {
		super(x, y, length, height, title, active, minLength, minHeight, minimizable, false, closeable);
		loadButtons();
	}


	@Override
	protected void internalWindowRender() {
		singlePlayer.render(width/2 + x, y - 26, isActive() && !isSaving(), getAlpha());
		multiPlayer.render(width/2 + x, y - 46, isActive() && !isSaving() && BloodAndMithrilClient.domain == null, getAlpha());
		options.render(width/2 + x, y - 66, isActive() && !isSaving(), getAlpha());
		exit.render(width/2 + x, y - 86, isActive() && !isSaving(), getAlpha());
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		multiPlayer.click();
		exit.click();

		if (options.click()) {
			this.setActive(false);
			windowsCopy.addLast(
				new OptionsWindow()
			);
		}

		if (singlePlayer.click()) {
			copy.add(new ContextMenu(
				getMouseScreenX(),
				getMouseScreenY(),
				true,
				new ContextMenu.MenuItem(
					"New game",
					() -> {
						singlePlayer();
					},
					Color.ORANGE,
					Color.GREEN,
					Color.ORANGE,
					null
				),
				new ContextMenu.MenuItem(
					"Save game",
					() -> {
						if (BloodAndMithrilClient.domain == null) {
							return;
						}

						UserInterface.addLayeredComponent(
							new TextInputWindow(
								BloodAndMithrilClient.WIDTH / 2 - 125,
								BloodAndMithrilClient.HEIGHT/2 + 50,
								250,
								100,
								"Enter name",
								250,
								100,
								args -> {
									String input = (String)args[0];

									if (StringUtils.isBlank(input.replace(" ", ""))) {
										UserInterface.addMessage("Invalid name", "Please enter a valid name.");
										return;
									}

									GameSaver.save(input, false);
								},
								"Save",
								true,
								""
							)
						);
					},
					BloodAndMithrilClient.domain == null ? Colors.UI_DARK_GRAY : Color.ORANGE,
					BloodAndMithrilClient.domain == null ? Colors.UI_DARK_GRAY : Color.GREEN,
					BloodAndMithrilClient.domain == null ? Colors.UI_DARK_GRAY : Color.ORANGE,
					null
				),
				new ContextMenu.MenuItem(
					"Load game",
					() -> {
						UserInterface.addLayeredComponentUnique(
							new LoadGameWindow()
						);
					},
					Color.ORANGE,
					Color.GREEN,
					Color.ORANGE,
					null
				)
			));
		}
	}


	@Override
	public void leftClickReleased() {
	}


	/**
	 * Loads all buttons
	 */
	private void loadButtons() {

		singlePlayer = new Button(
			"Single player",
			Fonts.defaultFont,
			0,
			8,
			120,
			16,
			() -> {},
			Color.ORANGE,
			Color.GREEN,
			Color.GRAY,
			UIRef.M
		);

		multiPlayer = new Button(
			"Multiplayer",
			Fonts.defaultFont,
			0,
			8,
			100,
			16,
			() -> {
				if (BloodAndMithrilClient.domain != null) {
					return;
				}

				UserInterface.addLayeredComponent(
					new TextInputWindow(
						BloodAndMithrilClient.WIDTH / 2 - 125,
						BloodAndMithrilClient.HEIGHT/2 + 50,
						250,
						100,
						"Enter IP",
						250,
						100,
						args -> {
							for (Component component : UserInterface.layeredComponents) {
								if (component instanceof Window && ((Window) component).title.equals("Enter IP") ||
									component instanceof MainMenuWindow) {
									component.setClosing(true);
								}
							}

							if (args[0].toString().equals("local")) {
								singlePlayer();
							} else {
								UserInterface.addMessage("Connecting", "Attemping to connect to " + args[0].toString());
								BloodAndMithrilClient.clientCSIThread.execute(() -> {
									try {
										removeWindows();
										Thread.sleep(1000);
										ClientServerInterface.setupAndConnect(args[0].toString());
										BloodAndMithrilClient.domain = new Domain();
										BloodAndMithrilClient.setup();
									} catch (Exception e) {

										// Deactivate all windows, close the connecting message pop-up.
										for (Component component : UserInterface.layeredComponents) {
											component.setActive(false);
											if (component instanceof Window && ((Window) component).title.equals("Connecting")) {
												component.setClosing(true);
											}
										}

										UserInterface.addLayeredComponent(
											new MainMenuWindow(
												BloodAndMithrilClient.WIDTH/2 - 100,
												BloodAndMithrilClient.HEIGHT/2 + 55,
												200,
												110,
												"Main menu",
												false,
												200,
												110,
												false,
												false
											)
										);

										UserInterface.addLayeredComponent(
											new MessageWindow(
												"Failed to connect",
												Color.RED,
												BloodAndMithrilClient.WIDTH/2 - 150,
												BloodAndMithrilClient.HEIGHT/2 + 50,
												300,
												100,
												"Error",
												true,
												300,
												100,
												() -> {
													for (Component component : UserInterface.layeredComponents) {
														if (component instanceof Window && ((Window) component).title.equals("Error")) {
															component.setClosing(true);
														} else if (component instanceof Window && ((Window) component).title.equals("Enter IP")) {
															component.setActive(true);
														}
													}
												}
											)
										);
									}
								});
							}
						},
						"Connect",
						false,
						""
					)
				);
				UserInterface.buttons.remove("connect");
			},
			Color.ORANGE,
			Color.GREEN,
			Color.GRAY,
			UIRef.M
		);

		options = new Button(
			"Options",
			defaultFont,
			0,
			0,
			70,
			16,
			() -> {},
			Color.ORANGE,
			Color.GREEN,
			Color.GRAY,
			UIRef.BL
		);


		exit = new Button(
			"Exit",
			defaultFont,
			0,
			0,
			40,
			16,
			() -> {
				Gdx.app.exit();
			},
			Color.ORANGE,
			Color.GREEN,
			Color.GRAY,
			UIRef.BL
		);
	}


	/**
	 * Single player mode was selected
	 */
	private void singlePlayer() {
		for (Component component : UserInterface.layeredComponents) {
			if (component instanceof MainMenuWindow) {
				component.setClosing(true);
			}
		}
		UserInterface.addLayeredComponentUnique(new NewGameWindow());
	}


	public static void removeWindows() {
		UserInterface.buttons.remove("connect");
		for (Component component : UserInterface.layeredComponents) {
			if (component instanceof Window && ((Window) component).title.equals("Connecting") ||
				component instanceof Window && ((Window) component).title.equals("Enter IP") ||
				component instanceof MainMenuWindow ||
				component instanceof NewGameWindow) {
				component.setClosing(true);
			}
		}
	}


	@Override
	protected void uponClose() {
	}


	@Override
	public Object getUniqueIdentifier() {
		return getClass();
	}


	@Override
	public boolean isActive() {
		return super.isActive() && UserInterface.contextMenus.isEmpty();
	}
}