package bloodandmithril.character.conditions;

import java.util.Collection;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.graphics.particles.ParticleService;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;
import bloodandmithril.world.Domain.Depth;

import com.badlogic.gdx.graphics.Color;

/**
 * Drains health, sets others on fire too
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Burning extends Condition {
	private static final long serialVersionUID = 8852771123010753122L;
	private float duration;

	/**
	 * Constructor
	 */
	public Burning(float duration) {
		this.duration = duration;
	}


	@Override
	public void affect(Individual affected, float delta) {
		affected.damage(delta * 2f);
		duration -= delta;

		Collection<Integer> nearbyIndividualIds = Domain.getWorld(affected.getWorldId()).getPositionalIndexMap().getNearbyEntityIds(Individual.class, affected.getState().position);
		for (int id : nearbyIndividualIds) {
			Individual toInfect = Domain.getIndividual(id);
			if (id != affected.getId().getId() && toInfect.getHitBox().overlapsWith(affected.getHitBox())) {
				infect(toInfect, delta);
			}
		}
	}


	@Override
	public void clientSideEffects(Individual affected, float delta) {
		ParticleService.randomVelocityDiminishing(affected.getEmissionPosition(), 13f, 30f, Color.ORANGE, Util.getRandom().nextFloat() * 6f, 10f, MovementMode.EMBER, Util.getRandom().nextInt(1000), Depth.FOREGOUND);
		ParticleService.randomVelocityDiminishing(affected.getEmissionPosition(), 13f, 30f, Color.ORANGE, Util.getRandom().nextFloat() * 3f, 4f, MovementMode.EMBER, Util.getRandom().nextInt(1400), Depth.FOREGOUND);
		ParticleService.randomVelocityDiminishing(affected.getEmissionPosition(), 13f, 30f, Colors.LIGHT_SMOKE, 16f, 0f, MovementMode.EMBER, Util.getRandom().nextInt(3000), Depth.BACKGROUND);
	}


	@Override
	public void infect(Individual infected, float delta) {
		infected.addCondition(this);
	}


	@Override
	public boolean isExpired() {
		return duration <= 0f;
	}


	@Override
	public void uponExpiry() {
	}


	@Override
	public void stack(Condition condition) {
	}


	@Override
	public boolean isNegative() {
		return true;
	}


	@Override
	public String getHelpText() {
		return "You're on fire, stop reading and do something!";
	}


	@Override
	public String getName() {
		return "Burning";
	}
}