package bloodandmithril.ui.components.window;

import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.item.Container;
import bloodandmithril.item.Item;
import bloodandmithril.item.material.Fuel;
import bloodandmithril.prop.building.Furnace;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.util.JITTask;
import bloodandmithril.util.Task;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

/**
 * {@link Window} used for the {@link Furnace} to heat things
 *
 * @author Matt
 */
public class FurnaceWindow extends TradeWindow {

	/** The {@link Furnace} backing this {@link FurnaceWindow} */
	private final Furnace furnace;

	/** The {@link TextInputWindow} that is responsible for changing the furnace temperature */
	private TextInputWindow temperatureInputWindow;

	/** Button that ignites the furnace */
	private final Button igniteButton = new Button(
		"Ignite",
		defaultFont,
		0,
		0,
		60,
		16,
		new Task() {
			@Override
			public void execute() {
				ignite();
			}
		},
		Color.GREEN,
		Color.ORANGE,
		Color.WHITE,
		UIRef.BL
	);

	/** Button that changes temperature of the furnace */
	private final Button changeTemperatureButton = new Button(
		"Change temperature",
		defaultFont,
		0,
		0,
		180,
		16,
		new Task() {
			@Override
			public void execute() {
				temperatureInputWindow = new TextInputWindow(
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
								if (!furnace.isBurning()) {
									UserInterface.addLayeredComponent(
										new MessageWindow(
											"Furnace is not burning",
											Color.RED,
											BloodAndMithrilClient.WIDTH/2 - 150,
											BloodAndMithrilClient.HEIGHT/2 + 50,
											300,
											100,
											"Furnace",
											true,
											300,
											100
										)
									);
									return;
								}

								if (newTemp > Furnace.MAX_TEMP) {
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

								if (newTemp < Furnace.MIN_TEMP) {
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

								if (ClientServerInterface.isServer()) {
									furnace.setCombustionDurationRemaining(furnace.getCombustionDurationRemaining() * (furnace.getTemperature() / newTemp));
									furnace.setTemperature(newTemp);
								} else {
									ClientServerInterface.SendRequest.sendChangeFurnaceTemperatureRequest(furnace.id, newTemp);
								}

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
					String.format("%.1f", furnace.getTemperature())
				);

				UserInterface.addLayeredComponent(
					temperatureInputWindow
				);
			}
		},
		Color.GREEN,
		Color.ORANGE,
		Color.WHITE,
		UIRef.BL
	);

	/**
	 * Constructor
	 */
	public FurnaceWindow(int x, int y, int length, int height, String title, boolean active, int minLength, int minHeight, Container proposer, Furnace furnace) {
		super(x, y, length, height, title, active, minLength, minHeight, proposer, furnace.container);
		this.furnace = furnace;
	}


	@Override
	protected void internalWindowRender() {
		if (furnace.isBurning()) {
			for (HashMap<ListingMenuItem<Item>, Integer> hashMap : proposeePanel.getListing()) {
				for (Entry<ListingMenuItem<Item>, Integer> entry : hashMap.entrySet()) {
					entry.getKey().button.setDownColor(Util.Colors.DARK_RED);
					entry.getKey().button.setOverColor(Util.Colors.DARK_RED);
					entry.getKey().button.setIdleColor(Util.Colors.DARK_RED);
					entry.getKey().button.setTask(new Task() {
						@Override
						public void execute() {
							// Do nothing
						}
					});
				}
			}

			// Render burn progress bar
			renderBurnProgressBar();
		}

		super.internalWindowRender();

		igniteButton.render(
			x + width/2,
			y - height + 65,
			!furnace.isBurning() && isActive() && isProposeeItemsEmpty(),
			getAlpha()
		);

		changeTemperatureButton.render(
			x + width/2,
			y - height + 90,
			furnace.isBurning() && isActive(),
			getAlpha()
		);
	}
	
	
	@Override
	protected boolean tradeButtonClickable() {
		return !furnace.isBurning() && super.tradeButtonClickable();
	}


	/**
	 * Renders the progress bar that indicates the current fuel burning status of the furnace
	 */
	private void renderBurnProgressBar() {
		UserInterface.shapeRenderer.begin(ShapeType.FilledRectangle);

		int maxWidth = width / 2 + 5;

		float max = 0f;
		for (Entry<Item, Integer> entry : furnace.container.getInventory().entrySet()) {
			Item item = entry.getKey();
			if (item instanceof Fuel) {
				max = max + ((Fuel)item).getCombustionDuration() * (Furnace.MIN_TEMP / furnace.getTemperature()) * entry.getValue();
			}
		}
		float fraction = furnace.getCombustionDurationRemaining() / max;

		Color alphaGreen = Colors.modulateAlpha(Color.GREEN, getAlpha());

		UserInterface.shapeRenderer.filledRect(
			x + width / 2 - 10,
			y - 25,
			fraction * maxWidth,
			2,
			alphaGreen,
			alphaGreen,
			alphaGreen,
			alphaGreen
		);
		UserInterface.shapeRenderer.end();
	}


	@Override
	protected void uponClose() {
		super.uponClose();

		if (temperatureInputWindow != null) {
			temperatureInputWindow.setClosing(true);
		}
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		super.internalLeftClick(copy, windowsCopy);

		if (isProposeeItemsEmpty()) {
			igniteButton.click();
		}

		if (furnace.isBurning()) {
			changeTemperatureButton.click();
		}
	}


	/**
	 * Renders the listing panels
	 */
	@Override
	protected void renderListingPanels() {
		proposerPanel.x = x;
		proposerPanel.y = y;
		proposerPanel.height = height - 70;
		proposerPanel.width = width / 2 - 10;

		proposeePanel.x = x + width / 2 + 10;
		proposeePanel.y = y;
		proposeePanel.height = height - 70;
		proposeePanel.width = width / 2 - 10;

		proposerPanel.render();
		proposeePanel.render();
	}


	/**
	 * ignites the furnace
	 */
	private void ignite() {
		if (ClientServerInterface.isServer()) {
			float finalDuration = 0f;
			for (Entry<Item, Integer> entry : proposee.getInventory().entrySet()) {
				Item item = entry.getKey();
				if (item instanceof Fuel) {
					finalDuration = finalDuration + ((Fuel) item).getCombustionDuration() * entry.getValue() * (Furnace.MIN_TEMP / Furnace.MIN_TEMP);
				}
			}

			if (finalDuration == 0f) {
				UserInterface.addLayeredComponent(
					new MessageWindow(
						"No fuel added to furnace",
						Color.RED,
						BloodAndMithrilClient.WIDTH/2 - 175,
						BloodAndMithrilClient.HEIGHT/2 + 100,
						350,
						200,
						"Furnace",
						true,
						100,
						100
					)
				);
				return;
			}

			furnace.setCombustionDurationRemaining(finalDuration);
			furnace.ignite();
		} else {
			ClientServerInterface.SendRequest.sendIgniteFurnaceRequest(furnace.id);
		}
	}
}