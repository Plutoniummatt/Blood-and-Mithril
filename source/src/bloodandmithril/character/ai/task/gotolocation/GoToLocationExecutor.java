package bloodandmithril.character.ai.task.gotolocation;

import static java.lang.Math.abs;

import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.AITaskExecutor;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.control.Controls;
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
	
	@Inject private Controls controls;
	
	@Override
	public void execute(AITask aiTask, float delta) {
		GoToLocation task = (GoToLocation) aiTask;
		
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
	public boolean isComplete(AITask aiTask) {
		GoToLocation task = (GoToLocation) aiTask;
		
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
	public boolean uponCompletion(AITask aiTask) {
		GoToLocation task = (GoToLocation) aiTask;
		final Individual host = Domain.getIndividual(task.getHostId().getId());

		host.sendCommand(controls.moveRight.keyCode, false);
		host.sendCommand(controls.moveLeft.keyCode, false);
		host.sendCommand(controls.walk.keyCode, host.isWalking());

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
			host.sendCommand(controls.walk.keyCode, host.isWalking() || host.getState().stamina == 0f);
			host.setWalking(host.isWalking() || host.getState().stamina == 0f);
			
			
			
			// Only change direction if we're not mid-air
			boolean onGround = false;
			try {
				onGround = !(Domain.getWorld(host.getWorldId()).getTopography().getTile(host.getState().position.x, host.getState().position.y - 1, true) instanceof EmptyTile);
			} catch (NoTileFoundException e) {
			}
			
			if (onGround) {
				if (!host.isCommandActive(controls.moveRight.keyCode) && wayPoint.waypoint.x > host.getState().position.x) {
					host.sendCommand(controls.moveRight.keyCode, true);
					host.sendCommand(controls.moveLeft.keyCode, false);
					task.stuckCounter++;
				} else if (!host.isCommandActive(controls.moveLeft.keyCode) && wayPoint.waypoint.x < host.getState().position.x) {
					host.sendCommand(controls.moveRight.keyCode, false);
					host.sendCommand(controls.moveLeft.keyCode, true);
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