package bloodandmithril.character.conditions;

import static bloodandmithril.util.Util.getRandom;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.particles.TracerParticle;
import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.util.Countdown;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/**
 * Drains health
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Bleeding extends Condition {
	private static final long serialVersionUID = 2191121600917403074L;

	/** The health/second being drained */
	private float severity;

	/** Constructor */
	public Bleeding(float severity) {
		this.severity = severity;
	}


	@Override
	public void clientSideEffects(Individual affected, float delta) {
		if (Util.roll(severity)) {
			Domain.getWorld(affected.getWorldId()).getParticles().add(
				new TracerParticle(
					affected.getEmissionPosition().add((getRandom().nextFloat() - 1f) * affected.getWidth() / 4, (getRandom().nextFloat() - 1f) * affected.getWidth() / 4),
					new Vector2(Util.getRandom().nextFloat() * 50f, 0f).rotate(Util.getRandom().nextFloat() * 360f),
					Color.RED,
					2f,
					affected.getWorldId(),
					new Countdown(Util.getRandom().nextInt(1000)),
					0f,
					MovementMode.GRAVITY
				)
			);
		}
	}


	@Override
	public void affect(Individual affected, float delta) {
		affected.damage(delta * severity);
	}


	@Override
	public void infect(Individual infected, float delta) {
		// Not infectious
	}


	@Override
	public boolean isExpired() {
		return severity <= 0f;
	}


	@Override
	public void uponExpiry() {
	}


	@Override
	public boolean isNegative() {
		return true;
	}


	@Override
	public String getHelpText() {
		return "Bleeding, technically known as hemorrhaging, is the loss of blood escaping from the circulatory system.  Vital signs become weaker over time if bleeding is not stopped.";
	}


	@Override
	public String getName() {
		String severity;

		int sev = Math.round(this.severity * 100)/10;
		switch (sev) {
			case 0:		severity = "Slight"; break;
			case 1:		severity = "Slight"; break;
			case 2:		severity = "Mild"; break;
			case 3:		severity = "Mild"; break;
			case 4:		severity = "Moderate"; break;
			case 5:		severity = "Moderate"; break;
			case 6:		severity = "Badly"; break;
			case 7:		severity = "Badly"; break;
			case 8:		severity = "Heavy"; break;
			case 9:		severity = "Heavy"; break;
			case 10:	severity = "Extreme"; break;
			default: 	severity = "Extreme"; break;
		}

		return severity + " bleeding";
	}


	@Override
	public void stack(Condition condition) {
		if (!(condition instanceof Bleeding)) {
			throw new RuntimeException("Cannot stack " + condition.getClass().getSimpleName() + " with Bleeding");
		}

		this.severity = this.severity + ((Bleeding) condition).severity;
	}
}