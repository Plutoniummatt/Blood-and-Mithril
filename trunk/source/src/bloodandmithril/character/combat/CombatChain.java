package bloodandmithril.character.combat;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.individuals.Humanoid.HumanoidCombatBodyParts;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.item.items.container.ContainerImpl;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.item.items.equipment.weapon.MeleeWeapon;
import bloodandmithril.item.items.equipment.weapon.Weapon;
import bloodandmithril.util.Util;
import bloodandmithril.util.datastructure.Wrapper;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Sets;

/**
 * Class for combat related calculations
 *
 * @author Matt
 */
@SuppressWarnings("rawtypes")
public class CombatChain {

	private Individual attacker;
	private Individual target;
	private Weapon weapon;

	public CombatChain(Individual attacker) {
		this.attacker = attacker;
	}

	public CombatChain target(Individual target) {
		this.target = target;
		return this;
	}

	public CombatChain withWeapon(Weapon weapon) {
		this.weapon = weapon;
		return this;
	}

	public void execute() {
		float knockbackStrength = 50f;
		if (weapon != null) {
			knockbackStrength = weapon.getKnockbackStrength();
		}

		Vector2 knockbackVector = target.getState().position.cpy().sub(attacker.getState().position.cpy()).nor().mul(knockbackStrength);

		boolean blocked = Util.roll(
			target.getBlockChance() * (1f - attacker.getBlockChanceIgnored())
		);

		if (blocked) {
			disarm(knockbackVector);
			if (ClientServerInterface.isClient()) {
				Sound blockSound = attacker.getBlockSound();
				if (blockSound != null) {
					SoundService.play(
						blockSound,
						SoundService.getVolumne(target.getState().position),
						1f,
						SoundService.getPan(target.getState().position)
					);
				}
			}
		} else {
			knockbackVector.mul(0.1f);
			hit(knockbackVector.cpy());
			if (ClientServerInterface.isClient()) {
				Sound hitSound = attacker.getHitSound();
				if (hitSound != null) {
					SoundService.play(
						hitSound,
						SoundService.getVolumne(target.getState().position),
						1f,
						SoundService.getPan(target.getState().position)
					);
				}
			}
		}

		target.getState().velocity.add(knockbackVector);
	}


	private void disarm(Vector2 disarmVector) {
		if (weapon == null) {
			target.damage(attacker.getUnarmedDamage());
		} else {
			target.damage(weapon.getBaseDamage());
		}

		// Disarming
		if (weapon != null && weapon instanceof MeleeWeapon && Util.roll(((MeleeWeapon) weapon).getDisarmChance())) {
			Sets.newHashSet(target.getEquipped().keySet()).stream().forEach(item -> {
				target.unequip((Equipable) item);
				ContainerImpl.discard(target, item, 1, disarmVector);
			});
		}
	}


	@SuppressWarnings("unchecked")
	private void hit(Vector2 disarmVector) {
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

		if (weapon == null) {
			target.damage(attacker.getUnarmedDamage());
		} else {
			target.damage(weapon.getBaseDamage());
		}

		// Disarming
		if (weapon != null && weapon instanceof MeleeWeapon && Util.roll(((MeleeWeapon) weapon).getDisarmChance() * 2f) && !target.getAvailableEquipmentSlots().get(hit.t.getLinkedEquipmentSlot()).call()) {
			Sets.newHashSet(target.getEquipped().keySet()).stream().filter(
				item -> {
					return ((Equipable) item).slot == hit.t.getLinkedEquipmentSlot();
				}
			).forEach(item -> {
				target.unequip((Equipable) item);
				ContainerImpl.discard(target, item, 1, disarmVector);
			});
		}
	}
}