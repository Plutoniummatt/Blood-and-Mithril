package bloodandmithril.graphics.particles;

import static bloodandmithril.core.BloodAndMithrilClient.getGraphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import bloodandmithril.core.Copyright;
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
			texture =  new TextureRegion(WorldRenderer.gameWorldTexture, 453, 276, 64, 64);
		}
	}


	/**
	 * Constructor
	 */
	public TextureBackedParticle(Vector2 position, Vector2 velocity, Color color, float radius, int worldId, SerializableFunction<Boolean> removalCondition, MovementMode movementMode, Depth depth, float scale) {
		super(position, velocity, color, radius, worldId, removalCondition, movementMode, depth);
		this.scale = scale;
	}


	@Override
	public void render(float delta, TextureRegion texture) {
		Topography topography = Domain.getWorld(worldId).getTopography();
		if (topography.hasTile(position.x, position.y, true)) {
			try {
				if (topography.getTile(position.x, position.y, true).isPassable()) {
					WorldRenderer.shapeRenderer.setColor(color.getColor());
					WorldRenderer.shapeRenderer.circle(position.x, position.y, radius <= 0.05f ? 0.05f : radius);
				}
			} catch (NoTileFoundException e) {}
		}

		radius -= 0.02f;
	}


	public void renderLighting() {
		getGraphics().getSpriteBatch().draw(
			texture,
			position.x - getGraphics().getCam().position.x + getGraphics().getWidth() / 2 - texture.getRegionWidth() / 2,
			- position.y + getGraphics().getCam().position.y + getGraphics().getHeight() / 2 - texture.getRegionHeight() / 2,
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
	public void renderLine(float delta) {
	}
}