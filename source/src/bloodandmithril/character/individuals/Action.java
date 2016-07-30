package bloodandmithril.character.individuals;

import java.io.Serializable;

import bloodandmithril.core.Copyright;

/**
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public enum Action implements Serializable {
	DEAD(true),
	JUMP_LEFT(true),
	JUMP_RIGHT(false),
	STAND_LEFT(true),
	STAND_RIGHT(false),
	STAND_LEFT_COMBAT_ONE_HANDED(true),
	STAND_RIGHT_COMBAT_ONE_HANDED(false),
	WALK_LEFT(true),
	WALK_RIGHT(false),
	RUN_LEFT(true),
	RUN_RIGHT(false),
	AIM_LEFT(true),
	AIM_RIGHT(false),
	ATTACK_LEFT_UNARMED(true),
	ATTACK_RIGHT_UNARMED(false),
	ATTACK_LEFT_ONE_HANDED_WEAPON(true),
	ATTACK_RIGHT_ONE_HANDED_WEAPON(false),
	ATTACK_LEFT_ONE_HANDED_WEAPON_STAB(true),
	ATTACK_RIGHT_ONE_HANDED_WEAPON_STAB(false),
	ATTACK_LEFT_TWO_HANDED_WEAPON(true),
	ATTACK_RIGHT_TWO_HANDED_WEAPON(false),
	ATTACK_LEFT_ONE_HANDED_WEAPON_MINE(true),
	ATTACK_RIGHT_ONE_HANDED_WEAPON_MINE(false),
	ATTACK_LEFT_SPEAR(true),
	ATTACK_RIGHT_SPEAR(false);

	private boolean left;

	private Action(final boolean left) {
		this.left = left;
	}

	public boolean left() {
		return left;
	}
}