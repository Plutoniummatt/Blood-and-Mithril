package bloodandmithril.playerinteraction.individual.service;

import static bloodandmithril.item.items.equipment.weapon.RangedWeapon.rangeControl;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.RangedWeapon;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;
import bloodandmithril.playerinteraction.individual.api.IndividualAttackRangedService;
import bloodandmithril.ui.FloatingTextService;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.world.Domain;

/**
 * Server-side implementation of {@link IndividualAttackRangedService}
 *
 * @author mattp
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class IndividualAttackRangedServiceServerImpl implements IndividualAttackRangedService {

	@Inject private UserInterface userInterface;
	@Inject private FloatingTextService floatingTextService;

	/**
	 * @see bloodandmithril.playerinteraction.individual.api.IndividualAttackRangedService#attack(bloodandmithril.character.individuals.Individual, com.badlogic.gdx.math.Vector2)
	 */
	@Override
	public void attack(final Individual individual, final Vector2 direction) {
		final RangedWeapon rangedWeapon = (RangedWeapon) individual.getEquipped().keySet().stream().filter(item -> {return item instanceof RangedWeapon;}).findAny().get();
		if (rangedWeapon != null) {
			final Vector2 emissionPosition = individual.getEmissionPosition();
			final Vector2 firingVector = direction.cpy().sub(emissionPosition);

			boolean hasAmmo = false;
			final Item ammo = rangedWeapon.getAmmo();

			if (ammo == null) {
				floatingTextService.addFloatingTextToIndividual(individual, "No ammo selected", Color.ORANGE);
				return;
			}

			for (final Item item : Lists.newArrayList(individual.getInventory().keySet())) {
				if (ammo.sameAs(item)) {
					hasAmmo = true;
					individual.takeItem(item);
					userInterface.refreshRefreshableWindows();
				}
			}

			if (hasAmmo) {
				final Projectile fired = rangedWeapon.fire(
					emissionPosition,
					firingVector.cpy().nor().scl(
						Math.min(
							1f,
							firingVector.len() / rangeControl
						)
					)
				);

				if (fired == null) {
					floatingTextService.addFloatingTextToIndividual(individual, "No ammo selected", Color.ORANGE);
				} else {
					fired.preFireDecorate(individual);
					fired.ignoreIndividual(individual);
					Domain.getWorld(individual.getWorldId()).projectiles().addProjectile(fired);
				}

				if (individual.has(ammo) == 0) {
					rangedWeapon.setAmmo(null);
					userInterface.refreshRefreshableWindows();
				}
			} else {
				floatingTextService.addFloatingTextToIndividual(individual, "Out of ammo", Color.ORANGE);
				rangedWeapon.setAmmo(null);
				userInterface.refreshRefreshableWindows();
			}
		}
	}
}