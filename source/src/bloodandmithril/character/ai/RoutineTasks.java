package bloodandmithril.character.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import bloodandmithril.character.ai.task.Attack;
import bloodandmithril.character.ai.task.Follow;
import bloodandmithril.character.ai.task.GoToMovingLocation;
import bloodandmithril.character.ai.task.Harvest;
import bloodandmithril.character.ai.task.LightLightable;
import bloodandmithril.character.ai.task.PlantSeed;
import bloodandmithril.character.ai.task.Speak;
import bloodandmithril.character.ai.task.TakeItem;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;

/**
 * Contains a set of {@link RoutineTask} classes
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class RoutineTasks {

	private static Set<Class<? extends RoutineTask>> routineTasks = Sets.newHashSet();

	static {
		routineTasks.add(Attack.class);
		routineTasks.add(Follow.class);
		routineTasks.add(GoToMovingLocation.class);
		routineTasks.add(Harvest.class);
		routineTasks.add(LightLightable.class);
		routineTasks.add(PlantSeed.class);
		routineTasks.add(Speak.class);
		routineTasks.add(TakeItem.class); // TODO
	}

	public static boolean isRoutineTask(AITask task) {
		return routineTasks.contains(task.getClass());
	}

	public static List<Class<? extends RoutineTask>> getTaskClasses() {
		ArrayList<Class<? extends RoutineTask>> newArrayList = Lists.newArrayList(routineTasks);
		Collections.sort(newArrayList, (e1, e2) -> {
			return e1.getAnnotation(Name.class).name().compareTo(e2.getAnnotation(Name.class).name());
		});
		return newArrayList;
	}
}