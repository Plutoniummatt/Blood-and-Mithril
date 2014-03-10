package bloodandmithril.character.ai.task;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.Individual;
import bloodandmithril.character.Individual.IndividualIdentifier;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.pathfinding.PathFinder;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.window.InventoryWindow;
import bloodandmithril.ui.components.window.Window;
import bloodandmithril.util.Task;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;

import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

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
			host.getId(),
			"Mining",
			new GoToLocation(
				host,
				new WayPoint(PathFinder.getGroundAboveOrBelowClosestEmptyOrPlatformSpace(coordinate, 10, Domain.getWorld(host.getWorldId())), 3 * Topography.TILE_SIZE),
				false,
				50f,
				true
			)
		);

		appendTask(this.new Mine(host.getId()));

		this.tileCoordinate = coordinate;
	}


	/**
	 * Task of mining a tile
	 *
	 * @author Matt
	 */
	public class Mine extends AITask {
		private static final long serialVersionUID = 7585777004625914828L;
		private final Topography topography = Domain.getWorld(Domain.individuals.get(hostId).getWorldId()).getTopography();

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
			return topography.getTile(tileCoordinate, true) instanceof EmptyTile;
		}


		@Override
		public void uponCompletion() {
		}


		@Override
		public void execute() {
			final Individual host = Domain.individuals.get(hostId.getId());

			if (host.getInteractionBox().isWithinBox(tileCoordinate)) {
				Topography.addTask(
					new Task() {
						@Override
						public void execute() {
							Tile tileToBeDeleted = topography.getTile(tileCoordinate.x, tileCoordinate.y, true);

							if (!ClientServerInterface.isServer()) {
								ClientServerInterface.SendRequest.sendDestroyTileRequest(tileCoordinate.x, tileCoordinate.y, true, host.getWorldId());
							}

							if (tileToBeDeleted != null && !(tileToBeDeleted instanceof EmptyTile)) {
								if (ClientServerInterface.isClient()) {
									SoundService.pickAxe.play(
										SoundService.getVolumne(tileCoordinate),
										1f,
										SoundService.getPan(tileCoordinate)
									);
								}

								if (ClientServerInterface.isServer() && ClientServerInterface.isClient()) {
									topography.deleteTile(tileCoordinate.x, tileCoordinate.y, true);
									host.giveItem(tileToBeDeleted.mine());

									InventoryWindow existingInventoryWindow = (InventoryWindow) Iterables.find(UserInterface.layeredComponents, new Predicate<Component>() {
										@Override
										public boolean apply(Component input) {
											if (input instanceof Window) {
												return ((Window) input).title.equals(hostId.getSimpleName() + " - Inventory");
											}
											return false;
										}
									}, null);

									if (existingInventoryWindow != null) {
										existingInventoryWindow.refresh();
									}
								} else if (ClientServerInterface.isServer()) {
									topography.deleteTile(tileCoordinate.x, tileCoordinate.y, true);
									ClientServerInterface.SendNotification.notifyTileMined(-1, tileCoordinate, true, host.getWorldId());
									ClientServerInterface.SendNotification.notifyGiveItem(host.getId().getId(), tileToBeDeleted.mine());
								}
							}
						}
					}
				);
			}
		}
	}
}