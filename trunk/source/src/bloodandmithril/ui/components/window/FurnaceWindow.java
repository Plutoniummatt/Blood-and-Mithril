package bloodandmithril.ui.components.window;

import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.character.Individual;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.item.Item;
import bloodandmithril.item.material.Fuel;
import bloodandmithril.item.material.fuel.Coal;
import bloodandmithril.prop.building.Furnace;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
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
		() -> {
			ignite();
		},
		Color.GREEN,
		Color.ORANGE,
		Color.WHITE,
		UIRef.BL
	);
	
	/** Button that begins the smelting process */
	private final Button smeltButton = new Button(
		"Smelt",
		defaultFont,
		0,
		0,
		70,
		16,
		() -> {
			smelt();
		},
		Color.GREEN,
		Color.ORANGE,
		Color.WHITE,
		UIRef.BL
	);


	/**
	 * Constructor
	 */
	public FurnaceWindow(int x, int y, int length, int height, String title, boolean active, int minLength, int minHeight, Individual proposer, Furnace furnace) {
		super(x, y, length, height, title, active, minLength, minHeight, proposer, furnace);
		this.furnace = furnace;
	}


	@Override
	protected void internalWindowRender() {
		if (furnace.isBurning()) {
			for (HashMap<ListingMenuItem<Item>, Integer> hashMap : proposeePanel.getListing()) {
				for (Entry<ListingMenuItem<Item>, Integer> entry : hashMap.entrySet()) {
					if (entry.getKey().t instanceof Coal) {
						entry.getKey().button.setDownColor(Color.GREEN);
						entry.getKey().button.setOverColor(Color.GREEN);
						entry.getKey().button.setIdleColor(Color.GREEN);
						entry.getKey().button.setTask(() -> {});
					} else if (furnace.isSmelting()) {
						entry.getKey().button.setDownColor(Util.Colors.DARK_RED);
						entry.getKey().button.setOverColor(Util.Colors.DARK_RED);
						entry.getKey().button.setIdleColor(Util.Colors.DARK_RED);
						entry.getKey().button.setTask(() -> {});
					}
				}
			}

			// Render burn progress bar
			renderBurnProgressBars();
		}

		super.internalWindowRender();

		if (!furnace.isBurning()) {
			igniteButton.render(
				x + width/2,
				y - height + 65,
				isActive() && isProposeeItemsEmpty(),
				getAlpha()
			);
		} else {
			smeltButton.render(
				x + width/2,
				y - height + 65,
				!furnace.isSmelting() && isActive(),
				getAlpha()
			);
		}
	}
	
	
	@Override
	protected boolean tradeButtonClickable() {
		return super.tradeButtonClickable() && !furnace.isSmelting();
	}


	/**
	 * Renders the progress bar that indicates the current fuel burning status of the furnace
	 */
	private void renderBurnProgressBars() {
		UserInterface.shapeRenderer.begin(ShapeType.FilledRectangle);

		int maxWidth = width / 2 + 5;

		float max = 0f;
		for (Entry<Item, Integer> entry : furnace.getInventory().entrySet()) {
			Item item = entry.getKey();
			if (item instanceof Fuel) {
				max = max + ((Fuel)item).getCombustionDuration() * entry.getValue();
			}
		}
		float fuelFraction = furnace.getCombustionDurationRemaining() / max;
		float smeltingFraction = furnace.getSmeltingDurationRemaining() / Furnace.SMELTING_DURATION;

		Color alphaGreen = Colors.modulateAlpha(Color.GREEN, getAlpha());
		Color alphaRed = Colors.modulateAlpha(Color.RED, getAlpha());

		// Fuel
		UserInterface.shapeRenderer.filledRect(
			x + width / 2 - 10,
			y - 25,
			fuelFraction * maxWidth,
			2,
			alphaGreen,
			alphaGreen,
			alphaGreen,
			alphaGreen
		);
		
		// Smelting
		if (furnace.isSmelting()) {
			UserInterface.shapeRenderer.filledRect(
				x + width / 2 - 10,
				y - 28,
				smeltingFraction * maxWidth,
				2,
				alphaRed,
				alphaRed,
				alphaRed,
				alphaRed
			);
		}
		
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

		if (furnace.isBurning()) {
			smeltButton.click();
		} else {
			if (isProposeeItemsEmpty()) {
				igniteButton.click();
			}
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
		float finalDuration = 0f;
		for (Entry<Item, Integer> entry : proposee.getInventory().entrySet()) {
			Item item = entry.getKey();
			if (item instanceof Fuel) {
				finalDuration = finalDuration + ((Fuel) item).getCombustionDuration() * entry.getValue();
			}
		}

		if (finalDuration == 0f) {
			UserInterface.addLayeredComponent(
				new MessageWindow(
					"Add coal to the furnace before attemping to ignite",
					Color.RED,
					BloodAndMithrilClient.WIDTH/2 - 175,
					BloodAndMithrilClient.HEIGHT/2 + 100,
					350,
					200,
					"No coal",
					true,
					100,
					100
				)
			);
			return;
		}
		
		if (ClientServerInterface.isServer()) {
			furnace.setCombustionDurationRemaining(finalDuration);
			furnace.ignite();
		} else {
			ClientServerInterface.SendRequest.sendIgniteFurnaceRequest(furnace.id);
		}
	}
	
	
	/**
	 * Begins the smelting
	 */
	private void smelt() {
		if (ClientServerInterface.isServer()) {
			if (furnace.getInventory().isEmpty()) {
				UserInterface.addLayeredComponent(
					new MessageWindow(
						"Can not smelt nothing",
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

			furnace.smelt();
		} else {
			ClientServerInterface.SendRequest.sendFurnaceSmeltRequest(furnace.id);
		}
	}
}