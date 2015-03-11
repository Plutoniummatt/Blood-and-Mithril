package bloodandmithril.character.ai.task;

import static bloodandmithril.character.ai.perception.Visible.getVisible;
import static bloodandmithril.character.ai.task.GoToLocation.goToWithTerminationFunction;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_ONE_HANDED_WEAPON_MINE;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_ONE_HANDED_WEAPON_MINE;
import static bloodandmithril.util.ComparisonUtil.obj;
import bloodandmithril.audio.SoundService;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.pathfinding.PathFinder;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.Individual.Action;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
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
@Copyright("Matthew Peck 2014")
public class MineTile extends CompositeAITask {
	private static final long serialVersionUID = -4098496856332182430L;

	/** Coordinate of the tile to mine */
	public final Vector2 tileCoordinate;

	/**
	 * Constructor
	 *
	 * @param coordinate - World pixel coordinate of the tile to mine, does not have to be a converted tile coordinate
	 */
	public MineTile(Individual host, Vector2 coordinate) {
		super(
			host.getId(),
			"Mining"
		);

		try {
			appendTask(
				goToWithTerminationFunction(
					host,
					host.getState().position.cpy(),
					new WayPoint(PathFinder.getGroundAboveOrBelowClosestEmptyOrPlatformSpace(coordinate, 10, Domain.getWorld(host.getWorldId())), 0),
					false,
					new WithinInteractionBox(),
					true
				)
			);
		} catch (NoTileFoundException e) {}
		appendTask(new AttemptMine(hostId));

		this.tileCoordinate = coordinate;
	}


	public class WithinInteractionBox implements SerializableFunction<Boolean> {
		private static final long serialVersionUID = -6658375092168650175L;

		@Override
		public Boolean call() {
			return Domain.getIndividual(hostId.getId()).getInteractionBox().isWithinBox(tileCoordinate);
		}
	}


	public class AttemptMine extends AITask {
		private static final long serialVersionUID = 2594481517015647682L;

		/**
		 * Constructor
		 */
		protected AttemptMine(IndividualIdentifier hostId) {
			super(hostId);
		}


		@Override
		public String getDescription() {
			return "Mining";
		}


		@Override
		public boolean isComplete() {
			try {
				return
					!Domain.getIndividual(hostId.getId()).getInteractionBox().isWithinBox(tileCoordinate) ||
					Domain.getWorld(Domain.getIndividual(hostId.getId()).getWorldId()).getTopography().getTile(tileCoordinate, true) instanceof EmptyTile;
			} catch (NoTileFoundException e) {
				return true;
			}
		}


		@Override
		public boolean uponCompletion() {
			return false;
		}


		@Override
		public void execute(float delta) {
			Individual host = Domain.getIndividual(hostId.getId());

			if (obj(host.getCurrentAction()).oneOf(ATTACK_LEFT_ONE_HANDED_WEAPON_MINE, ATTACK_RIGHT_ONE_HANDED_WEAPON_MINE)) {
				return;
			}

			if (host.getAttackTimer() < host.getAttackPeriod()) {
				return;
			}

			host.setAnimationTimer(0f);
			host.setAttackTimer(0f);
			if (tileCoordinate.x < host.getState().position.x) {
				host.setCurrentAction(Action.ATTACK_LEFT_ONE_HANDED_WEAPON_MINE);
			} else {
				host.setCurrentAction(Action.ATTACK_RIGHT_ONE_HANDED_WEAPON_MINE);
			}
		}
	}


	/**
	 * Actual mining of a tile
	 *
	 * @author Matt
	 */
	public static class Mine {

		/**
		 * Mines the tile
		 */
		public static void mine(final Individual host, Vector2 tileCoordinate) {
			final Topography topography = Domain.getWorld(host.getWorldId()).getTopography();

			if (host.getInteractionBox().isWithinBox(tileCoordinate)) {
				Topography.addTask(() ->
					{
						Tile tileToBeDeleted;
						try {
							tileToBeDeleted = topography.getTile(tileCoordinate.x, tileCoordinate.y, true);
						} catch (NoTileFoundException e) {
							return;
						}

						if (!ClientServerInterface.isServer()) {
							ClientServerInterface.SendRequest.sendDestroyTileRequest(tileCoordinate.x, tileCoordinate.y, true, host.getWorldId());
						}
						
						if (tileToBeDeleted != null && !(tileToBeDeleted instanceof EmptyTile)) {
							SoundService.play(
								SoundService.pickAxe,
								tileCoordinate,
								true,
								getVisible(host)
							);

							Item mined = tileToBeDeleted.mine();
							if (ClientServerInterface.isServer() && ClientServerInterface.isClient()) {
								topography.deleteTile(tileCoordinate.x, tileCoordinate.y, true);
								if (host.canReceive(mined)) {
									host.giveItem(mined);
								} else {
									Domain.getWorld(host.getWorldId()).items().addItem(mined, tileCoordinate.cpy(), new Vector2());
								}

								UserInterface.refreshRefreshableWindows();
							} else if (ClientServerInterface.isServer()) {
								topography.deleteTile(tileCoordinate.x, tileCoordinate.y, true);
								ClientServerInterface.SendNotification.notifyTileMined(-1, tileCoordinate, true, host.getWorldId());

								if (host.canReceive(mined)) {
									ClientServerInterface.SendNotification.notifyGiveItem(host.getId().getId(), tileToBeDeleted.mine(), tileCoordinate.cpy());
								} else {
									Domain.getWorld(host.getWorldId()).items().addItem(mined, tileCoordinate.cpy(), new Vector2());
									ClientServerInterface.SendNotification.notifySyncItems(host.getWorldId());
								}
							}
						}
					}
				);
			}
		}
	}
}