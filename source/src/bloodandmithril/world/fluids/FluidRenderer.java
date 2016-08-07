package bloodandmithril.world.fluids;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.WorldFluids;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * @author Sam
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class FluidRenderer {
	
	@Inject private Graphics graphics;

	/**
	 * Renders the fluids
	 */
	public void render(final ShapeRenderer shapeRenderer, WorldFluids fluids) {
		World world = Domain.getWorld(fluids.worldId);
		
		shapeRenderer.setColor(0.1f, 0.3f, 0.5f, 0.8f);
		world.getPositionalIndexMap().getOnScreenEntities(FluidStrip.class, graphics)
		.stream()
		.distinct()
		.map(id -> world.fluids().getFluidStrip(id))
		.forEach(strip -> {
			if(strip.isPresent()) {
				try {
					shapeRenderer.rect(
						(world.getTopography().getTile(strip.get().worldTileX - 1, strip.get().worldTileY, true).isPassable() ? strip.get().worldTileX : (strip.get().worldTileX - 1)) * TILE_SIZE,
						strip.get().worldTileY * TILE_SIZE,
						((world.getTopography().getTile(strip.get().worldTileX + strip.get().width, strip.get().worldTileY, true).isPassable() ? (strip.get().width) : (strip.get().width + 1)) +
						(world.getTopography().getTile(strip.get().worldTileX - 1, strip.get().worldTileY, true).isPassable() ? 0 : 1))
						* TILE_SIZE,
						Math.min(strip.get().getVolume() / strip.get().width, 1f) * TILE_SIZE 
					);
				} catch (NoTileFoundException e) {}
			}
		});
		
		world.fluids().getAllFluidParticles().stream().forEach(particle -> {
			shapeRenderer.circle(
				particle.position.x, 
				particle.position.y, 
				particle.getRadius()
			);
		});
	}
}