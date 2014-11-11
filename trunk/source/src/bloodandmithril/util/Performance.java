package bloodandmithril.util;

import bloodandmithril.core.Copyright;

/**
 * Indicates that something is not performant, and should only be used for debugging
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public @interface Performance {
	String explanation();
}
