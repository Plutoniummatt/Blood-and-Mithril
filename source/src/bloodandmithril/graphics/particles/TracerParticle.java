package bloodandmithril.graphics.particles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.graphics.WorldRenderer;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.util.Performance;
import bloodandmithril.util.SerializableColor;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.util.Shaders;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * A particle that renders like a tracer, the length scales linearly with the velocity of the particle.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@Performance(explanation = "Use with care, these don't perform as well as we hope")
public class TracerParticle extends Particle {
	private static final long serialVersionUID = 4646397525209769227L;

	public float glowIntensity;
	public Vector2 prevPosition;
	public SerializableColor glowColow;

	public TracerParticle(final Vector2 position, final Vector2 velocity, final Color color, final Color glowColor, final float radius, final int worldId, final SerializableFunction<Boolean> removalCondition, final float glowIntensity, final MovementMode movementMode, final Depth depth) {
		super(position, velocity, color, radius, worldId, removalCondition, movementMode, depth);
		this.prevPosition = position.cpy();
		this.glowIntensity = glowIntensity;
		this.glowColow = new SerializableColor(glowColor);
	}


	@Override
	public synchronized void render(final float delta, final TextureRegion texture, final Graphics graphics) {
		final Topography topography = Domain.getWorld(worldId).getTopography();
		if (topography.hasTile(position.x, position.y, true)) {
			try {
				if (topography.getTile(position.x, position.y, true).isPassable()) {
					final Color c = color.getColor();
					Shaders.particleTexture.setUniformf("override", c.r, c.g, c.b, c.a);
					graphics.getSpriteBatch().draw(
						texture,
						position.x - radius,
						position.y - radius,
						radius * 2,
						radius * 2
					);
				}
			} catch (final NoTileFoundException e) {}
		}
	}


	@Override
	public synchronized void update(final float delta) throws NoTileFoundException {
		if (doNotUpdate) {
			return;
		}
		prevPosition.x = position.x;
		prevPosition.y = position.y;

		super.update(delta);
	}


	@Override
	public synchronized void renderLine(final float delta) {
		final WorldRenderer worldRenderer = Wiring.injector().getInstance(WorldRenderer.class);
		final Topography topography = Domain.getWorld(worldId).getTopography();
		if (topography.hasTile(position.x, position.y, true) && topography.hasTile(prevPosition.x, prevPosition.y, true)) {
			try {
				if (topography.getTile(position.x, position.y, true).isPassable()) {
					Gdx.gl.glLineWidth(radius == 1f ? 1f : 2 * radius);
					worldRenderer.getShapeRenderer().setColor(color.getColor());
					worldRenderer.getShapeRenderer().line(position.x, position.y, prevPosition.x, prevPosition.y);
					worldRenderer.getShapeRenderer().flush();
				}
			} catch (final NoTileFoundException e) {}
		}
	}
}