package bloodandmithril.character.combat;

import static java.lang.Math.max;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.item.items.equipment.weapon.Weapon;
import bloodandmithril.util.Util;

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
			target.damage(weapon.getBaseDamage());
		} else {
			target.damage(attacker.getUnarmedDamage());
		}
		
		if (Util.roll(max(0f, target.getBlockChance() - attacker.getBlockChanceIgnored()))) {
			Sound blockSound = attacker.getBlockSound();
			if (blockSound != null) {
				blockSound.play(
					SoundService.getVolumne(target.getState().position),
					1f,
					SoundService.getPan(target.getState().position)
				);
			}
		} else {
			knockbackStrength *= 0.1f;
			Sound hitSound = attacker.getHitSound();
			if (hitSound != null) {
				hitSound.play(
					SoundService.getVolumne(target.getState().position),
					1f,
					SoundService.getPan(target.getState().position)
				);
			}
		}
		Vector2 knockbackVector = target.getState().position.cpy().sub(attacker.getState().position.cpy()).nor().mul(knockbackStrength);
		target.getState().velocity.add(knockbackVector);
	}
}