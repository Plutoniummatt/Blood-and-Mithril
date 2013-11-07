package bloodandmithril.ui.components.panel;

import java.util.Deque;
import java.util.List;

import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.Panel;
import bloodandmithril.ui.components.window.Window;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;


/**
 * A listing view that is scrollable
 *
 * @author Matt
 */
public class ScrollableListingPanel extends Panel {

	/** List of buttons to be rendered */
	private List<Button> buttonListing;

	/** Color of the scroll bar */
	private final Color scrollBarColor;

	/** The current starting rendering index of {@link #buttonListing} */
	private int currentStartingIndex;

	/**
	 * Constructor
	 */
	public ScrollableListingPanel(Window parent, Color color, int parentOffsetX, int parentOffsetY, int width, int height, Color scrollBarColor) {
		super(parent, color, parentOffsetX, parentOffsetY, width, height);
		this.scrollBarColor = scrollBarColor;
	}


	/**
	 * @see bloodandmithril.ui.components.Panel#internalPanelRender()
	 */
	@Override
	protected void internalPanelRender() {
		shapeRenderer.begin(ShapeType.FilledRectangle);
		shapeRenderer.setColor(scrollBarColor.r, scrollBarColor.g, scrollBarColor.b, 0.5f * parent.alpha);

		shapeRenderer.filledRect(
			parent.x + parentOffsetX + bottomLeft.getRegionWidth() + width - 1 - 10,
			parent.y - parentOffsetY + bottomLeft.getRegionHeight() - height + 1,
			10,
			height - 2
		);

		shapeRenderer.end();

		renderListing();
	}


	/**
	 * Renders {@link #buttonListing}
	 */
	private void renderListing() {
		for (Button button : buttonListing) {
		}
	}


	/**
	 * @see bloodandmithril.ui.components.Component#leftClick(java.util.List, java.util.Deque)
	 */
	@Override
	public boolean leftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		return false;
	}
}