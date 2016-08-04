package bloodandmithril.world.fluids;

import java.util.Collection;

import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile;

/**
 * Updates fluids
 * 
 * @author Sam
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class FluidUpdater {
	private static final float PARTICLE_VOLUME = 0.1f;
	private static final float SPEW_RADIUS = 3f;
	
	@Inject private FluidStripPopulator fluidStripPopulator;
	
	/**
	 * Updates a {@link FluidStrip}
	 */
	public void updateStrip(World world, FluidStrip strip, float delta) {
		transferFromStripToStripBelow(world, strip, delta);
		
		if(strip.getVolume() > 0) {
			spewFromBottomOfStrip(world, strip);
			spewFromLeftOfStrip(world, strip);
			spreFromRightOfStrip(world, strip);
		}
		
		transferFromStripToStripAbove(world, strip);
	}
	
	
	/**
	 * Updates a {@link FluidParticle}
	 */
	public void updateParticle(World world, FluidParticle particle, float delta) {
		try {
			Vector2 previousPosition = particle.position.cpy();
			Vector2 previousVelocity = particle.velocity.cpy();

			particle.position.add(particle.velocity.cpy().scl(0.016f));
			
			particle.position.add(particle.velocity.cpy().scl(delta));
			float gravity = Domain.getWorld(particle.worldId).getGravity();
			if (particle.velocity.len() > 2000f) {
				particle.velocity.add(0f, -gravity * delta).scl(0.95f);
			} else {
				particle.velocity.add(0f, -gravity * delta);
			}

			Tile tile = Domain.getWorld(particle.worldId).getTopography().getTile(particle.position.x, particle.position.y - 1, true);
			if (!tile.isPassable()) {
				particle.velocity.y = 0f;
			}

			Tile tileUnder = Domain.getWorld(particle.worldId).getTopography().getTile(particle.position.x, particle.position.y, true);
			if (!tileUnder.isPassable()) {
				Vector2 trial = particle.position.cpy();
				trial.y += -previousVelocity.y*delta;

				if (Domain.getWorld(particle.worldId).getTopography().getTile(trial.x, trial.y, true).isPassable()) {
					particle.position.x = previousPosition.x;
					particle.position.y = previousPosition.y;
					particle.velocity.y = -previousVelocity.y * 0.25f;
				} else {
					particle.velocity.x = -particle.velocity.x;
					particle.position.x = previousPosition.x;
					particle.position.y = previousPosition.y;
				}
			}
			Optional<FluidStrip> stripOn = world.fluids().getFluid(Topography.convertToWorldTileCoord(particle.position.x), Topography.convertToWorldTileCoord(particle.position.y));
			if(stripOn.isPresent()) {
				stripOn.get().addVolume(PARTICLE_VOLUME);
				world.fluids().removeFluidParticle(particle.id);
			}
		} catch (NoTileFoundException e) {}
	}

	
	private void transferFromStripToStripAbove(World world, FluidStrip strip) {
		if (strip.getVolume() > strip.width) {
			Collection<Integer> stripsAbove = Sets.newConcurrentHashSet();
			for (int x = strip.worldTileX; x < strip.worldTileX + strip.width; x++) {
				Optional<FluidStrip> tempStrip = world.fluids().getFluid(x, strip.worldTileY + 1);
				//if there's a strip there already, use it
				if (tempStrip.isPresent()) {
					if (!stripsAbove.contains(tempStrip.get().id)) {
						stripsAbove.add(tempStrip.get().id);
						x += tempStrip.get().width;
					}
				//if not, try to make one
				} else {
					Optional<FluidStrip> addedStrip = fluidStripPopulator.createFluidStrip(world, x, strip.worldTileY + 1, 0);
					if(addedStrip.isPresent()) {
						System.out.println("wat?");
						stripsAbove.add(addedStrip.get().id);
						x += addedStrip.get().width;
					}
				}
			}
			
			float extraFluid = strip.getVolume() - strip.width;
			if(!stripsAbove.isEmpty()) {
				for (Integer key : stripsAbove) {
					FluidStrip tempStrip = world.fluids().getFluidStrip(key);
					strip.addVolume(-tempStrip.addVolume(extraFluid/stripsAbove.size()));
				}
			}
		}
	}

	
	private void spreFromRightOfStrip(World world, FluidStrip strip) {
		try {
			if(world.getTopography().getTile(strip.worldTileX + strip.width + 1, strip.worldTileY, true).isPassable()) {
				final Vector2 rotate = new Vector2(Util.getRandom().nextFloat() * 200f, 0f).rotate(Util.getRandom().nextFloat() * 360f).add(200f,0f);
				FluidParticle particle = new FluidParticle(
					new Vector2(Topography.convertToWorldCoord(strip.worldTileX + strip.width, true) + 1f, Topography.convertToWorldCoord(strip.worldTileY, true) + 0.5f),
					rotate,
					SPEW_RADIUS,
					world.getWorldId()
				);
				world.fluids().addFluidParticle(particle);
				strip.addVolume(-PARTICLE_VOLUME);
			}
		} catch (NoTileFoundException e) {}
	}

	
	private void spewFromLeftOfStrip(World world, FluidStrip strip) {
		try {
			if(world.getTopography().getTile(strip.worldTileX - 1, strip.worldTileY, true).isPassable()) {
				final Vector2 rotate = new Vector2(Util.getRandom().nextFloat() * 200f, 0f).rotate(Util.getRandom().nextFloat() * 360f).add(-200f,0f);
				FluidParticle particle = new FluidParticle(
					new Vector2(Topography.convertToWorldCoord(strip.worldTileX, true) - 1f, Topography.convertToWorldCoord(strip.worldTileY, true) + 0.5f),
					rotate,
					SPEW_RADIUS,
					world.getWorldId()
				);
				world.fluids().addFluidParticle(particle);
				strip.addVolume(-PARTICLE_VOLUME);
			}
		} catch (NoTileFoundException e) {}
	}

	
	private void spewFromBottomOfStrip(World world, FluidStrip strip) {
		for (int x = strip.worldTileX; x < strip.worldTileX + strip.width; x++) { 
			Optional<FluidStrip> tempStrip = world.fluids().getFluid(x, strip.worldTileY - 1);
			if (tempStrip.isPresent()) {
				if(tempStrip.get().getVolume() < tempStrip.get().width) {
					
					//particles for each strip tile
					for(int i = tempStrip.get().worldTileX; i < tempStrip.get().worldTileX + tempStrip.get().width; i++) {
						final Vector2 rotate = new Vector2(Util.getRandom().nextFloat() * 200f, 0f).rotate(Util.getRandom().nextFloat() * 360f).add(0f,-200f);
						FluidParticle particle = new FluidParticle(
							new Vector2(i, strip.worldTileY - 1),
							rotate,
							SPEW_RADIUS,
							world.getWorldId()
						);
						world.fluids().addFluidParticle(particle);
						strip.addVolume(-PARTICLE_VOLUME);
					}
				}
				
				x += tempStrip.get().width - 1;
			} else {
				try {
					if(world.getTopography().getTile(x, strip.worldTileY - 1, true).isPassable()) {
						//particles below this tile
						final Vector2 rotate = new Vector2(Util.getRandom().nextFloat() * 200f, 0f).rotate(Util.getRandom().nextFloat() * 360f).add(0f,-200f);
						FluidParticle particle = new FluidParticle(
							new Vector2(Topography.convertToWorldCoord(x, true)+0.5f, Topography.convertToWorldCoord(strip.worldTileY, true)-1),
							rotate,
							SPEW_RADIUS,
							world.getWorldId()
						);
						world.fluids().addFluidParticle(particle);
						strip.addVolume(-PARTICLE_VOLUME);
					}
				} catch (NoTileFoundException e) {}
			}
		}
	}

	
	private void transferFromStripToStripBelow(World world, FluidStrip strip, float delta) {
		Collection<Integer> stripsBelow = Sets.newConcurrentHashSet();
		for (int x = strip.worldTileX; x < strip.worldTileX + strip.width; x++) { 
			Optional<FluidStrip> tempStrip = world.fluids().getFluid(x, strip.worldTileY-1);
			if (
				tempStrip.isPresent() && 
				!stripsBelow.contains(tempStrip.get().id) && 
				tempStrip.get().getVolume() < tempStrip.get().width
			) {
				stripsBelow.add(tempStrip.get().id);
				x += tempStrip.get().width - 1;
			}
		}
		
		if (!stripsBelow.isEmpty()) {
			for (Integer key : stripsBelow) {
				FluidStrip tempStrip = world.fluids().getFluidStrip(key);
				float transferVolume = Math.min(tempStrip.width - tempStrip.getVolume(), tempStrip.width*delta);
				tempStrip.addVolume(-strip.addVolume(-transferVolume));
			}
		}
	}
}