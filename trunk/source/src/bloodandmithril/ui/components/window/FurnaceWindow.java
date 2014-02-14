package bloodandmithril.ui.components.window;

import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Map.Entry;

import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.item.Container;
import bloodandmithril.item.Item;
import bloodandmithril.item.material.Fuel;
import bloodandmithril.prop.building.Furnace;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
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

	/** Button that ignites the furnace */
	private final Button igniteButton = new Button(
		"Ignite",
		defaultFont,
		0,
		0,
		130,
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
			y - height + 20,
			furnace.isBurning(),
			alpha
		);
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
			furnace.setCombustionDuration(finalDuration);
			furnace.ignite();
		} else {
			//TODO send request to ignite furnace
		}
	}
}