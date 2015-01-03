package bloodandmithril.item.items.equipment.weapon.ranged.projectile;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.graphics.particles.ParticleService;
import bloodandmithril.item.material.metal.Metal;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/**
 * A flaming arrow
 * 
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class FireArrow<T extends Metal> extends Arrow<T> {
	private static final long serialVersionUID = -4345906143614035570L;
	private float burnDuration;

	/**
	 * Constructor
	 */
	public FireArrow(Class<T> metal, Vector2 position, Vector2 velocity, float burnDuration) {
		super(metal, position, velocity);
		this.burnDuration = burnDuration;
	}
	
	
	@Override
	public void update(float delta) {
		if (burnDuration > 0f) {
			burnDuration -= delta;
			ParticleService.randomVelocity(position, 0f, 30f, Color.ORANGE, 10f, MovementMode.EMBER);
		}
		
		super.update(delta);
	}
}