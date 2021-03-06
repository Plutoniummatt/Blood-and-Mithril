package bloodandmithril.ui.components;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;

import java.util.Deque;
import java.util.List;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;

/**
 * A renderable panel that sits inside {@link Component}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class Panel extends Component {

	/** Position of this {@link Panel} */
	public int x, y;

	/** Dimensions of this {@link Panel} */
	public int width, height;

	/** The {@link Component} that this {@link Panel} lives on */
	protected Component parent;

	/**
	 * Protected constructor
	 */
	protected Panel(Component parent) {
		this.parent = parent;
	}

	/** Called when left mouse button is clicked */
	@Override
	public abstract boolean leftClick(List<ContextMenu> copy, Deque<Component> windowsCopy);

	/** Called when left click is released */
	@Override
	public abstract void leftClickReleased();

	/** Renders this panel */
	@Override
	public abstract void render(Graphics graphics);

	/** True if mouse is within the window */
	protected boolean isMouseWithin() {
		int posX = getMouseScreenX();
		int posY = getMouseScreenY();
		return posX > x && posX < x + width && posY < y && posY > y - height;
	}

	/** Component specific render */
	@Override
	protected void internalComponentRender(Graphics graphics) {
		// Do nothing
	}
}