package bloodandmithril.objectives;

import java.util.List;

import bloodandmithril.core.Copyright;
import bloodandmithril.event.Event;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.window.MissionsWindow;

import com.google.common.collect.Lists;

/**
 * Missions have multiple {@link Objective}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public abstract class Mission implements Objective {
	private static final long serialVersionUID = -3237913948268389651L;

	private List<Objective> objectives = Lists.newLinkedList();
	protected int worldId;

	/**
	 * Constructor
	 */
	protected Mission(int worldId) {
		this.worldId = worldId;
		objectives.addAll(getNewObjectives());

		if (objectives.isEmpty()) {
			throw new IllegalStateException("Can not have a mission with no objectives");
		}
	}


	public void update() {
		for (Objective objective : Lists.newLinkedList(objectives)) {
			if (objective != null && objective.getStatus() == ObjectiveStatus.COMPLETE) {
				objective.uponCompletion();
				UserInterface.refreshRefreshableWindows(MissionsWindow.class);
			}
		}
	}


	public void addObjective(Objective o) {
		objectives.add(o);
	}


	public List<Objective> getObjectives() {
		return Lists.newLinkedList(objectives);
	}


	@Override
	public ObjectiveStatus getStatus() {
		for (Objective o : objectives) {
			if (o.getStatus() == ObjectiveStatus.FAILED) {
				return ObjectiveStatus.FAILED;
			}
		}

		for (Objective o : objectives) {
			if (o.getStatus() == ObjectiveStatus.ACTIVE) {
				return ObjectiveStatus.ACTIVE;
			}
		}

		return ObjectiveStatus.COMPLETE;
	}


	@Override
	public void renderHints() {
		for (Objective objective : getObjectives()) {
			objective.renderHints();
		}
	}


	@Override
	public void listen(Event event) {
		for (Objective objective : getObjectives()) {
			objective.listen(event);
		}
		update();
	}


	/**
	 * @return the description of this {@link Mission}
	 */
	public abstract String getDescription();


	/**
	 * Populates the map of {@link Objective}s for this {@link Mission}
	 */
	protected abstract List<Objective> getNewObjectives();
}