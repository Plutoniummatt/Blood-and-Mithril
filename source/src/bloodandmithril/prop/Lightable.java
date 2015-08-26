package bloodandmithril.prop;

import bloodandmithril.character.ai.perception.Visible;
import bloodandmithril.core.Copyright;

/**
 * Something that can be lit
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public interface Lightable extends Visible {

	/**
	 * Lights this thing
	 */
	public void light();

	/**
	 * Extinguishes this thing
	 */
	public void extinguish();
	
	/**
	 * True if is lit
	 */
	public boolean isLit();
	
	/**
	 * True if is lit
	 */
	public boolean canLight();
}