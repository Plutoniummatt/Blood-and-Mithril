package bloodandmithril.item;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static java.lang.Math.abs;
import static java.lang.Math.max;

import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.performance.PositionalIndexingService;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile;

/**
 * Updates {@link Item}s
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class ItemUpdateService {

	@Inject
	private PositionalIndexingService positionalIndexingService;

	/**
	 * update the {@link Item}
	 */
	public void update(final Item item, final float delta) throws NoTileFoundException {
		if (item.getId() == null) {
			return;
		}

		if (!Domain.getWorld(item.getWorldId()).getTopography().getChunkMap().doesChunkExist(item.getPosition())) {
			return;
		}

		final Vector2 previousPosition = item.getPosition().cpy();
		final Vector2 previousVelocity = item.getVelocity().cpy();

		item.getPosition().add(item.getVelocity().cpy().scl(delta));

		final float gravity = Domain.getWorld(item.getWorldId()).getGravity();
		if (item.getVelocity().cpy().scl(delta).len() > TILE_SIZE) {
			item.getVelocity().scl(0.9f);
		}

		item.getVelocity().y = item.getVelocity().y - delta * gravity;

		final Tile tileUnder = Domain.getWorld(item.getWorldId()).getTopography().getTile(item.getPosition().x, item.getPosition().y, true);
		if (item.rotates() && tileUnder.isPassable()) {
			item.setAngle(item.getAngle() + item.getAngularVelocity());
		}

		if (tileUnder.isPlatformTile || !tileUnder.isPassable()) {
			final Vector2 trial = item.getPosition().cpy();
			trial.y += -previousVelocity.y * delta;

			if (Domain.getWorld(item.getWorldId()).getTopography().getTile(trial.x, trial.y, true).isPassable()) {
				if (previousVelocity.y <= 0f) {

					int i = (int) item.getAngle() % 360 - (int) item.getUprightAngle();
					if (i < 0) {
						i = i + 360;
					}
					final boolean pointingUp = i > 350 || i > 0 && i < 190;
					if (pointingUp && item.doesBounce()) {
						if (abs(item.getVelocity().y) > 400f) {
							item.setAngularVelocity((Util.getRandom().nextFloat() - 0.5f) * 40f);
						} else {
							item.setAngularVelocity(max(item.getAngularVelocity() * 0.6f, 5f));
						}
						item.setPosition(previousPosition);
						item.getVelocity().y = -previousVelocity.y * 0.7f;
						item.getVelocity().x = previousVelocity.x * 0.3f;
					} else {
						item.setAngularVelocity(0f);
						item.getVelocity().x = item.getVelocity().x * 0.3f;
						item.getVelocity().y = 0f;
						item.getPosition().y = Domain.getWorld(item.getWorldId()).getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(item.getPosition(), true).y;
					}
				} else {
					item.setPosition(previousPosition);
					item.getVelocity().y = -previousVelocity.y;
				}
			} else {
				item.getVelocity().x = 0f;
				item.setPosition(previousPosition);
			}
		}

		positionalIndexingService.indexItem(item);
	}
}