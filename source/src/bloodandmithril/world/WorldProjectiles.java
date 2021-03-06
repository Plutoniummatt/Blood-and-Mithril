package bloodandmithril.world;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import com.google.inject.Inject;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;
import bloodandmithril.persistence.ParameterPersistenceService;

/**
 * {@link Projectile}s of a specific {@link World}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public final class WorldProjectiles implements Serializable {
	private static final long serialVersionUID = 6667309466390992787L;

	/** Every {@link Projectile} that exists */
	private final ConcurrentHashMap<Integer, Projectile> projectiles = new ConcurrentHashMap<>();
	private final int worldId;

	@Inject private transient ParameterPersistenceService parameterPersistenceService;

	/**
	 * Constructor
	 */
	public WorldProjectiles(int worldId) {
		this.worldId = worldId;
		Wiring.injector().injectMembers(this);
	}


	public final int addProjectile(Projectile projectile) {
		projectile.setWorldId(worldId);
		projectile.setId(parameterPersistenceService.getParameters().getNextProjectileId());
		projectiles.put(projectile.getId(), projectile);

		return projectile.getId();
	}


	public final Collection<Projectile> getProjectiles() {
		return projectiles.values();
	}


	public final ConcurrentHashMap<Integer, Projectile> getProjectilesMap() {
		return projectiles;
	}


	public final void removeProjectile(Integer id) {
		Projectile projectile = projectiles.get(id);
		Domain.getWorld(worldId).getPositionalIndexMap().get(projectile.getPosition().x, projectile.getPosition().y).removeItem(id);
		projectiles.remove(id);
	}
}