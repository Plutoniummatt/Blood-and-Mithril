package bloodandmithril.core;

/**
 * Indicates that something can be mouse-overed
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public interface MouseOverable {

	/**
	 * @return true if the mouse is currently over this entity
	 */
	public boolean isMouseOver();

	/**
	 * @return the context menu item title
	 */
	public String getMenuTitle();

	/**
	 * Highlights this {@link MouseOverable}
	 */
	public void highlight();
}