package bloodandmithril.ui.components.window;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Deque;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.google.inject.Inject;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.GameSetupService;
import bloodandmithril.core.Threading;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
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

/**
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class MainMenuWindow extends Window {

	private Button singlePlayer, multiPlayer, options, exit;

	@Inject	private Threading threading;
	@Inject	private GameSaver gameSaver;
	@Inject private UserInterface userInterface;
	@Inject private GameSetupService gameSetupService;
	@Inject private GameClientStateTracker gameClientStateTracker;

	/**
	 * Constructor
	 */
	public MainMenuWindow(final boolean closeable) {
		super(200, 130, "Main Menu", true, false, false, closeable);
		loadButtons();
		Wiring.injector().injectMembers(this);
	}


	@Override
	protected void internalWindowRender(final Graphics graphics) {
		singlePlayer.render(width/2 + x, y - 26, isActive() && !gameSaver.isSaving(), getAlpha(), graphics);
		multiPlayer.render(width/2 + x, y - 46, isActive() && !gameSaver.isSaving() && !gameClientStateTracker.isInGame(), getAlpha(), graphics);
		options.render(width/2 + x, y - 66, isActive() && !gameSaver.isSaving(), getAlpha(), graphics);
		exit.render(width/2 + x, y - 86, isActive() && !gameSaver.isSaving(), getAlpha(), graphics);
	}


	@Override
	protected void internalLeftClick(final List<ContextMenu> copy, final Deque<Component> windowsCopy) {
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
						if (!gameClientStateTracker.isInGame()) {
							return;
						}

						UserInterface.addLayeredComponent(
							new TextInputWindow(
								250,
								100,
								"Enter name",
								250,
								100,
								args -> {
									final String input = (String)args[0];

									if (StringUtils.isBlank(input.replace(" ", ""))) {
										UserInterface.addGlobalMessage("Invalid name", "Please enter a valid name.");
										return;
									}

									gameSaver.save(input, false);
								},
								"Save",
								true,
								""
							)
						);
					},
					gameClientStateTracker.isInGame() ? Color.ORANGE : Colors.UI_DARK_GRAY,
					gameClientStateTracker.isInGame() ? Color.GREEN  : Colors.UI_DARK_GRAY,
					gameClientStateTracker.isInGame() ? Color.ORANGE : Colors.UI_DARK_GRAY,
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
				if (gameClientStateTracker.isInGame()) {
					return;
				}

				UserInterface.addLayeredComponent(
					new TextInputWindow(
						250,
						100,
						"Enter IP",
						250,
						100,
						args -> {
							for (final Component component : UserInterface.getLayeredComponents()) {
								if (component instanceof Window && ((Window) component).title.equals("Enter IP") ||
									component instanceof MainMenuWindow) {
									component.setClosing(true);
								}
							}

							if (args[0].toString().equals("local")) {
								singlePlayer();
							} else {
								UserInterface.addGlobalMessage("Connecting", "Attemping to connect to " + args[0].toString());
								threading.clientProcessingThreadPool.execute(() -> {
									try {
										removeWindows();
										Thread.sleep(1000);
										ClientServerInterface.setupAndConnect(args[0].toString());

										while (Domain.getWorlds().isEmpty()) {
											try {
												Thread.sleep(100);
											} catch (final Exception e) {
												throw new RuntimeException(e);
											}
										}

										gameClientStateTracker.setSelectedActiveWorldId(Domain.getWorlds().keySet().iterator().next().intValue());
										gameClientStateTracker.setInGame(true);
										gameSetupService.setup();
									} catch (final Exception e) {

										// Deactivate all windows, close the connecting message pop-up.
										for (final Component component : UserInterface.getLayeredComponents()) {
											component.setActive(false);
											if (component instanceof Window && ((Window) component).title.equals("Connecting")) {
												component.setClosing(true);
											}
										}

										UserInterface.addLayeredComponent(
											new MainMenuWindow(false)
										);

										UserInterface.addLayeredComponent(
											new MessageWindow(
												"Failed to connect",
												Color.RED,
												300,
												100,
												"Error",
												true,
												300,
												100,
												() -> {
													for (final Component component : UserInterface.getLayeredComponents()) {
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
				userInterface.removeButton("connect");
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
		for (final Component component : UserInterface.getLayeredComponents()) {
			if (component instanceof MainMenuWindow) {
				component.setClosing(true);
			}
		}
		UserInterface.addLayeredComponentUnique(new NewGameWindow());
	}


	public static void removeWindows() {
		Wiring.injector().getInstance(UserInterface.class).removeButton("connect");
		for (final Component component : UserInterface.getLayeredComponents()) {
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