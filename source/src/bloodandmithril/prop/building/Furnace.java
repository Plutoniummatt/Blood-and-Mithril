package bloodandmithril.prop.building;


import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.task.TradeWith;
import bloodandmithril.character.ai.task.Trading;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.ContextMenuItem;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.ui.components.window.TextInputWindow;
import bloodandmithril.util.JITTask;
import bloodandmithril.util.Task;
import bloodandmithril.world.GameWorld;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A Furnace
 *
 * @author Matt
 */
public class Furnace extends ConstructionWithContainer {

	/** {@link TextureRegion} of the {@link Furnace} */
	public static TextureRegion furnace;

	public static final float minTemp = 1400f, maxTemp = 2500f;

	/** The duration which this furnace will combust, in seconds */
	private float combustionDuration;

	/** Temperature of the {@link Furnace} */
	private float temperature;

	/** True if burning */
	private boolean burning;

	/**
	 * Constructor
	 */
	public Furnace(float x, float y) {
		super(x, y, 49, 76, false, 500f);
	}


	@Override
	public ContextMenu getContextMenu() {

		ContextMenu menu = new ContextMenu(BloodAndMithrilClient.getMouseScreenX(), BloodAndMithrilClient.getMouseScreenY(),
			new ContextMenuItem(
				"Show info",
				new Task() {
					@Override
					public void execute() {
						UserInterface.addLayeredComponent(
							new MessageWindow(
								"A furnace, able to achieve temperatures hot enough to melt most metals",
								Color.ORANGE,
								BloodAndMithrilClient.WIDTH/2 - 175,
								BloodAndMithrilClient.HEIGHT/2 + 100,
								350,
								200,
								"Furnace",
								true,
								350,
								200
							)
						);
					}
				},
				Color.WHITE,
				Color.GREEN,
				Color.GRAY,
				null
			)
		);
		
		ContextMenuItem changeTempItem = new ContextMenuItem(
			"Change temperature",
			new Task() {
				@Override
				public void execute() {
					UserInterface.addLayeredComponent(
						new TextInputWindow(
							BloodAndMithrilClient.WIDTH / 2 - 125,
							BloodAndMithrilClient.HEIGHT/2 + 50,
							250,
							100,
							"Change temperature",
							250,
							100,
							new JITTask() {
								@Override
								public void execute(Object... args) {
									try {
										float newTemp = Float.parseFloat(args[0].toString());

										if (burning && newTemp > maxTemp) {
											UserInterface.addLayeredComponent(
												new MessageWindow(
													"Temperature too high",
													Color.RED,
													BloodAndMithrilClient.WIDTH/2 - 150,
													BloodAndMithrilClient.HEIGHT/2 + 50,
													300,
													100,
													"Too hot",
													true,
													300,
													100
												)
											);
											return;
										}

										if (burning && newTemp < minTemp) {
											UserInterface.addLayeredComponent(
												new MessageWindow(
													"Temperature too low",
													Color.RED,
													BloodAndMithrilClient.WIDTH/2 - 150,
													BloodAndMithrilClient.HEIGHT/2 + 50,
													300,
													100,
													"Too cold",
													true,
													300,
													100
													)
												);
											return;
										}

										temperature = newTemp;
										combustionDuration = combustionDuration * (minTemp / newTemp);
									} catch (Exception e) {
										UserInterface.addLayeredComponent(
											new MessageWindow(
												"Invalid temperature",
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
									}
								}
							},
							"Change",
							true,
							String.format("%.1f", temperature)
						)
					);
				}
			},
			Color.WHITE,
			Color.GREEN,
			Color.GRAY,
			null
		);
		
		if (GameWorld.selectedIndividuals.size() == 1 && !(GameWorld.selectedIndividuals.iterator().next().getAI().getCurrentTask() instanceof Trading)) {
			final Individual selected = GameWorld.selectedIndividuals.iterator().next();
			ContextMenuItem openChestMenuItem = new ContextMenuItem(
				"Open furnace",
				new Task() {
					@Override
					public void execute() {
						if (ClientServerInterface.isServer()) {
							selected.getAI().setCurrentTask(
								new TradeWith(selected, container)
							);
						} else {
							ConstructionContainer chestContainer = (ConstructionContainer) container;
							ClientServerInterface.SendRequest.sendTradeWithPropRequest(selected, chestContainer.propId);
						}
					}
				},
				Color.WHITE,
				Color.GREEN,
				Color.GRAY,
				null
			);
			menu.addMenuItem(openChestMenuItem);
		}
		
		if (!burning) {
			menu.addMenuItem(changeTempItem);
		}
		
		return menu;
	}


	@Override
	public void synchronize(Prop other) {
		if (other instanceof Furnace) {
			this.temperature = ((Furnace) other).temperature;
			this.container.synchronize(((Furnace)other).container);
		} else {
			throw new RuntimeException("Can not synchronize Furnace with " + other.getClass().getSimpleName());
		}
	}


	/**
	 * Ignites this furnace
	 */
	public void ignite() {
		burning = true;
		temperature = 1400f;
	}


	@Override
	protected void internalRender(float constructionProgress) {
		BloodAndMithrilClient.spriteBatch.draw(furnace, position.x - width / 2, position.y);
	}


	/**
	 * Returns the {@link #temperature} of this furnace.
	 */
	public float getTemperature() {
		return temperature;
	}


	public float getCombustionDuration() {
		return combustionDuration;
	}


	public void setCombustionDuration(float combustionDuration) {
		this.combustionDuration = combustionDuration;
	}


	public boolean isBurning() {
		return burning;
	}
}