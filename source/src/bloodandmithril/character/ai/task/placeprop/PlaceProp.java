package bloodandmithril.character.ai.task.placeprop;

import static bloodandmithril.character.ai.task.gotolocation.GoToLocation.goToWithTerminationFunction;

import com.badlogic.gdx.math.Vector2;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ExecutedBy;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.pathfinding.PathFinder;
import bloodandmithril.character.ai.task.compositeaitask.CompositeAITask;
import bloodandmithril.character.ai.task.compositeaitask.CompositeAITaskExecutor;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.PropItem;
import bloodandmithril.prop.Prop;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * A composite AI task that instructs the host to go to location then place a {@link Prop}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
@ExecutedBy(CompositeAITaskExecutor.class)
public class PlaceProp extends CompositeAITask {
	private static final long serialVersionUID = 6459464517158625281L;

	Vector2 position;
	PropItem propItem;

	public PlaceProp(final Individual host, final Vector2 position, final PropItem propItem) {
		super(host.getId(), "Placing");
		this.position = position;
		this.propItem = propItem;

		try {
			appendTask(
				goToWithTerminationFunction(
					host,
					host.getState().position.cpy(),
					new WayPoint(PathFinder.getGroundAboveOrBelowClosestEmptyOrPlatformSpace(position, 10, Domain.getWorld(host.getWorldId())).get(), 0),
					false,
					new WithinInteractionBox(),
					true
				)
			);
		} catch (final NoTileFoundException e) {}

		appendTask(new Place(position, hostId));
	}


	public class WithinInteractionBox implements SerializableFunction<Boolean> {
		private static final long serialVersionUID = -6658375092168650175L;

		@Override
		public Boolean call() {
			return Domain.getIndividual(hostId.getId()).getInteractionBox().isWithinBox(position);
		}
	}


	@ExecutedBy(PlaceExecutor.class)
	public class Place extends AITask {
		private static final long serialVersionUID = 7788789888406267718L;
		boolean placed;

		protected Place(final Vector2 position, final IndividualIdentifier hostId) {
			super(hostId);
		}
		
		
		PlaceProp getParent() {
			return PlaceProp.this;
		}


		@Override
		public String getShortDescription() {
			return "Placing";
		}
	}
}