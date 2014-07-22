package bloodandmithril.ui.components.window;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;

import java.util.Deque;
import java.util.List;

import bloodandmithril.character.ai.task.TradeWith;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.construction.craftingstation.CraftingStation;
import bloodandmithril.prop.construction.craftingstation.Furnace;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.util.Fonts;

import com.badlogic.gdx.graphics.Color;

/**
 * {@link Furnace} specific {@link CraftingStationWindow}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class FurnaceCraftingWindow extends CraftingStationWindow {

	private Button addFuel;

	/**
	 * Constructor
	 */
	public FurnaceCraftingWindow(int x, int y, String title, Individual individual, CraftingStation craftingStation) {
		super(x, y, title, individual, craftingStation);

		addFuel = new Button(
			"Add fuel",
			Fonts.defaultFont,
			0,
			0,
			90,
			16,
			() -> {
				if (ClientServerInterface.isServer()) {
					individual.getAI().setCurrentTask(
						new TradeWith(individual, craftingStation)
					);
				} else {
					ClientServerInterface.SendRequest.sendTradeWithPropRequest(individual, craftingStation.id);
				}
			},
			Color.ORANGE,
			Color.GREEN,
			Color.WHITE,
			UIRef.BL
		);
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		super.internalLeftClick(copy, windowsCopy);
		addFuel.click();
	}


	@Override
	protected void renderButtons() {
		showInfoButton.render(
			x + width / 2 + 11,
			y - 45,
			isActive(),
			getAlpha()
		);

		addFuel.render(
			x + width / 2 + 11,
			y - 65,
			isActive(),
			getAlpha()
		);
		
		craftButton.render(
			x + width / 2 + 11,
			y - 85,
			isActive() && (craftingStation.getCurrentlyBeingCrafted() == null || !craftingStation.isOccupied()) && !craftingStation.isFinished() && (enoughMaterials || craftingStation.getCurrentlyBeingCrafted() != null) && customCanCraft(),
			getAlpha()
		);
		
		takeFinishedItemButton.render(
			x + width / 2 + 11,
			y - 105,
			isActive() && craftingStation.isFinished(),
			getAlpha()
		);
		
		spriteBatch.flush();
	}
}
