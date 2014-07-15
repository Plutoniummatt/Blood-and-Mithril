package bloodandmithril.character.combat;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.individuals.Humanoid.HumanoidCombatBodyParts;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.graphics.TracerParticle;
import bloodandmithril.item.items.container.ContainerImpl;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.item.items.equipment.weapon.MeleeWeapon;
import bloodandmithril.item.items.equipment.weapon.Weapon;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.Countdown;
import bloodandmithril.util.Util;
import bloodandmithril.util.datastructure.Wrapper;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
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

	public String execute() {
		String text = "";
		float knockbackStrength = 50f;
		if (weapon != null) {
			knockbackStrength = weapon.getKnockbackStrength();
		}

		Vector2 knockbackVector = target.getState().position.cpy().sub(attacker.getState().position.cpy()).nor().mul(knockbackStrength);

		boolean blocked = Util.roll(
			target.getParryChance() * (1f - attacker.getParryChanceIgnored())
		);

		if (blocked) {
			blockDisarmLogic(knockbackVector);
			if (ClientServerInterface.isServer()) {
				int blockSound = attacker.getBlockSound();
				if (blockSound != 0) {
					SoundService.play(
						blockSound,
						target.getState().position,
						true
					);
				}
			}
			for (int i = 0; i < 35; i++) {
				Domain.getActiveWorld().getParticles().add(new TracerParticle(
					target.getState().position.cpy().add(0, 50f),
					new Vector2(Util.getRandom().nextFloat() * 200f, 0f).rotate(Util.getRandom().nextFloat() * 360f).add(knockbackVector).mul(2f),
					Util.getRandom().nextBoolean() ? Color.YELLOW : Color.WHITE,
					2f,
					Domain.getActiveWorld().getWorldId(),
					new Countdown(Util.getRandom().nextInt(100))
				));
			}
		} else {
			knockbackVector.mul(0.1f);
			text = hit(knockbackVector.cpy());
			if (ClientServerInterface.isServer()) {
				int hitSound = attacker.getHitSound();
				if (hitSound != 0) {
					SoundService.play(
						hitSound,
						target.getState().position,
						true
					);
				}
			}
			for (int i = 0; i < 35; i++) {
				Domain.getActiveWorld().getParticles().add(new TracerParticle(
					target.getState().position.cpy().add(0, 50f),
					new Vector2(Util.getRandom().nextFloat() * 50f, 0f).rotate(Util.getRandom().nextFloat() * 360f).add(knockbackVector).mul(5f),
					Color.RED,
					2f,
					Domain.getActiveWorld().getWorldId(),
					new Countdown(Util.getRandom().nextInt(2500))
				));
			}
		}

		if (ClientServerInterface.isServer()) {
			target.getState().velocity.add(knockbackVector);
			return text;
		}

		return null;
	}


	private void blockDisarmLogic(Vector2 disarmVector) {
		if (!ClientServerInterface.isServer()) {
			return;
		}

		// Disarming
		if (weapon != null && weapon instanceof MeleeWeapon && Util.roll(((MeleeWeapon) weapon).getDisarmChance())) {
			Sets.newHashSet(target.getEquipped().keySet()).stream().forEach(item -> {
				target.unequip((Equipable) item);
				ContainerImpl.discard(target, item, 1, disarmVector);
			});

			target.addFloatingText(
				"Disarmed!",
				Color.YELLOW
			);

			return;
		}

		target.addFloatingText(
			"Parried!",
			Color.GREEN
		);
	}


	@SuppressWarnings("unchecked")
	private String hit(Vector2 disarmVector) {
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
					ContainerImpl.discard(target, item, 1, disarmVector);
				}
			});
		}

		float damage = 0f;
		boolean crit = false;
		if (weapon == null) {
			damage = attacker.getUnarmedDamage();
		} else {
			float weaponDamage = weapon.getBaseMinDamage() + (weapon.getBaseMaxDamage() - weapon.getBaseMinDamage()) * Util.getRandom().nextFloat();
			if (Util.roll(weapon.getBaseCritChance())) {
				crit = true;
				damage = weaponDamage * weapon.getCritDamageMultiplier();
			} else {
				damage = weaponDamage;
			}
		}

		target.damage(damage);
		return String.format("%.2f", damage) + (crit ? " (Crit!)" : "");
	}
}