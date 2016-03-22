package bloodandmithril.event;

import java.io.Serializable;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.persistence.ParameterPersistenceService;

/**
 * An event in game that can notify listeners
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public abstract class Event implements Serializable {
	private static final long serialVersionUID = -3079936825865383835L;

	protected final long eventId;

	/**
	 * Protected constructor
	 */
	protected Event() {
		this.eventId = Wiring.injector().getInstance(ParameterPersistenceService.class).getParameters().getNextEventId();
	}


	/**
	 * @return the unique identifier for this event
	 */
	public long getUniqueID() {
		return eventId;
	}
}