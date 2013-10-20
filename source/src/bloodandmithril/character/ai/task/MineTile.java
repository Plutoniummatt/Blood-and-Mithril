package bloodandmithril.character.ai.task;


import bloodandmithril.audio.SoundService;
import bloodandmithril.character.Individual;
import bloodandmithril.character.Individual.IndividualIdentifier;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.pathfinding.PathFinder;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.util.Task;
import bloodandmithril.world.GameWorld;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;

import com.badlogic.gdx.math.Vector2;

/**
 * Mine a {@link Tile}, a {@link CompositeAITask} comprising of:
 * 
 * {@link GoToLocation} of the tile.
 * {@link Mine} mine the actual tile.
 *
 * @author Matt
 */
public class MineTile extends CompositeAITask {
	private static final long serialVersionUID = -4098496856332182430L;

	/** Coordinate of the tile to mine */
	private final Vector2 tileCoordinate;

	/**
	 * Constructor
	 *
	 * @param coordinate - World pixel coordinate of the tile to mine, does not have to be a converted tile coordinate
	 */
	public MineTile(Individual host, Vector2 coordinate) {
		super(
			host.id, 
			"Mining", 
			new GoToLocation(
				host, 
				new WayPoint(PathFinder.getGroundAboveOrBelowClosestEmptyOrPlatformSpace(coordinate, 10), 3 * Topography.TILE_SIZE), 
				false,
				50f, 
				true
			)
		);
		
		appendTask(this.new Mine(host.id));
		
		this.tileCoordinate = coordinate;
	}
	
	
	/**
	 * Task of mining a tile
	 *
	 * @author Matt
	 */
	public class Mine extends AITask {
		private static final long serialVersionUID = 7585777004625914828L;

		/**
		 * Constructor
		 */
		public Mine(IndividualIdentifier hostId) {
			super(hostId);
		}

		
		@Override
		public String getDescription() {
			return "Mining";
		}

		
		@Override
		public boolean isComplete() {
			return Topography.getTile(tileCoordinate, true) instanceof EmptyTile;
		}

		
		@Override
		public void uponCompletion() {
		}

		
		@Override
		public void execute() {
			Individual host = GameWorld.individuals.get(hostId.id);
			
			if (host.interactionBox.isWithinBox(tileCoordinate)) {
				Topography.addTask(
					new Task() {
						@Override
						public void execute() {
							if (Topography.deleteTile(tileCoordinate.x, tileCoordinate.y, true)) {
								SoundService.pickAxe.play(
									SoundService.getVolumne(tileCoordinate), 
									1f, 
									SoundService.getPan(tileCoordinate)
								);
							}
						}
					}
				);
			}
		}
	}
}