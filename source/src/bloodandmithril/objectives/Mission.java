package bloodandmithril.objectives;

import java.util.List;
import java.util.TreeMap;

import bloodandmithril.core.Copyright;

import com.google.common.collect.Maps;

/**
 * Missions have multiple {@link Objective}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public abstract class Mission implements Objective {

	private TreeMap<Integer, Objective> objectives = Maps.newTreeMap();
	private int currentObjective;

	/**
	 * Constructor
	 */
	protected Mission() {
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


	public void update() {
		if (objectives.get(currentObjective).isComplete()) {
			currentObjective = objectives.ceilingKey(currentObjective + 1);
		}
	}


	@Override
	public boolean isComplete() {
		for (Objective o : objectives.values()) {
			if (!o.isComplete()) {
				return false;
			}
		}
		return true;
	}


	@Override
	public boolean hasFailed() {
		for (Objective o : objectives.values()) {
			if (o.hasFailed()) {
				return true;
			}
		}
		return false;
	}


	@Override
	public void renderHints() {
		objectives.get(currentObjective).renderHints();
	}


	/**
	 * Populates the map of {@link Objective}s for this {@link Mission}
	 */
	protected abstract List<Objective> getNewObjectives();
}