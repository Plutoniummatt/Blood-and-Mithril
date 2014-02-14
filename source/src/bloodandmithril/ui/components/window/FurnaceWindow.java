package bloodandmithril.ui.components.window;

import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Deque;
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
import bloodandmithril.util.JITTask;
import bloodandmithril.util.Task;

import com.badlogic.gdx.graphics.Color;

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
								
								if (newTemp > Furnace.maxTemp) {
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

								if (newTemp < Furnace.minTemp) {
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

								furnace.setTemperature(newTemp);
								furnace.setCombustionDurationRemaining(furnace.getCombustionDurationRemaining() * (Furnace.minTemp / newTemp));
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
		super.internalWindowRender();

		igniteButton.render(
			x + width/2,
			y - height + 65,
			!furnace.isBurning() && active && isProposeeItemsEmpty(),
			alpha
		);
		
		changeTemperatureButton.render(
			x + width/2,
			y - height + 90,
			furnace.isBurning() && active,
			alpha
		);
	}
	
	
	@Override
	protected void uponClose() {
		super.uponClose();
		
		if (temperatureInputWindow != null) {
			temperatureInputWindow.closing = true;
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
		buyerPanel.x = x;
		buyerPanel.y = y;
		buyerPanel.height = height - 70;
		buyerPanel.width = width / 2 - 10;

		sellerPanel.x = x + width / 2 + 10;
		sellerPanel.y = y;
		sellerPanel.height = height - 70;
		sellerPanel.width = width / 2 - 10;

		buyerPanel.render();
		sellerPanel.render();
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
					finalDuration = finalDuration + ((Fuel) item).getCombustionDuration() * entry.getValue() * (Furnace.minTemp / furnace.getTemperature());
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
			//TODO send request to ignite furnace
		}
	}
}