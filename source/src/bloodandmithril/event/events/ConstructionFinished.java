package bloodandmithril.event.events;

import bloodandmithril.core.Copyright;
import bloodandmithril.event.Event;
import bloodandmithril.prop.construction.Construction;

/**
 * An event to signal that a construction has finished being constructed
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class ConstructionFinished extends Event {
	private static final long serialVersionUID = 2102460238420158266L;
	private final int constructionId;
	private final int worldId;

	/**
	 * Constructor
	 */
	public ConstructionFinished(Construction construction) {
		super();
		this.constructionId = construction.id;
		this.worldId = construction.getWorldId();
	}

	public int getConstructionId() {
		return constructionId;
	}

	public int getWorldId() {
		return worldId;
	}
}