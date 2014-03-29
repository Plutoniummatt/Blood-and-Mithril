package bloodandmithril.ui.components.window;

import static bloodandmithril.util.Fonts.defaultFont;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.character.Individual;
import bloodandmithril.prop.building.Construction;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;

/**
 * An extension of {@link TradeWindow} for construction
 *
 * @author Matt
 */
public class ConstructionWindow extends TradeWindow {

	/** The construction this window represents */
	private final Construction construction;
	
	/** Button that ignites the furnace */
	private final Button constructButton = new Button(
		"Construct",
		defaultFont,
		0,
		0,
		90,
		16,
		() -> {
			beginConstruction();
		},
		Color.GREEN,
		Color.ORANGE,
		Color.WHITE,
		UIRef.BL
	);

	/**
	 * Construction
	 */
	public ConstructionWindow(int x, int y, int length, int height, String title, boolean active, int minLength, int minHeight, Individual proposer, Construction construction) {
		super(x, y, length, height, title, active, minLength, minHeight, proposer, construction);
		this.construction = construction;
	}

	
	
	@Override
	protected void internalWindowRender() {
		super.internalWindowRender();

		constructButton.render(
			x + width/2,
			y - height + 65,
			isActive() && construction.canConstruct(),
			getAlpha()
		);
	}
	
	
	private void beginConstruction() {
		
	}
}