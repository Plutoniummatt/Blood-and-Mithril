package bloodandmithril.character.ai.task.minetile;

import static bloodandmithril.character.ai.task.gotolocation.GoToLocation.goToWithTerminationFunction;

import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ExecutedBy;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.pathfinding.PathFinder;
import bloodandmithril.character.ai.task.compositeaitask.CompositeAITask;
import bloodandmithril.character.ai.task.compositeaitask.CompositeAITaskExecutor;
import bloodandmithril.character.ai.task.gotolocation.GoToLocation;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile;

/**
 * Mine a {@link Tile}, a {@link CompositeAITask} comprising of:
 *
 * {@link GoToLocation} of the tile.
 * {@link MileTileService} mine the actual tile.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@Name(name = "Mine")
@ExecutedBy(CompositeAITaskExecutor.class)
public class MineTile extends CompositeAITask {
	private static final long serialVersionUID = -4098496856332182430L;

	/** Coordinate of the tile to mine */
	public Vector2 tileCoordinate;
	
	@Inject
	MineTile() {
		super(null, "");
	}

	/**
	 * Constructor
	 *
	 * @param coordinate - World pixel coordinate of the tile to mine, does not have to be a converted tile coordinate
	 */
	public MineTile(final Individual host, final Vector2 coordinate) {
		super(
			host.getId(),
			"Mining"
		);

		try {
			appendTask(
				goToWithTerminationFunction(
					host,
					host.getState().position.cpy(),
					new WayPoint(PathFinder.getGroundAboveOrBelowClosestEmptyOrPlatformSpace(coordinate, 10, Domain.getWorld(host.getWorldId())), 0f),
					false,
					new WithinInteractionBox(),
					true
				)
			);
		} catch (final NoTileFoundException e) {}
		appendTask(new AttemptMine(hostId));

		try {
			this.tileCoordinate = Topography.convertToWorldCoord(coordinate, false);
		} catch (final NoTileFoundException e) {
			this.tileCoordinate = coordinate;
		}
	}


	public class WithinInteractionBox implements SerializableFunction<Boolean> {
		private static final long serialVersionUID = -6658375092168650175L;

		@Override
		public Boolean call() {
			return Domain.getIndividual(hostId.getId()).getInteractionBox().isWithinBox(tileCoordinate);
		}
	}


	@ExecutedBy(AttemptMineExecutor.class)
	public class AttemptMine extends AITask {
		private static final long serialVersionUID = 2594481517015647682L;

		/**
		 * Constructor
		 */
		protected AttemptMine(final IndividualIdentifier hostId) {
			super(hostId);
		}
		
		
		public MineTile getParent() {
			return MineTile.this;
		}


		@Override
		public String getShortDescription() {
			return "Mining";
		}
	}
}