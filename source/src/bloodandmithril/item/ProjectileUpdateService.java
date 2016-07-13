package bloodandmithril.item;

import static bloodandmithril.character.ai.perception.Visible.getVisible;

import java.util.Optional;

import com.badlogic.gdx.math.Vector2;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile;

/**
 * Service for updating {@link Projectile}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class ProjectileUpdateService {

	/**
	 * Updates the {@link Projectile}
	 */
	public void update(final Projectile projectile, final float delta) {
		projectile.particleEffects(delta);

		if (projectile.isStuck()) {
			return;
		}

		final float length = projectile.pPosition.dst(projectile.position);
		final Vector2 nor = projectile.pPosition.cpy().sub(projectile.position).nor();
		final Optional<Integer> findAny = Domain.getWorld(projectile.getWorldId()).getPositionalIndexMap().getNearbyEntityIds(Individual.class, projectile.position).stream().filter(individual -> {
			for (float l = 0f; l < length; l += 4f) {
				final Vector2 test = projectile.position.cpy().add(nor.cpy().scl(l));
				final Individual target = Domain.getIndividual(individual);
				if (target.getHitBox().isWithinBox(test) && target.isAlive()) {
					return true;
				}
			}
			return false;
		}).findAny();

		if (findAny.isPresent()) {
			final Individual individual = Domain.getIndividual(findAny.get());

			if (projectile.canAffect(individual)) {
				projectile.hit(individual);
				SoundService.play(projectile.getHitSound(individual), individual.getState().position, true, getVisible(individual));
				projectile.ignoreIndividual(individual);
				if (!projectile.penetrating()) {
					projectile.targetHitKinematics();
				}
			}
		}

		if (!Domain.getWorld(projectile.getWorldId()).getTopography().getChunkMap().doesChunkExist(projectile.position)) {
			return;
		}
		final Vector2 previousPosition = projectile.position.cpy();
		projectile.position.add(projectile.velocity.cpy().scl(delta));
		final float gravity = Domain.getWorld(projectile.getWorldId()).getGravity();
		if (projectile.velocity.len() > projectile.getTerminalVelocity()) {
			projectile.velocity.add(0f, -gravity * delta).scl(0.95f);
		} else {
			projectile.velocity.add(0f, -gravity * delta);
		}

		try {
			final Tile tileUnder = Domain.getWorld(projectile.getWorldId()).getTopography().getTile(projectile.position.x, projectile.position.y, true);
			if (tileUnder.isPlatformTile || !tileUnder.isPassable()) {
				projectile.collision(previousPosition);
			}
		} catch (final NoTileFoundException e) {
		}

		projectile.pPosition = previousPosition;
	}
}