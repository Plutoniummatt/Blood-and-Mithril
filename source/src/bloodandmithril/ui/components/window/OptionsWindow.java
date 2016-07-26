package bloodandmithril.ui.components.window;

import static bloodandmithril.graphics.Graphics.getGdxHeight;
import static bloodandmithril.graphics.Graphics.getGdxWidth;

import java.util.Deque;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.google.inject.Inject;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.persistence.ConfigPersistenceService;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.util.Fonts;

/**
 * The options window
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class OptionsWindow extends Window {

	@Inject private UserInterface userInterface;

	private Button changeRes, fullScreen, controls;

	/**
	 * Constructor
	 */
	public OptionsWindow() {
		super(250, 140, "Options", true, false, false, true);

		this.changeRes = new Button(
			"Change resolution",
			Fonts.defaultFont,
			0,
			0,
			170,
			16,
			() -> {
				userInterface.addLayeredComponentUnique(
					new TextInputWindow(
						"enterWidthResolution",
						250,
						100,
						"Enter width",
						250,
						100,
						enteredWidth -> {
							try {
								final int resX = Integer.parseInt(enteredWidth[0].toString());
								ConfigPersistenceService.getConfig().setResX(resX);
							} catch (final Exception e) {
								userInterface.addLayeredComponent(
									new MessageWindow(
										"Invalid resolution, enter an integer",
										Color.RED,
										300,
										100,
										"Error",
										true,
										300,
										100
									)
								);
								return;
							}

							userInterface.addLayeredComponentUnique(
								new TextInputWindow(
									"enterResolutionHeight",
									250,
									100,
									"Enter height",
									250,
									100,
									enteredHeight -> {
										try {
											final int resY = Integer.parseInt(enteredHeight[0].toString());
											ConfigPersistenceService.getConfig().setResY(resY);
										} catch (final Exception e) {
											userInterface.addLayeredComponent(
												new MessageWindow(
													"Invalid resolution, enter an integer",
													Color.RED,
													300,
													100,
													"Error",
													true,
													300,
													100
												)
											);
											return;
										}

										userInterface.addLayeredComponent(
											new MessageWindow(
												"Please restart the game for the changes to take effect",
												Color.GREEN,
												500,
												150,
												"Information",
												true,
												300,
												100
											)
										);

										ConfigPersistenceService.saveConfig();
										userInterface.removeLayeredComponent("Options");
									},
									"Confirm",
									true,
									Integer.toString(getGdxHeight())
								)
							);
						},
						"Confirm",
						true,
						Integer.toString(getGdxWidth())
					)
				);
			},
			Color.ORANGE,
			Color.GREEN,
			Color.WHITE,
			UIRef.BL
		);

		this.fullScreen = new Button(
			ConfigPersistenceService.getConfig().isFullScreen() ? "Change to windowed" : "Change to full screen",
			Fonts.defaultFont,
			0,
			0,
			200,
			16,
			() -> {
				ConfigPersistenceService.getConfig().setFullScreen(!ConfigPersistenceService.getConfig().isFullScreen());
				ConfigPersistenceService.saveConfig();

				userInterface.addLayeredComponent(
					new MessageWindow(
						"Please restart the game for the changes to take effect",
						Color.GREEN,
						500,
						150,
						"Information",
						true,
						300,
						100
					)
				);

				fullScreen.text = ConfigPersistenceService.getConfig().isFullScreen() ? () -> {return "Change to windowed";} : () -> {return "Change to full screen";};
			},
			Color.ORANGE,
			Color.GREEN,
			Color.WHITE,
			UIRef.BL
		);

		this.controls = new Button(
			"Controls",
			Fonts.defaultFont,
			0,
			0,
			80,
			16,
			() -> {
				userInterface.addLayeredComponentUnique(
					new KeyMappingsWindow()
				);

				OptionsWindow.this.setClosing(true);
			},
			Color.ORANGE,
			Color.GREEN,
			Color.WHITE,
			UIRef.BL
		);
	}


	@Override
	protected void internalWindowRender(final Graphics graphics) {
		changeRes.render(x + width/2, y - 30, isActive() && Gdx.app.getGraphics().isFullscreen(), getAlpha(), graphics);
		fullScreen.render(x + width/2, y - 50, isActive(), getAlpha(), graphics);
		controls.render(x + width/2, y - 70, isActive(), getAlpha(), graphics);
	}


	@Override
	protected void internalLeftClick(final List<ContextMenu> copy, final Deque<Component> windowsCopy) {
		if (Gdx.app.getGraphics().isFullscreen()) {
			changeRes.click();
		}
		fullScreen.click();
		controls.click();
	}


	@Override
	protected void uponClose() {
	}


	@Override
	public void leftClickReleased() {
	}


	@Override
	public Object getUniqueIdentifier() {
		return getClass();
	}
}