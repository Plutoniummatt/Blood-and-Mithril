package bloodandmithril.character.combat;

import static bloodandmithril.character.ai.perception.Visible.getVisible;
import bloodandmithril.audio.SoundService;
import bloodandmithril.character.individuals.Humanoid.HumanoidCombatBodyParts;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.particles.ParticleService;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.ContainerImpl;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.item.items.equipment.weapon.MeleeWeapon;
import bloodandmithril.item.items.equipment.weapon.Weapon;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.Util;
import bloodandmithril.util.datastructure.Wrapper;
import bloodandmithril.world.Domain.Depth;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * Class for combat related calculations
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@SuppressWarnings("rawtypes")
public class CombatChain {

	private Individual attacker;
	private Individual target;
	private Weapon weapon;

	public CombatChain(Individual attacker) {
		this.attacker = attacker;

		Optional<Item> weaponOptional = Iterables.tryFind(attacker.getEquipped().keySet(), equipped -> {
			return equipped instanceof Weapon;
		});

		this.weapon = weaponOptional.isPresent() ? (Weapon)weaponOptional.get() : null;
	}

	public CombatChain target(Individual target) {
		this.target = target;

		return this;
	}

	public String execute() {
		String text = "";
		float knockbackStrength = 50f;
		if (weapon != null) {
			knockbackStrength = weapon.getKnockbackStrength();
		}

		Vector2 knockbackVector = target.getState().position.cpy().sub(attacker.getState().position.cpy()).nor().scl(knockbackStrength);

		if (parry(knockbackVector)) {
			if (disarm(knockbackVector)) {
				target.addFloatingText(
					"Disarmed!",
					Color.YELLOW
				);
			} else {
				target.addFloatingText(
					"Parried!",
					Color.GREEN
				);
			}
			return text;
		}

		text = hit(knockbackVector.scl(0.1f).cpy());

		if (ClientServerInterface.isServer()) {
			target.getState().velocity.add(knockbackVector);
			return text;
		}

		return null;
	}


	private boolean disarm(Vector2 knockbackVector) {
		// Disarming
		if (weapon != null && weapon instanceof MeleeWeapon && Util.roll(((MeleeWeapon) weapon).getDisarmChance())) {
			Sets.newHashSet(target.getEquipped().keySet()).stream().forEach(item -> {
				target.unequip((Equipable) item);
				ContainerImpl.discard(target, item, 1, ()-> {
					return knockbackVector.cpy();
				});
			});

			target.addFloatingText(
				"Disarmed!",
				Color.YELLOW
			);

			return true;
		}

		return false;
	}


	private boolean parry(Vector2 knockbackVector) {
		boolean parried = Util.roll(
			target.getParryChance() * (1f - attacker.getParryChanceIgnored())
		);

		if (parried) {
			if (ClientServerInterface.isServer()) {
				int blockSound = attacker.getBlockSound();
				if (blockSound != 0) {
					SoundService.play(
						blockSound,
						target.getState().position,
						true,
						getVisible(target)
					);
				}
				ParticleService.parrySpark(target.getEmissionPosition(), knockbackVector, Depth.FOREGROUND, Color.WHITE, Color.WHITE, 100, true, 30, 200f);
			}

			return true;
		}

		return false;
	}


	@SuppressWarnings("unchecked")
	private String hit(final Vector2 disarmVector) {
		if (!ClientServerInterface.isServer()) {
			return "";
		}

		disarmVector.rotate(90f * (Util.getRandom().nextFloat() - 0.5f));
		float f = Util.getRandom().nextFloat();
		float t = 0f;
		final Wrapper<HumanoidCombatBodyParts> hit = new Wrapper(null);

		for (HumanoidCombatBodyParts p : HumanoidCombatBodyParts.values()) {
			if (f >= t && f < t + p.getProbability()) {
				hit.t = p;
			}
			t += p.getProbability();
		}

		// Disarming
		if (weapon != null && weapon instanceof MeleeWeapon && Util.roll(((MeleeWeapon) weapon).getDisarmChance() * 2f) && !target.getAvailableEquipmentSlots().get(hit.t.getLinkedEquipmentSlot()).call()) {
			Sets.newHashSet(target.getEquipped().keySet()).stream().filter(
				item -> {
					return ((Equipable) item).slot == hit.t.getLinkedEquipmentSlot();
				}
			).forEach(item -> {
				if (item instanceof Weapon) {
					target.unequip((Equipable) item);
					ContainerImpl.discard(target, item, 1, () -> {
						return disarmVector.cpy();
					});
				}
			});
		}

		float damage = 0f;
		boolean crit = false;
		if (weapon == null) {
			damage = attacker.getUnarmedMaxDamage() - Util.getRandom().nextFloat() * (attacker.getUnarmedMaxDamage() - attacker.getUnarmedMinDamage());
		} else {
			float weaponDamage = weapon.getBaseMinDamage() + (weapon.getBaseMaxDamage() - weapon.getBaseMinDamage()) * Util.getRandom().nextFloat();
			if (Util.roll(weapon.getBaseCritChance())) {
				crit = true;
				damage = weaponDamage * weapon.getCritDamageMultiplier();
			} else {
				damage = weaponDamage;
			}
			weapon.specialEffect(target);
		}

		int hitSound = attacker.getHitSound();
		if (hitSound != 0) {
			SoundService.play(
				hitSound,
				target.getState().position,
				true,
				getVisible(target)
			);
		}
		ParticleService.bloodSplat(target.getEmissionPosition(), disarmVector);

		target.damage(damage);
		return String.format("%.2f", damage) + (crit ? " (Crit!)" : "");
	}
}