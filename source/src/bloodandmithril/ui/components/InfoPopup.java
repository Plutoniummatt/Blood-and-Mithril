package bloodandmithril.ui.components;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.core.BloodAndMithrilClient.getGraphics;

import java.util.Deque;
import java.util.List;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.core.Copyright;
import bloodandmithril.util.Function;

/**
 * Displays mouse-over popup info
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class InfoPopup extends Component {

	private final Color borderColor, backGroundColor;
	public final Function<Boolean> expiryFunction;
	private Panel panel;

	/**
	 * Constructor
	 */
	public InfoPopup(Panel panel, Function<Boolean> expiryFunction) {
		this.panel = panel;
		this.expiryFunction = expiryFunction;
		this.borderColor = Color.GRAY;
		this.backGroundColor = Color.BLACK;

		this.panel.parent = this;
	}


	@Override
	public boolean keyPressed(int keyCode) {
		return false;
	}


	@Override
	public boolean leftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		return false;
	}


	@Override
	public void leftClickReleased() {
	}


	@Override
	protected void internalComponentRender() {
		setActive(true);
		getGraphics().getSpriteBatch().begin();
		renderRectangle(
			getMouseScreenX() + bottomLeft.getRegionWidth() + 10,
			getMouseScreenY() + bottomLeft.getRegionHeight() - 10,
			panel.width + 10,
			panel.height + 10,
			true,
			backGroundColor
		);

		renderBox(
			getMouseScreenX() + 10,
			getMouseScreenY() - 10,
			panel.width + 10,
			panel.height + 10,
			isActive(),
			borderColor
		);
		getGraphics().getSpriteBatch().end();

		getGraphics().getSpriteBatch().begin();
		panel.x = getMouseScreenX() + 20;
		panel.y = getMouseScreenY() - 20;
		panel.render();
		getGraphics().getSpriteBatch().end();
	}
}