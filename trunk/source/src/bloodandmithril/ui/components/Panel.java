package bloodandmithril.ui.components;

import java.util.Deque;
import java.util.List;

/**
 * A renderable panel that sits inside {@link Component}s
 *
 * @author Matt
 */
public abstract class Panel {
	
	/** Position of this {@link Panel} */
	public int x, y;
	
	/** Dimensions of this {@link Panel} */
	public int width, height;
	
	/** The {@link Component} that this {@link Panel} lives on */
	protected final Component parent;
	
	/**
	 * Protected constructor
	 */
	protected Panel(Component parent) {
		this.parent = parent;
	}
	
	/** Called when left mouse button is clicked */
	public abstract boolean leftClick(List<ContextMenu> copy, Deque<Component> windowsCopy);

	/** Called when left click is released */
	public abstract void leftClickReleased();

	/** Renders this panel */
	public abstract void render();
}