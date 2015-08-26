package bloodandmithril.character.ai.task;

import static bloodandmithril.character.ai.task.GoToLocation.goToWithTerminationFunction;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.pathfinding.PathFinder;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.PropItem;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

import com.badlogic.gdx.math.Vector2;

/**
 * A composite AI task that instructs the host to go to location then place a {@link Prop}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class PlaceProp extends CompositeAITask {
	private static final long serialVersionUID = 6459464517158625281L;
	private Vector2 position;
	private PropItem propItem;

	public PlaceProp(Individual host, Vector2 position, PropItem propItem) {
		super(host.getId(), "Placing");
		this.position = position;
		this.propItem = propItem;
		
		try {
			appendTask(
				goToWithTerminationFunction(
					host,
					host.getState().position.cpy(),
					new WayPoint(PathFinder.getGroundAboveOrBelowClosestEmptyOrPlatformSpace(position, 10, Domain.getWorld(host.getWorldId())), 0),
					false,
					new WithinInteractionBox(),
					true
				)
			);
		} catch (NoTileFoundException e) {}
		
		appendTask(new Place(position, hostId));
	}

	
	public class WithinInteractionBox implements SerializableFunction<Boolean> {
		private static final long serialVersionUID = -6658375092168650175L;

		@Override
		public Boolean call() {
			return Domain.getIndividual(hostId.getId()).getInteractionBox().isWithinBox(position);
		}
	}

	
	public class Place extends AITask {
		private static final long serialVersionUID = 7788789888406267718L;
		private boolean placed;

		protected Place(Vector2 position, IndividualIdentifier hostId) {
			super(hostId);
		}

		
		@Override
		public String getDescription() {
			return "Placing";
		}

		
		@Override
		public boolean isComplete() {
			return placed;
		}

		
		@Override
		public boolean uponCompletion() {
			return false;
		}

		
		@Override
		public void execute(float delta) {
			Prop prop = propItem.getProp();
			Individual host = getHost();
			prop.setWorldId(host.getWorldId());
			if (host.has(propItem) > 0 && host.getInteractionBox().isWithinBox(position) && prop.canPlaceAt(position)) {
				prop.position.x = position.x;
				prop.position.y = position.y;
				Domain.getWorld(host.getWorldId()).props().addProp(prop);
				host.takeItem(propItem);
				UserInterface.refreshRefreshableWindows();
			}
			
			placed = true;
		}
	}
}