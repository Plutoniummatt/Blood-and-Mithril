package bloodandmithril.world;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.individuals.IndividualUpdateService;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.ItemUpdateService;
import bloodandmithril.item.ProjectileUpdateService;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.PropUpdater;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * Service that updates {@link World}s
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class WorldUpdateService {

	private static final float UPDATE_TICK = 1/60f;

	@Inject	private ItemUpdateService itemUpdateService;
	@Inject	private IndividualUpdateService individualUpdateService;
	@Inject	private ProjectileUpdateService projectileUpdateService;
	@Inject	private PropUpdater propUpdater;

	/**
	 * Updates a {@link World}
	 */
	public void update(final World world) {
		world.getEpoch().incrementTime(UPDATE_TICK);

		for (int i = 12; i > 0; i--) {
			for (final int individualId : world.getIndividuals()) {
				individualUpdateService.update(Domain.getIndividual(individualId), UPDATE_TICK / 12f);
			}
		}

		for (final Prop prop : world.props().getProps()) {
			propUpdater.update(prop, UPDATE_TICK);
		}

		for (final Projectile projectile : world.projectiles().getProjectiles()) {
			projectileUpdateService.update(projectile, UPDATE_TICK);
		}

		for (final Item item : world.items().getItems()) {
			try {
				itemUpdateService.update(item, UPDATE_TICK);
			} catch (final NoTileFoundException e) {}
		}
		
		world.getWeatherState().setWind(0.2f * (float) Math.sin(world.getEpoch().getTime() * 8f * Math.PI));
	}
}