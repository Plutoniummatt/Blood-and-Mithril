package bloodandmithril.ui.components.window;

import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import bloodandmithril.character.ai.task.LightLightable;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.Fuel;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.item.items.mineral.earth.Ashes;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Lightable;
import bloodandmithril.prop.construction.craftingstation.FueledCraftingStation;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

/**
 * {@link Window} used for the {@link FueledCraftingStation} to heat things
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class FueledCraftingStationFuelWindow extends TradeWindow {

	/** The {@link FueledCraftingStation} backing this {@link FueledCraftingStationFuelWindow} */
	private final FueledCraftingStation fueledCraftingStation;

	/** Button that ignites the FueledCraftingStation */
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
	public FueledCraftingStationFuelWindow(int x, int y, int length, int height, String title, boolean active, int minLength, int minHeight, Individual proposer, FueledCraftingStation fueledCraftingStation) {
		super(x, y, length, height, title, active, minLength, minHeight, proposer, fueledCraftingStation, new Comparator<Item>() {
			@Override
			public int compare(Item o1, Item o2) {
				if (o1 instanceof Fuel ^ o2 instanceof Fuel) {
					return o1 instanceof Fuel ? -1 : 1;
				} else {
					return o1.getSingular(false).compareTo(o2.getSingular(false));
				}
			}
		});
		this.fueledCraftingStation = fueledCraftingStation;
	}


	@Override
	protected void internalWindowRender() {
		if (fueledCraftingStation.isBurning()) {
			for (HashMap<ListingMenuItem<Item>, Integer> hashMap : proposeePanel.getListing()) {
				for (Entry<ListingMenuItem<Item>, Integer> entry : hashMap.entrySet()) {
					if (fueledCraftingStation.isValidFuel(entry.getKey().t)) {
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
			x + width/2 + 35,
			y - height + 70,
			isActive() && isProposeeItemsEmpty() && !fueledCraftingStation.isBurning(),
			getAlpha()
		);
	}


	/**
	 * @return whether or not a listing item is able to be selected for trade
	 */
	@Override
	protected boolean isItemAvailableToTrade(Container proposer, Container proposee, Item item) {
		return ((FueledCraftingStation) proposee).isValidFuel(item) || item instanceof Ashes;
	}


	/**
	 * Renders the progress bar that indicates the current fuel burning status of the fueledCraftingStation
	 */
	private void renderBurnProgressBars() {
		UserInterface.shapeRenderer.begin(ShapeType.FilledRectangle);

		int maxWidth = width / 2 + 5;

		float max = (float) fueledCraftingStation.getInventory().entrySet().stream().mapToDouble(entry -> {
			if (fueledCraftingStation.isValidFuel(entry.getKey())) {
				return fueledCraftingStation.deriveCombustionDuration(entry.getKey()) * entry.getValue();
			} else {
				return 0D;
			}
		}).sum();

		float fuelFraction = fueledCraftingStation.getCombustionDurationRemaining() / max;

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

		if (isProposeeItemsEmpty() && !fueledCraftingStation.isBurning()) {
			igniteButton.click();
		}
	}


	/**
	 * ignites the fueledCraftingStation
	 */
	private void ignite() {
		float finalDuration = fueledCraftingStation.calculateCurrentCombutionDuration();

		if (finalDuration == 0f) {
			UserInterface.addLayeredComponent(
				new MessageWindow(
					"Add fuel before attemping to ignite",
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
			if (fueledCraftingStation instanceof Lightable && proposer instanceof Individual) {
				try {
					((Individual) proposer).getAI().setCurrentTask(new LightLightable((Individual) proposer, (Lightable) fueledCraftingStation, false));
				} catch (NoTileFoundException e) {}
			} else {
				fueledCraftingStation.ignite();
			}
		} else {
			if (fueledCraftingStation instanceof Lightable && proposer instanceof Individual) {
				ClientServerInterface.SendRequest.sendLightLightableRequest((Individual) proposer, (Lightable) fueledCraftingStation);
			} else {
				ClientServerInterface.SendRequest.sendIgniteFueledCraftingStationRequest(fueledCraftingStation.id, fueledCraftingStation.getWorldId());
			}
		}
	}
}