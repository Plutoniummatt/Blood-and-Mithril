package bloodandmithril.ui.components.window;

import static bloodandmithril.persistence.GameSaver.isSaving;
import static bloodandmithril.util.Fonts.defaultFont;

import java.io.IOException;
import java.util.Deque;
import java.util.List;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.character.faction.Faction;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.persistence.GameLoader;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.JITTask;
import bloodandmithril.util.Task;
import bloodandmithril.world.GameWorld;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

/**
 * @author Matt
 */
public class MainMenuWindow extends Window {

	private Button connect, options, exit;

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
		connect.render(width/2 + x, y - 26, active && !isSaving() && BloodAndMithrilClient.gameWorld == null, alpha);
		options.render(width/2 + x, y - 46, active && !isSaving(), alpha);
		exit.render(width/2 + x, y - 66, active && !isSaving(), alpha);
	}


	/**
	 * @see bloodandmithril.ui.components.window.Window#internalLeftClick(java.util.List)
	 */
	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		connect.click();
		exit.click();

		if (options.click()) {
			this.active = false;
			windowsCopy.addLast(
				new MessageWindow(
					"This is the options window",
					Color.ORANGE,
					BloodAndMithrilClient.WIDTH/2 - 150,
					BloodAndMithrilClient.HEIGHT/2 + 100,
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
		connect = new Button(
			"Connect",
			Fonts.defaultFont,
			0,
			8,
			80,
			16,
			new Task() {
				@Override
				public void execute() {
					if (BloodAndMithrilClient.gameWorld != null) {
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
							new JITTask() {
								@Override
								public void execute(Object... args) {
									try {
										if (args[0].toString().equals("local")) {
											GameWorld.factions.put(0, new Faction("NPC", 0));
											GameWorld.factions.put(1, new Faction("Elves", 1));

											ClientServerInterface.setServer(true);
											BloodAndMithrilClient.gameWorld = new GameWorld(true);
											GameLoader.load();
										} else {
											ClientServerInterface.setupAndConnect(args[0].toString());
											BloodAndMithrilClient.gameWorld = new GameWorld(true);
										}

										UserInterface.buttons.remove("connect");
										UserInterface.setup();
										for (Component component : UserInterface.layeredComponents) {
											if (component instanceof Window && ((Window) component).title.equals("Enter IP")) {
												component.closing = true;
											} else if (component instanceof MainMenuWindow) {
												component.closing = true;
											}
										}
									} catch (IOException e) {
										for (Component component : UserInterface.layeredComponents) {
											component.active = false;
										}
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
												new Task() {
													@Override
													public void execute() {
														for (Component component : UserInterface.layeredComponents) {
															if (component instanceof Window && ((Window) component).title.equals("Error")) {
																component.closing = true;
															} else if (component instanceof Window && ((Window) component).title.equals("Enter IP")) {
																component.active = true;
															}
														}
													}
												}
											)
										);
									}
								}
							},
							"Connect",
							false
						)
					);
					UserInterface.buttons.remove("connect");
				}
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


		exit = new Button(
			"Exit",
			defaultFont,
			0,
			0,
			40,
			16,
			new Task() {
				@Override
				public void execute() {
					Gdx.app.exit();
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


	@Override
	public boolean keyPressed(int keyCode) {
		return false;
	}
}