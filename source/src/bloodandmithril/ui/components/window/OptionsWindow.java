package bloodandmithril.ui.components.window;

import java.util.Deque;
import java.util.List;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.persistence.ConfigPersistenceService;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.JITTask;
import bloodandmithril.util.Task;

/**
 * The options window
 *
 * @author Matt
 */
public class OptionsWindow extends Window {
	
	private Button changeRes;

	/**
	 * Constructor
	 */
	public OptionsWindow() {
		super(BloodAndMithrilClient.WIDTH/2 - 125, BloodAndMithrilClient.HEIGHT/2 + 125, 250, 350, "Options", true, 250, 350, false);
		
		this.changeRes = new Button(
			"Change resolution", 
			Fonts.defaultFont, 
			0, 
			0, 
			170, 
			16, 
			new Task() {
				@Override
				public void execute() {
					UserInterface.addLayeredComponent(
						new TextInputWindow(
							BloodAndMithrilClient.WIDTH / 2 - 125,
							BloodAndMithrilClient.HEIGHT/2 + 50,
							250,
							100,
							"Enter width",
							250,
							100,
							new JITTask() {
								@Override
								public void execute(Object... args) {
									try {
										int resX = Integer.parseInt(args[0].toString());
										ConfigPersistenceService.getConfig().setResX(resX);
									} catch (Exception e) {
										UserInterface.addLayeredComponent(
											new MessageWindow(
												"Invalid resolution, enter an integer",
												Color.RED,
												BloodAndMithrilClient.WIDTH/2 - 150,
												BloodAndMithrilClient.HEIGHT/2 + 50,
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
									
									UserInterface.addLayeredComponent(
										new TextInputWindow(
											BloodAndMithrilClient.WIDTH / 2 - 125,
											BloodAndMithrilClient.HEIGHT/2 + 50,
											250,
											100,
											"Enter height",
											250,
											100,
											new JITTask() {
												@Override
												public void execute(Object... args) {
													try {
														int resY = Integer.parseInt(args[0].toString());
														ConfigPersistenceService.getConfig().setResY(resY);
													} catch (Exception e) {
														UserInterface.addLayeredComponent(
															new MessageWindow(
																"Invalid resolution, enter an integer",
																Color.RED,
																BloodAndMithrilClient.WIDTH/2 - 150,
																BloodAndMithrilClient.HEIGHT/2 + 50,
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
													
													UserInterface.addLayeredComponent(
														new MessageWindow(
															"Please restart the game for the changes to take effect",
															Color.GREEN,
															BloodAndMithrilClient.WIDTH/2 - 250,
															BloodAndMithrilClient.HEIGHT/2 + 75,
															500,
															150,
															"Information",
															true,
															300,
															100
														)
													);
													
													ConfigPersistenceService.saveConfig();
													UserInterface.removeLayeredComponent("Options");
												}
											}, 
											"Confirm", 
											true, 
											Integer.toString(BloodAndMithrilClient.HEIGHT)
										)
									);
								}
							}, 
							"Confirm", 
							true, 
							Integer.toString(BloodAndMithrilClient.WIDTH)
						)
					);
				}
			}, 
			Color.ORANGE, 
			Color.WHITE, 
			Color.GREEN, 
			UIRef.BL
		);
	}
	

	@Override
	protected void internalWindowRender() {
		changeRes.render(x + width/2, y - 30, isActive(), getAlpha());
	}

	
	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		changeRes.click();
	}

	
	@Override
	protected void uponClose() {
	}

	
	@Override
	public boolean keyPressed(int keyCode) {
		return false;
	}

	
	@Override
	public void leftClickReleased() {
	}
}