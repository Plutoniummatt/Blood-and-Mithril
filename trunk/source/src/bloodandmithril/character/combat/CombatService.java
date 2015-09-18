package bloodandmithril.character.combat;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.Individual.Action;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.MeleeWeapon;
import bloodandmithril.item.items.equipment.weapon.Weapon;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

/**
 * Service for handling combat
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class CombatService {

	/**
	 * Attacks a set of other {@link Individual}s
	 */
	@SuppressWarnings("rawtypes")
	public static boolean attack(Individual attacker, Set<Integer> individuals) {
		if (attacker.getAttackTimer() < attacker.getAttackPeriod() || !attacker.isAlive()) {
			return false;
		}

		attacker.setAttackTimer(0f);
		attacker.setAnimationTimer(0f);

		Optional<Item> weapon = Iterables.tryFind(attacker.getEquipped().keySet(), equipped -> {
			return equipped instanceof Weapon;
		});

		boolean left = attacker.getCurrentAction().left();
		if (individuals.size() == 1) {
			left = Domain.getIndividual(individuals.iterator().next()).getState().position.x < attacker.getState().position.x;
		}

		if (weapon.isPresent()) {
			if (left) {
				attacker.setCurrentAction(((Weapon) weapon.get()).getAttackAction(false));
			} else {
				attacker.setCurrentAction(((Weapon) weapon.get()).getAttackAction(true));
			}
		} else {
			if (left) {
				attacker.setCurrentAction(Action.ATTACK_LEFT_UNARMED);
			} else {
				attacker.setCurrentAction(Action.ATTACK_RIGHT_UNARMED);
			}
		}

		attacker.getIndividualsToBeAttacked().clear();
		attacker.getIndividualsToBeAttacked().addAll(individuals);

		return true;
	}


	public static void attack(Individual attacker) {
		if (!attacker.isAlive()) {
			return;
		}

		if (attacker.getIndividualsToBeAttacked().isEmpty()) {
			// Attack environmental objects... maybe?...
		} else {
			for (Integer individualId : attacker.getIndividualsToBeAttacked()) {
				Box attackingBox = attacker.getInteractionBox();

				Individual toBeAttacked = Domain.getIndividual(individualId);
				if (attackingBox.overlapsWith(toBeAttacked.getHitBox())) {
					String floatingText = new CombatChain(attacker).target(toBeAttacked).execute();
					if (!StringUtils.isBlank(floatingText)) {
						toBeAttacked.addFloatingText(
							floatingText,
							Color.RED
						);
					}
				}
			}
		}
	}


	@SuppressWarnings("rawtypes")
	public static Box getAttackingHitBox(Individual attacker) {
		Box attackingBox = null;
		Optional<Item> weapon = Iterables.tryFind(attacker.getEquipped().keySet(), equipped -> {
			return equipped instanceof Weapon;
		});

		if (weapon.isPresent()) {
			if (weapon.get() instanceof MeleeWeapon) {
				attackingBox = ((MeleeWeapon) weapon.get()).getActionFrameHitBox(attacker);
			}
		}

		if (attackingBox == null) {
			attackingBox = attacker.getDefaultAttackingHitBox();
		};

		return attackingBox;
	}
}