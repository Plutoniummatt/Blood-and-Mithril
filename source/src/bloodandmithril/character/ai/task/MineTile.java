package bloodandmithril.character.ai.task;

import static bloodandmithril.character.ai.task.GoToLocation.goToWithTerminationFunction;
import static bloodandmithril.character.combat.CombatService.getAttackPeriod;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_ONE_HANDED_WEAPON_MINE;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_ONE_HANDED_WEAPON_MINE;
import static bloodandmithril.util.ComparisonUtil.obj;

import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.pathfinding.PathFinder;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.Individual.Action;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;

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


	public class AttemptMine extends AITask {
		private static final long serialVersionUID = 2594481517015647682L;

		/**
		 * Constructor
		 */
		protected AttemptMine(final IndividualIdentifier hostId) {
			super(hostId);
		}


		@Override
		public String getShortDescription() {
			return "Mining";
		}


		@Override
		public boolean isComplete() {
			try {
				return
					!Domain.getIndividual(hostId.getId()).getInteractionBox().isWithinBox(tileCoordinate) ||
					Domain.getWorld(Domain.getIndividual(hostId.getId()).getWorldId()).getTopography().getTile(tileCoordinate, true) instanceof EmptyTile;
			} catch (final NoTileFoundException e) {
				return true;
			}
		}


		@Override
		public boolean uponCompletion() {
			return false;
		}


		@Override
		protected void internalExecute(final float delta) {
			final Individual host = Domain.getIndividual(hostId.getId());

			if (obj(host.getCurrentAction()).oneOf(ATTACK_LEFT_ONE_HANDED_WEAPON_MINE, ATTACK_RIGHT_ONE_HANDED_WEAPON_MINE)) {
				return;
			}

			if (host.getAttackTimer() < getAttackPeriod(host)) {
				return;
			}

			if (!new WithinInteractionBox().call()) {
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
}