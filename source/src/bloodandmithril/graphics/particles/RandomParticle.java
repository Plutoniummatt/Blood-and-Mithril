package bloodandmithril.graphics.particles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * A {@link TracerParticle} that zigzags
 * 
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class RandomParticle extends DiminishingTracerParticle {
	private static final long serialVersionUID = -4102202774486399870L;
	
	private final SerializableFunction<Vector2> velocityChange;
	private final SerializableFunction<Boolean> velocityChangeFunction;
	
	public RandomParticle(
		Vector2 position, 
		Vector2 velocity, 
		Color color, 
		Color glowColor, 
		float radius, 
		int worldId,
		float glowIntensity, 
		MovementMode movementMode, 
		Depth depth, 
		long diminishingDuration,
		SerializableFunction<Vector2> velocityChange,
		SerializableFunction<Boolean> velocityChangeFunction
	) {
		super(position, velocity, color, glowColor, radius, worldId, glowIntensity, movementMode, depth, diminishingDuration);
		this.velocityChange = velocityChange;
		this.velocityChangeFunction = velocityChangeFunction;
	}


	@Override
	public synchronized void update(float delta) throws NoTileFoundException {
		if (velocityChangeFunction.call()) {
			Vector2 newVelocity = velocityChange.call();
			this.velocity.x = newVelocity.x;
			this.velocity.y = newVelocity.y;
		}

		super.update(delta);
	}
}
