package bloodandmithril.graphics.particles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.graphics.WorldRenderer;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * Texture backed particle, should be efficient at rendering.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class TextureBackedParticle extends Particle {
	private static final long serialVersionUID = -138875409800054318L;

	private static TextureRegion texture;
	private float scale;

	static {
		if (ClientServerInterface.isClient()) {
			texture =  new TextureRegion(WorldRenderer.GAME_WORLD_TEXTURE, 453, 276, 64, 64);
		}
	}


	/**
	 * Constructor
	 */
	public TextureBackedParticle(final Vector2 position, final Vector2 velocity, final Color color, final float radius, final int worldId, final SerializableFunction<Boolean> removalCondition, final MovementMode movementMode, final Depth depth, final float scale) {
		super(position, velocity, color, radius, worldId, removalCondition, movementMode, depth);
		this.scale = scale;
	}


	@Override
	public void render(final float delta, final TextureRegion texture, final Graphics graphics) {
		final WorldRenderer renderer = Wiring.injector().getInstance(WorldRenderer.class);
		final Topography topography = Domain.getWorld(worldId).getTopography();
		if (topography.hasTile(position.x, position.y, true)) {
			try {
				if (topography.getTile(position.x, position.y, true).isPassable()) {
					renderer.getShapeRenderer().setColor(color.getColor());
					renderer.getShapeRenderer().circle(position.x, position.y, radius <= 0.05f ? 0.05f : radius);
				}
			} catch (final NoTileFoundException e) {}
		}

		radius -= 0.02f;
	}


	public void renderLighting(final Graphics graphics) {
		graphics.getSpriteBatch().draw(
			texture,
			position.x - graphics.getCam().position.x + graphics.getWidth() / 2 - texture.getRegionWidth() / 2,
			- position.y + graphics.getCam().position.y + graphics.getHeight() / 2 - texture.getRegionHeight() / 2,
			texture.getRegionWidth() / 2,
			texture.getRegionHeight() / 2,
			texture.getRegionWidth(),
			texture.getRegionHeight(),
			scale,
			scale,
			0f
		);
	}


	@Override
	public void renderLine(final float delta) {
	}
}