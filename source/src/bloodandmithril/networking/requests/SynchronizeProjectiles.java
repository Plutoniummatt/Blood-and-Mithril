package bloodandmithril.networking.requests;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;
import bloodandmithril.networking.Response;
import bloodandmithril.world.Domain;
import bloodandmithril.world.WorldProjectiles;

/**
 * Synchronizes {@link Projectile}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class SynchronizeProjectiles implements Response {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7646256470317154496L;
	private final WorldProjectiles projectiles;
	private final int worldId;

	/**
	 * Constructor
	 */
	public SynchronizeProjectiles(int worldId) {
		this.worldId = worldId;
		this.projectiles = Domain.getWorld(worldId).projectiles();
	}
	
	
	@Override
	public void acknowledge() {
		Domain.getWorld(worldId).projectiles().getProjectilesMap().clear();
		Domain.getWorld(worldId).projectiles().getProjectilesMap().putAll(projectiles.getProjectilesMap());
	}


	@Override
	public int forClient() {
		return -1;
	}


	@Override
	public void prepare() {
	}
}