package bloodandmithril.objectives;

import java.util.List;
import java.util.TreeMap;

import bloodandmithril.core.Copyright;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.window.MissionsWindow;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Missions have multiple {@link Objective}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public abstract class Mission implements Objective {
	private static final long serialVersionUID = -3237913948268389651L;
	
	private TreeMap<Integer, Objective> objectives = Maps.newTreeMap();
	private int currentObjective;
	protected int worldId;

	/**
	 * Constructor
	 */
	protected Mission(int worldId) {
		this.worldId = worldId;
		int index = 1;
		for (Objective o : getNewObjectives()) {
			objectives.put(index, o);
			index++;
		}

		if (objectives.isEmpty()) {
			throw new IllegalStateException("Can not have a mission with no objectives");
		}

		currentObjective = objectives.firstKey();
	}
	
	
	protected Objective getCurrentObjective() {
		return objectives.get(currentObjective);
	}


	public void update() {
		Objective objective = objectives.get(currentObjective);
		if (objective != null && objective.getStatus() == ObjectiveStatus.COMPLETE) {
			Integer ceilingKey = objectives.ceilingKey(currentObjective + 1);
			currentObjective = ceilingKey == null ? -1 : ceilingKey;
			UserInterface.refreshRefreshableWindows(MissionsWindow.class);
		}
	}


	public List<Objective> getObjectives() {
		return Lists.newLinkedList(objectives.values());
	}


	@Override
	public ObjectiveStatus getStatus() {
		for (Objective o : objectives.values()) {
			if (o.getStatus() == ObjectiveStatus.FAILED) {
				return ObjectiveStatus.FAILED;
			}
		}

		for (Objective o : objectives.values()) {
			if (o.getStatus() == ObjectiveStatus.ACTIVE) {
				return ObjectiveStatus.ACTIVE;
			}
		}

		return ObjectiveStatus.COMPLETE;
	}


	@Override
	public void renderHints() {
		Objective objective = objectives.get(currentObjective);
		if (objective != null) {
			objective.renderHints();
		}
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