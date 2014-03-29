package bloodandmithril.ui.components.window;

import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Deque;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.task.Construct;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.prop.building.Construction;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.util.Util.Colors;

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
		renderProgressBar();
		super.internalWindowRender();

		constructButton.render(
			x + width/2,
			y - height + 65,
			isActive() && construction.canConstruct(),
			getAlpha()
		);
		
		if (construction.getConstructionProgress() == 1f) {
			setClosing(true);
		}
	}
	
	
	/**
	 * Renders the progress bar that indicates the current fuel burning status of the furnace
	 */
	private void renderProgressBar() {
		UserInterface.shapeRenderer.begin(ShapeType.FilledRectangle);

		int maxWidth = width / 2 + 5;
		float fraction = construction.getConstructionProgress();
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
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		super.internalLeftClick(copy, windowsCopy);
		constructButton.click();
	}
	
	
	private void beginConstruction() {
		if (ClientServerInterface.isServer()) {
			((Individual) proposer).getAI().setCurrentTask(new Construct(((Individual) proposer), construction));
		} else {
			ClientServerInterface.SendRequest.sendConstructRequest(((Individual) proposer).getId().getId(), construction.id);
		}
	}
}