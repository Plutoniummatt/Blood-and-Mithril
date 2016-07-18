package bloodandmithril.world.topography;

import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.ai.perception.Visible;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.particles.ParticleService;
import bloodandmithril.item.items.Item;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;

/**
 * Service that handles mining tiles
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class MineTileService {

	@Inject	private transient TopographyTaskExecutor topographyTaskExecutor;
	@Inject	private transient UserInterface userInterface;

	/**
	 * Mines the tile
	 */
	public void mine(final Individual miner, final Vector2 tileCoordinate) {
		final Topography topography = Domain.getWorld(miner.getWorldId()).getTopography();

		if (miner.getInteractionBox().isWithinBox(tileCoordinate)) {
			topographyTaskExecutor.addTask(() ->
				{
					Tile tileToBeDeleted;
					try {
						tileToBeDeleted = topography.getTile(tileCoordinate.x, tileCoordinate.y, true);
					} catch (final NoTileFoundException e) {
						return;
					}

					if (!ClientServerInterface.isServer()) {
						ClientServerInterface.SendRequest.sendDestroyTileRequest(tileCoordinate.x, tileCoordinate.y, true, miner.getWorldId());
					}

					if (tileToBeDeleted != null && !(tileToBeDeleted instanceof EmptyTile)) {
						SoundService.play(
							SoundService.pickAxe,
							tileCoordinate,
							true,
							Visible.getVisible(miner)
						);

						final Item mined = tileToBeDeleted.mine();
						ParticleService.mineExplosion(tileCoordinate, tileToBeDeleted.getMineExplosionColor());
						if (ClientServerInterface.isServer() && ClientServerInterface.isClient()) {
							if (topography.deleteTile(tileCoordinate.x, tileCoordinate.y, true, false) != null) {
								if (miner.canReceive(mined)) {
									miner.giveItem(mined);
								} else {
									Domain.getWorld(miner.getWorldId()).items().addItem(mined, tileCoordinate.cpy(), new Vector2());
								}

								userInterface.refreshRefreshableWindows();
							}
						} else if (ClientServerInterface.isServer()) {
							if (topography.deleteTile(tileCoordinate.x, tileCoordinate.y, true, false) != null) {
								ClientServerInterface.SendNotification.notifyTileMined(-1, tileCoordinate, true, miner.getWorldId());

								if (miner.canReceive(mined)) {
									ClientServerInterface.SendNotification.notifyGiveItem(miner.getId().getId(), tileToBeDeleted.mine(), tileCoordinate.cpy());
								} else {
									Domain.getWorld(miner.getWorldId()).items().addItem(mined, tileCoordinate.cpy(), new Vector2());
									ClientServerInterface.SendNotification.notifySyncItems(miner.getWorldId());
								}
							}
						}
					}
				}
			);
		}
	}
}