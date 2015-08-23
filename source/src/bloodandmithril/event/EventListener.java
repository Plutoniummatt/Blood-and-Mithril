package bloodandmithril.event;

import java.io.Serializable;

import bloodandmithril.core.Copyright;

/**
 * A listener of {@link Event}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public interface EventListener extends Serializable {

	/**
	 * Listens to an {@link Event}
	 */
	public void listen(Event event);
}