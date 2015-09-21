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
	 * Attacks a set of other {@link Individual}s, this triggers an animation sequence and eventually leads to a {@link CombatService#strike(Individual)}
	 */
	@SuppressWarnings("rawtypes")
	public static boolean attack(Individual attacker, Set<Integer> individuals) {
		if (attacker.getAttackTimer() < getAttackPeriod(attacker) || !attacker.isAlive()) {
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


	/**
	 * Makes an individual strike
	 */
	public static void strike(Individual attacker) {
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


	/**
	 * The attacking hit box is the hit box that should be used to calculate whether an attack has made contact with the victim
	 */
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


	@SuppressWarnings("rawtypes")
	public static float getParryChance(Individual indi) {
		Optional<Item> weapon = Iterables.tryFind(indi.getEquipped().keySet(), equipped -> {
			return equipped instanceof MeleeWeapon;
		});

		if (weapon.isPresent()) {
			return ((MeleeWeapon) weapon.get()).getParryChance();
		}

		return 0f;
	}


	@SuppressWarnings("rawtypes")
	public static float getParryChanceIgnored(Individual indi) {
		Optional<Item> weapon = Iterables.tryFind(indi.getEquipped().keySet(), equipped -> {
			return equipped instanceof MeleeWeapon;
		});

		if (weapon.isPresent()) {
			return ((MeleeWeapon) weapon.get()).getParryChanceIgnored();
		}

		return 0f;
	}


	@SuppressWarnings("rawtypes")
	public static float getAttackPeriod(Individual indi) {
		float attackingPeriod = indi.getDefaultAttackPeriod();
		Optional<Item> weapon = Iterables.tryFind(indi.getEquipped().keySet(), equipped -> {
			return equipped instanceof Weapon;
		});

		if (weapon.isPresent()) {
			attackingPeriod = ((Weapon)weapon.get()).getBaseAttackPeriod();
		}

		return attackingPeriod;
	}


	@SuppressWarnings("rawtypes")
	public static int getHitSound(Individual indi) {
		java.util.Optional<Item> meleeWeapon = indi.getEquipped().keySet().stream().filter(item -> {return item instanceof MeleeWeapon;}).findFirst();

		if (meleeWeapon.isPresent()) {
			return ((MeleeWeapon) meleeWeapon.get()).getHitSound();
		}

		return 0;
	}


	@SuppressWarnings("rawtypes")
	public static int getBlockSound(Individual indi) {
		java.util.Optional<Item> meleeWeapon = indi.getEquipped().keySet().stream().filter(item -> {return item instanceof MeleeWeapon;}).findFirst();

		if (meleeWeapon.isPresent()) {
			return ((MeleeWeapon) meleeWeapon.get()).getBlockSound();
		}

		return 0;
	}
}