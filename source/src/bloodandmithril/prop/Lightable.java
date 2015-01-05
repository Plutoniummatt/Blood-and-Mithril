package bloodandmithril.prop;

import bloodandmithril.core.Copyright;

/**
 * Something that can be lit
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public interface Lightable {

	/**
	 * Lights this thing
	 */
	public void light();

	/**
	 * Extinguishes this thing
	 */
	public void extinguish();
}