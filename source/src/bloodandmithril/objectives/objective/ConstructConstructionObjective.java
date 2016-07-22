package bloodandmithril.objectives.objective;

import bloodandmithril.character.ai.task.construct.Construct;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.event.Event;
import bloodandmithril.event.events.ConstructionFinished;
import bloodandmithril.objectives.Objective;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.world.Domain;

/**
 * An {@link Objective} to {@link Construct} a {@link Construction}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class ConstructConstructionObjective implements Objective {
	private static final long serialVersionUID = 2491217112920284007L;
	private Class<? extends Construction> constructionClass;
	private boolean complete = false;

	/**
	 * Constructor
	 */
	public ConstructConstructionObjective(Class<? extends Construction> constructionClass) {
		this.constructionClass = constructionClass;
	}


	@Override
	public void listen(Event event) {
		if (event instanceof ConstructionFinished) {
			Prop prop = Domain.getWorld(
				((ConstructionFinished) event).getWorldId()
			).props().getProp(
				((ConstructionFinished) event).getConstructionId()
			);

			if (prop.getClass().equals(constructionClass)) {
				complete = true;
			}
		}
	}


	@Override
	public ObjectiveStatus getStatus() {
		if (complete) {
			return ObjectiveStatus.COMPLETE;
		} else {
			return ObjectiveStatus.ACTIVE;
		}
	}


	@Override
	public int getWorldId() {
		return -1;
	}


	@Override
	public void renderHints() {
	}


	@Override
	public String getTitle() {
		return "Construct " + constructionClass.getAnnotation(Name.class).name();
	}


	@Override
	public void uponCompletion() {
	}
}