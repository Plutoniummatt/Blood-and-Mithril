package bloodandmithril.world.fluids;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static bloodandmithril.world.topography.Topography.convertToWorldCoord;

import java.util.ArrayList;
import java.util.Collection;

import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.util.Util;
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
	private static final float MAX_PARTICLE_VOLUME = 0.1f;
	
	@Inject private FluidStripPopulator fluidStripPopulator;
	@Inject private FluidParticlePopulator fluidParticlePopulator;
	private ArrayList<Integer> flaggedForDeletion = new ArrayList<>();
	
	/**
	 * Updates a {@link FluidStrip}
	 */
	public void updateStrip(World world, FluidStrip strip, float delta) {
		
		//Merge strips to left and right if the volume is similar
		Optional<FluidStrip> leftStrip = world.fluids().getFluidStrip(strip.worldTileX-1, strip.worldTileY);
		Optional<FluidStrip> rightStrip = world.fluids().getFluidStrip(strip.worldTileX + strip.width, strip.worldTileY);
		final float mergeTolerance = 0.1f;
		if(
			(leftStrip.isPresent() &&
			Math.abs((leftStrip.get().getVolume() / leftStrip.get().width) - (strip.getVolume() / strip.width)) < mergeTolerance) ||
			(rightStrip.isPresent() &&
			Math.abs((rightStrip.get().getVolume() / rightStrip.get().width) - (strip.getVolume() / strip.width)) < mergeTolerance) 
		) {
			world.fluids().removeFluidStrip(strip.id);
		}
		if(
			(leftStrip.isPresent() &&
			Math.abs((leftStrip.get().getVolume() / leftStrip.get().width) - (strip.getVolume() / strip.width)) < mergeTolerance)
		) {
			world.fluids().removeFluidStrip(leftStrip.get().id);
			fluidStripPopulator.createFluidStrip(world, strip.worldTileX, strip.worldTileY, leftStrip.get().getVolume() + strip.getVolume());
		}
		if(
			(rightStrip.isPresent() &&
			Math.abs((rightStrip.get().getVolume() / rightStrip.get().width) - (strip.getVolume() / strip.width)) < mergeTolerance)
		) {
			world.fluids().removeFluidStrip(rightStrip.get().id);
			fluidStripPopulator.createFluidStrip(world, strip.worldTileX, strip.worldTileY, rightStrip.get().getVolume() + strip.getVolume());
		}
		if(
			(leftStrip.isPresent() &&
			Math.abs((leftStrip.get().getVolume() / leftStrip.get().width) - (strip.getVolume() / strip.width)) < mergeTolerance) ||
			(rightStrip.isPresent() &&
			Math.abs((rightStrip.get().getVolume() / rightStrip.get().width) - (strip.getVolume() / strip.width)) < mergeTolerance) 
		) {
			return;
		}
		//Delete empty non-base strips
		try {
			if(
				strip.getVolume() == 0f
			) {
				if(
					(world.getTopography().getTile(strip.worldTileX - 1, strip.worldTileY, true).isPassable() &&
					!world.fluids().getFluidStrip(strip.worldTileX - 1, strip.worldTileY).isPresent()) ||
					(world.getTopography().getTile(strip.worldTileX + strip.width, strip.worldTileY, true).isPassable() &&
					!world.fluids().getFluidStrip(strip.worldTileX + strip.width, strip.worldTileY).isPresent())
				) {
					world.fluids().removeFluidStrip(strip.id);
					return;
				}
				for (int x = strip.worldTileX; x < strip.worldTileX + strip.width; x++) { 
					if(
						world.getTopography().getTile(x, strip.worldTileY - 1, true).isPassable() &&
						!world.fluids().getFluidStrip(strip.worldTileX - 1, strip.worldTileY).isPresent() &&
						!world.fluids().getFluidStrip(strip.worldTileX + strip.width, strip.worldTileY).isPresent()
					) {
						if(flaggedForDeletion.contains(strip.id)) { // to make sure it's updated at least once before being deleted.
							world.fluids().removeFluidStrip(strip.id);
							return;
						} else {
							flaggedForDeletion.add(strip.id);
						}
					}
				}
			}
		} catch (NoTileFoundException e) {}
		
		transferFromStripToStripsBelow(world, strip);
		
		if(strip.getVolume() > 0) {
			spewFromBottomOfStrip(world, strip);
			spewFromLeftOfStrip(world, strip);
			spewFromRightOfStrip(world, strip);
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
			float gravity = world.getGravity();
			if (particle.velocity.len() > 2000f) {
				particle.velocity.add(0f, -gravity * delta).scl(0.95f);
			} else {
				particle.velocity.add(0f, -gravity * delta);
			}

			Tile tile = world.getTopography().getTile(particle.position.x, particle.position.y - 1, true);
			if (!tile.isPassable()) {
				particle.velocity.y = 0f;
			}

			Tile tileUnder = world.getTopography().getTile(particle.position.x, particle.position.y, true);
			if (!tileUnder.isPassable()) {
				Vector2 trial = particle.position.cpy();
				trial.y += -previousVelocity.y*delta;

				if (world.getTopography().getTile(trial.x, trial.y, true).isPassable()) {
					particle.position.x = previousPosition.x;
					particle.position.y = previousPosition.y;
					particle.velocity.y = -previousVelocity.y * 0.25f;
				} else {
					particle.velocity.x = -particle.velocity.x;
					particle.position.x = previousPosition.x;
					particle.position.y = previousPosition.y;
				}
			}
			Optional<FluidStrip> stripOn = world.fluids().getFluidStrip(Topography.convertToWorldTileCoord(particle.position.x), Topography.convertToWorldTileCoord(particle.position.y));
			if(stripOn.isPresent()) {
				if((particle.position.y < convertToWorldCoord(stripOn.get().worldTileY, true) + TILE_SIZE * stripOn.get().getVolume() / stripOn.get().width) || stripOn.get().getVolume() == 0f) {
					stripOn.get().addVolume(particle.getVolume());
					world.fluids().removeFluidParticle(particle.id);
				}
			}
		} catch (NoTileFoundException e) {}
	}

	
	private void transferFromStripToStripAbove(World world, FluidStrip strip) {
		if (strip.getVolume() > strip.width) {
			Collection<Integer> stripsAbove = Sets.newConcurrentHashSet();
			for (int x = strip.worldTileX; x < strip.worldTileX + strip.width; x++) {
				Optional<FluidStrip> tempStrip = world.fluids().getFluidStrip(x, strip.worldTileY + 1);
				//if there's a strip there already, use it
				if (tempStrip.isPresent()) {
					stripsAbove.add(tempStrip.get().id);
					x = tempStrip.get().worldTileX + tempStrip.get().width;
				//if not, try to make one
				} else {
					Optional<FluidStrip> addedStrip = fluidStripPopulator.createFluidStrip(world, x, strip.worldTileY + 1, 0f);
					if(addedStrip.isPresent()) {
						stripsAbove.add(addedStrip.get().id);
						x = addedStrip.get().worldTileX + addedStrip.get().width;
					}
				}
			}
			
			float extraFluid = strip.getVolume() - strip.width;
			if(!stripsAbove.isEmpty()) {
				for (Integer key : stripsAbove) {
					FluidStrip tempStrip = world.fluids().getFluidStrip(key).get();
					strip.addVolume(-tempStrip.addVolume(extraFluid/stripsAbove.size()));
				}
			}
		}
	}

	
	private void spewFromRightOfStrip(World world, FluidStrip strip) {
		try {
			if(world.getTopography().getTile(strip.worldTileX + strip.width, strip.worldTileY, true).isPassable()) {
				final Vector2 position = new Vector2(Topography.convertToWorldCoord(strip.worldTileX + strip.width, true) + 1f, Topography.convertToWorldCoord(strip.worldTileY, true) + 8f);
				final Vector2 velocity = new Vector2(Util.getRandom().nextFloat() * 200f, 0f).rotate(Util.getRandom().nextFloat() * 360f).add(200f,0f);
				Optional<FluidStrip> rightStrip = world.fluids().getFluidStrip(strip.worldTileX + strip.width, strip.worldTileY);
				if(!rightStrip.isPresent() || strip.getVolume() / strip.width > rightStrip.get().getVolume() / rightStrip.get().width) {
					spewParticle(world, strip, position, velocity);
				}
			}
		} catch (NoTileFoundException e) {}
	}

	
	private void spewFromLeftOfStrip(World world, FluidStrip strip) {
		try {
			if(world.getTopography().getTile(strip.worldTileX - 1, strip.worldTileY, true).isPassable()) {
				final Vector2 position = new Vector2(Topography.convertToWorldCoord(strip.worldTileX, true) - 1f, Topography.convertToWorldCoord(strip.worldTileY, true) + 8f);
				final Vector2 velocity = new Vector2(Util.getRandom().nextFloat() * 200f, 0f).rotate(Util.getRandom().nextFloat() * 360f).add(-200f,0f);
				Optional<FluidStrip> leftStrip = world.fluids().getFluidStrip(strip.worldTileX - 1, strip.worldTileY);
				if(!leftStrip.isPresent() || strip.getVolume() / strip.width > leftStrip.get().getVolume() / leftStrip.get().width) {
					spewParticle(world, strip, position, velocity);
				}
			}
		} catch (NoTileFoundException e) {}
	}


	private void spewFromBottomOfStrip(World world, FluidStrip strip) {
		for (int x = strip.worldTileX; x < strip.worldTileX + strip.width; x++) { 
			Optional<FluidStrip> tempStrip = world.fluids().getFluidStrip(x, strip.worldTileY - 1);
			if (tempStrip.isPresent()) {
				if(tempStrip.get().getVolume() < tempStrip.get().width) {
					//particles for each strip tile
					for(int i = tempStrip.get().worldTileX; i < tempStrip.get().worldTileX + tempStrip.get().width; i++) {
						final Vector2 position = new Vector2(i + Topography.TILE_SIZE / 2f, strip.worldTileY - 1);
						final Vector2 velocity = new Vector2(Util.getRandom().nextFloat() * 200f, 0f).rotate(Util.getRandom().nextFloat() * 360f).add(0f,-200f);
						spewParticle(world, strip, position, velocity);
					}
				}
				x = tempStrip.get().worldTileX + tempStrip.get().width;
			} else {
				try {
					if(world.getTopography().getTile(x, strip.worldTileY - 1, true).isPassable()) {
						//particles below this tile
						final Vector2 position = new Vector2(Topography.convertToWorldCoord(x, true) + Topography.TILE_SIZE / 2f, Topography.convertToWorldCoord(strip.worldTileY, true)-1);
						final Vector2 velocity = new Vector2(Util.getRandom().nextFloat() * 200f, 0f).rotate(Util.getRandom().nextFloat() * 360f).add(0f,-200f);
						spewParticle(world, strip, position, velocity);
					}
				} catch (NoTileFoundException e) {}
			}
		}
	}

	
	private void transferFromStripToStripsBelow(World world, FluidStrip strip) {
		Collection<Integer> stripsBelow = Sets.newConcurrentHashSet();
		for (int x = strip.worldTileX; x < strip.worldTileX + strip.width; x++) { 
			Optional<FluidStrip> tempStrip = world.fluids().getFluidStrip(x, strip.worldTileY-1);
			if (
				tempStrip.isPresent() && 
				!stripsBelow.contains(tempStrip.get().id) &&
				tempStrip.get().getVolume() < tempStrip.get().width
			) {
				stripsBelow.add(tempStrip.get().id);
				x = tempStrip.get().worldTileX + tempStrip.get().width;
			}
		}
		if (!stripsBelow.isEmpty()) {
			for (Integer key : stripsBelow) {
				FluidStrip tempStrip = world.fluids().getFluidStrip(key).get();
				float transferVolume = Math.min(tempStrip.width - tempStrip.getVolume(),strip.getVolume()/stripsBelow.size());
				tempStrip.addVolume(-strip.addVolume(-transferVolume));
			}
		}
	}

	
	/**
	 * @param world
	 * @param strip
	 * @param position
	 * @param velocity
	 * 
	 * Spews some particles according to the pressure of the strip.
	 */
	private void spewParticle(World world, FluidStrip strip, final Vector2 position, final Vector2 velocity) {
		int particlesToSpew = (int)(strip.pressureCounter + getPressure(world, strip));
		strip.pressureCounter = (strip.pressureCounter + getPressure(world, strip))%1;
		for(int p = particlesToSpew; p > 0; p--) {
			fluidParticlePopulator.createFluidParticle(position, velocity, -strip.addVolume(-MAX_PARTICLE_VOLUME), world);
		}
	}
	
	
	/**
	 * @param world
	 * @param strip
	 * @return The number of particles to spew.
	 */
	private float getPressure(World world, FluidStrip strip) {
		return getDepth(world, strip) * 3; //max 3 per tile depth
	}
	
	
	/**
	 * @param world
	 * @param strip
	 * @return the depth of the strips above the given strip at the deepest level.
	 */
	private float getDepth(World world, FluidStrip strip) {
		int count = -1; // because we count the one we're on
		float extra = 0f;
		Collection<FluidStrip> currentStrips = Sets.newConcurrentHashSet();
		currentStrips.add(strip);
		while(!currentStrips.isEmpty()) {
			count++;
			Collection<FluidStrip> nextStrips = Sets.newConcurrentHashSet();
			for(FluidStrip currentStrip : currentStrips) {
				nextStrips.addAll(getStripsAbove(world, currentStrip)); // can have duplicates, this doesn't matter
			}
			if(nextStrips.isEmpty()) {
				for(FluidStrip nextStrip : currentStrips) {
					extra = Math.max(nextStrip.getVolume()/nextStrip.width, extra);
				}
			}
			currentStrips = nextStrips;
		}
		
		return count + extra;
	}


	/**
	 * @param world
	 * @param strip
	 * @return a collection of the Fluid Strips one tile above the given strip which are connected.
	 */
	private Collection<FluidStrip> getStripsAbove(World world, FluidStrip strip) {
		Collection<FluidStrip> stripsAbove = Sets.newConcurrentHashSet();
		for (int x = strip.worldTileX; x < strip.worldTileX + strip.width; x++) {
			Optional<FluidStrip> tempStrip = world.fluids().getFluidStrip(x, strip.worldTileY + 1);
			//if there's a strip there, put it in the list
			if (tempStrip.isPresent()) {
				stripsAbove.add(tempStrip.get());
				x = tempStrip.get().worldTileX + tempStrip.get().width;
			}
		}
		return stripsAbove;
	}
}