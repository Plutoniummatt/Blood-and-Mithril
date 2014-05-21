package bloodandmithril.ui.components.window;

import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import bloodandmithril.character.Individual;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.item.Fuel;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.material.mineral.Coal;
import bloodandmithril.item.material.mineral.Rock;
import bloodandmithril.prop.construction.craftingstation.Furnace;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
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

	/**
	 * Constructor
	 */
	public FurnaceWindow(int x, int y, int length, int height, String title, boolean active, int minLength, int minHeight, Individual proposer, Furnace furnace) {
		super(x, y, length, height, title, active, minLength, minHeight, proposer, furnace, new Comparator<Item>() {
			@Override
			public int compare(Item o1, Item o2) {
				if (o1 instanceof Fuel ^ o2 instanceof Fuel) {
					return o1 instanceof Fuel ? -1 : 1;
				} else {
					return o1.getSingular(false).compareTo(o2.getSingular(false));
				}
			}
		});
		this.furnace = furnace;
	}


	@Override
	protected void internalWindowRender() {
		if (furnace.isBurning()) {
			for (HashMap<ListingMenuItem<Item>, Integer> hashMap : proposeePanel.getListing()) {
				for (Entry<ListingMenuItem<Item>, Integer> entry : hashMap.entrySet()) {
					if (entry.getKey().t instanceof Fuel) {
						entry.getKey().button.setDownColor(Color.GREEN);
						entry.getKey().button.setOverColor(Color.GREEN);
						entry.getKey().button.setIdleColor(Color.GREEN);
						entry.getKey().button.setTask(() -> {});
					}
				}
			}

			// Render burn progress bar
			renderBurnProgressBars();
		}

		super.internalWindowRender();

		igniteButton.render(
			x + width/2,
			y - height + 65,
			isActive() && isProposeeItemsEmpty() && !furnace.isBurning(),
			getAlpha()
		);
	}


	/**
	 * @return whether or not a listing item is able to be selected for trade
	 */
	@Override
	protected boolean isItemAvailableToTrade(Item item) {
		return item instanceof Rock && ((Rock)item).getMineral().equals(Coal.class); // by default
	}


	/**
	 * Renders the progress bar that indicates the current fuel burning status of the furnace
	 */
	private void renderBurnProgressBars() {
		UserInterface.shapeRenderer.begin(ShapeType.FilledRectangle);

		int maxWidth = width / 2 + 5;

		float max = (float) furnace.getInventory().entrySet().stream().mapToDouble(entry -> {
			if (entry.getKey() instanceof Fuel) {
				return ((Fuel)entry.getKey()).getCombustionDuration() * entry.getValue();
			} else {
				return 0D;
			}
		}).sum();

		float fuelFraction = furnace.getCombustionDurationRemaining() / max;

		Color alphaGreen = Colors.modulateAlpha(Color.GREEN, isActive() ? getAlpha() : getAlpha() * 0.6f);

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

		UserInterface.shapeRenderer.end();
	}


	@Override
	protected void uponClose() {
		super.uponClose();
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		super.internalLeftClick(copy, windowsCopy);

		if (isProposeeItemsEmpty() && !furnace.isBurning()) {
			igniteButton.click();
		}
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
					"Add fuel to the furnace before attemping to ignite",
					Color.RED,
					BloodAndMithrilClient.WIDTH/2 - 175,
					BloodAndMithrilClient.HEIGHT/2 + 100,
					350,
					200,
					"No fuel",
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
}