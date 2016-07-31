package bloodandmithril.character.ai.task.gotolocation;

import static java.lang.Math.abs;

import com.badlogic.gdx.math.Vector2;
import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.AITaskExecutor;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.individuals.Action;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;

/**
 * Executes {@link GoToLocation}
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class GoToLocationExecutor implements AITaskExecutor {

	@Override
	public void execute(final AITask aiTask, final float delta) {
		final GoToLocation task = (GoToLocation) aiTask;

		if (!task.path.isEmpty()) {
			if (task.fly) {
				// TODO Flying
			} else {
				final Vector2 waypoint = task.path.getNextPoint().waypoint;
				try {
					if (Domain.getWorld(task.getHost().getWorldId()).getTopography().getTile(waypoint.x, waypoint.y - Topography.TILE_SIZE / 2, true).isPassable() &&
						Domain.getWorld(task.getHost().getWorldId()).getTopography().getTile(waypoint.x, waypoint.y - 3 * Topography.TILE_SIZE / 2, true).isPassable() &&
						!Domain.getWorld(task.getHost().getWorldId()).getTopography().getTile(waypoint.x, waypoint.y - Topography.TILE_SIZE / 2, true).isPlatformTile) {

						task.getHost().speak("Looks like I'm stuck...", 1500);
						task.path.clear();
					} else {
						final WayPoint closest = task.path.getNextPoint();
						int counter = 0;
						int toRemove = 0;
						for(final WayPoint w : task.path.getWayPoints()) {
							if (counter > 5) {
								break;
							}
							counter++;
							if (abs(w.waypoint.x - task.getHost().getState().position.x) <= Topography.TILE_SIZE/2 && task.getHost().getState().position.dst(w.waypoint) < Topography.TILE_SIZE && w.waypoint.y < task.getHost().getState().position.y) {
								task.path.getAndRemoveNextWayPoint();
								for (int i = 0; i < toRemove; i++) {
									task.path.getAndRemoveNextWayPoint();
								}
							} else {
								toRemove++;
							}
						}

						goToWayPoint(task, closest, 4);
					}
				} catch (final NoTileFoundException e) {}
			}
		}
	}


	@Override
	public boolean isComplete(final AITask aiTask) {
		final GoToLocation task = (GoToLocation) aiTask;

		if (task.function != null) {
			if (task.function.call()) {
				return true;
			}
		}

		final Individual host = Domain.getIndividual(task.getHostId().getId());

		boolean finalWayPointCheck = false;

		final WayPoint finalWayPoint = task.path.getDestinationWayPoint();

		if (finalWayPoint == null) {
			return task.path.isEmpty() || finalWayPointCheck;
		} else {
			final float distance = Math.abs(host.getState().position.cpy().sub(finalWayPoint.waypoint).len());
			finalWayPointCheck = distance < finalWayPoint.tolerance;
		}

		return task.path.isEmpty() || finalWayPointCheck;
	}


	@Override
	public boolean uponCompletion(final AITask aiTask) {
		final GoToLocation task = (GoToLocation) aiTask;
		final Individual host = Domain.getIndividual(task.getHostId().getId());
		host.setAnimationTimer(0f);
		if (host.inCombatStance()) {
			host.setCurrentAction(host.getCurrentAction().left() ? Action.STAND_LEFT_COMBAT_ONE_HANDED : Action.STAND_RIGHT_COMBAT_ONE_HANDED);
		} else {
			host.setCurrentAction(host.getCurrentAction().left() ? Action.STAND_LEFT : Action.STAND_RIGHT);
		}

		return false;
	}


	/**
	 * Goes to a {@link WayPoint} in the {@link #path}, removing it upon arrival
	 */
	private void goToWayPoint(final GoToLocation task, final WayPoint wayPoint, final int stuckTolerance) {

		final Individual host = Domain.getIndividual(task.getHostId().getId());

		// If we're outside WayPoint.tolerance, then move toward WayPoint.wayPoint
		boolean notReached;
		if (wayPoint.tolerance == 0f) {
			try {
				if (host.getState().position.y < 0f) {
					notReached = !wayPoint.waypoint.equals(Topography.convertToWorldCoord(new Vector2(host.getState().position.x, host.getState().position.y + 1), true));
				} else {
					notReached = !wayPoint.waypoint.equals(Topography.convertToWorldCoord(host.getState().position, true));
				}
			} catch (final NoTileFoundException e) {
				return;
			}
		} else {
			notReached = Math.abs(wayPoint.waypoint.cpy().sub(host.getState().position).len()) > wayPoint.tolerance;
		}


		if (notReached) {
			host.setWalking(host.isWalking() || host.getState().stamina == 0f);

			// Only change direction if we're not mid-air
			boolean onGround = false;
			try {
				onGround = !(Domain.getWorld(host.getWorldId()).getTopography().getTile(host.getState().position.x, host.getState().position.y - 1, true) instanceof EmptyTile);
			} catch (final NoTileFoundException e) {
			}

			if (onGround) {
				final Action currentAction = host.getCurrentAction();
				if (!(currentAction == Action.WALK_RIGHT || currentAction == Action.RUN_RIGHT) && wayPoint.waypoint.x > host.getState().position.x) {
					host.setCurrentAction(host.isWalking() ? Action.WALK_RIGHT : Action.RUN_RIGHT);
					host.setAnimationTimer(0f);
					task.stuckCounter++;
				} else if (!(currentAction == Action.WALK_LEFT ||  currentAction == Action.RUN_LEFT) && wayPoint.waypoint.x < host.getState().position.x) {
					host.setCurrentAction(host.isWalking() ? Action.WALK_LEFT : Action.RUN_LEFT);
					host.setAnimationTimer(0f);
					task.stuckCounter++;
				}
			}

			if (task.stuckCounter > stuckTolerance) {
				task.path.clear();
			}

		// If we've reached the waypoint, and the next waypoint in the path is non-null, then move to the next one in the path.
		} else if (task.path.getNextPoint() != null) {
			task.path.getAndRemoveNextWayPoint();
			task.stuckCounter = 0;
		}
	}
}