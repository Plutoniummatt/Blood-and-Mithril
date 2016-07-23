package bloodandmithril.character.ai.task.placeprop;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.AITaskExecutor;
import bloodandmithril.character.ai.task.placeprop.PlaceProp.Place;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.world.Domain;

/**
 * Executes {@link Place}
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class PlaceExecutor implements AITaskExecutor {
	
	@Inject private UserInterface userInterface;
	
	@Override
	public void execute(AITask aiTask, float delta) {
		Place task = (Place) aiTask;
		
		final Prop prop = task.getParent().propItem.getProp();
		final Individual host = task.getHost();
		prop.setWorldId(host.getWorldId());
		if (host.has(task.getParent().propItem) > 0 && host.getInteractionBox().isWithinBox(task.getParent().position) && prop.canPlaceAt(task.getParent().position)) {
			prop.position.x = task.getParent().position.x;
			prop.position.y = task.getParent().position.y;
			Domain.getWorld(host.getWorldId()).props().addProp(prop);
			host.takeItem(task.getParent().propItem);
			userInterface.refreshRefreshableWindows();
		}

		task.placed = true;		
	}
	

	@Override
	public boolean isComplete(AITask aiTask) {
		return ((Place) aiTask).placed;
	}

	
	@Override
	public boolean uponCompletion(AITask aiTask) {
		return false;
	}
}
