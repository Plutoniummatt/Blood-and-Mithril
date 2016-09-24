package bloodandmithril.character.conditions;

import java.util.Collection;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.character.IndividualStateService;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.graphics.particles.ParticleService;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;

/**
 * Drains health, sets others on fire too
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@Name(name = "Burning")
public class Burning extends Condition {
	private static final long serialVersionUID = 8852771123010753122L;
	private float duration;

	/**
	 * Constructor
	 */
	public Burning(final float duration) {
		this.duration = duration;
	}


	@Override
	public void affect(final Individual affected, final float delta) {
		Wiring.injector().getInstance(IndividualStateService.class).damage(affected, delta * 2f);
		duration -= delta;

		final Collection<Integer> nearbyIndividualIds = Domain.getWorld(affected.getWorldId()).getPositionalIndexChunkMap().getNearbyEntityIds(Individual.class, affected.getState().position);
		for (final int id : nearbyIndividualIds) {
			final Individual toInfect = Domain.getIndividual(id);
			if (id != affected.getId().getId() && toInfect.getHitBox().overlapsWith(affected.getHitBox())) {
				infect(toInfect, delta);
			}
		}
	}


	@Override
	public void clientSideEffects(final Individual affected, final float delta) {
		if (Util.roll(0.2f)) {
			ParticleService.randomVelocityDiminishing(affected.getEmissionPosition(), 13f, 30f, Color.ORANGE, Color.ORANGE, Util.getRandom().nextFloat() * 6f, 10f, MovementMode.EMBER, Util.getRandom().nextInt(1000), Depth.FOREGROUND, false, Color.RED);
			ParticleService.randomVelocityDiminishing(affected.getEmissionPosition(), 13f, 30f, Color.ORANGE, Color.ORANGE, Util.getRandom().nextFloat() * 3f, 4f, MovementMode.EMBER, Util.getRandom().nextInt(1400), Depth.FOREGROUND, false, Color.RED);
			ParticleService.randomVelocityDiminishing(affected.getEmissionPosition(), 13f, 30f, Colors.LIGHT_SMOKE, Colors.LIGHT_SMOKE, 16f, 0f, MovementMode.EMBER, Util.getRandom().nextInt(3000), Depth.BACKGROUND, false, null);
		}
	}


	@Override
	public void infect(final Individual infected, final float delta) {
		Wiring.injector().getInstance(IndividualStateService.class).addCondition(infected, this);
	}


	@Override
	public boolean isExpired() {
		return duration <= 0f;
	}


	@Override
	public void uponExpiry() {
	}


	@Override
	public void stack(final Condition condition) {
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