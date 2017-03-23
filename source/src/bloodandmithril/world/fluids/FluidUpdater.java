package bloodandmithril.world.fluids;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static bloodandmithril.world.topography.Topography.convertToWorldCoord;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.performance.PositionalIndexingService;
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
	@Inject private PositionalIndexingService positionalIndexingService;

	/**
	 * Updates a {@link FluidStrip}
	 */
	public void updateStrip(final World world, final FluidStrip strip, final float delta) {

		//Merge strips to left and right if the volume is similar
		final Optional<FluidStrip> leftStrip = world.fluids().getFluidStrip(strip.worldTileX-1, strip.worldTileY);
		final Optional<FluidStrip> rightStrip = world.fluids().getFluidStrip(strip.worldTileX + strip.width, strip.worldTileY);
		final float mergeTolerance = 0.1f;
		if(
			leftStrip.isPresent() &&
			Math.abs(leftStrip.get().getVolume() / leftStrip.get().width - strip.getVolume() / strip.width) < mergeTolerance ||
			rightStrip.isPresent() &&
			Math.abs(rightStrip.get().getVolume() / rightStrip.get().width - strip.getVolume() / strip.width) < mergeTolerance
		) {
			world.fluids().removeFluidStrip(strip.id);
		}
		if(
			leftStrip.isPresent() &&
			Math.abs(leftStrip.get().getVolume() / leftStrip.get().width - strip.getVolume() / strip.width) < mergeTolerance
		) {
			world.fluids().removeFluidStrip(leftStrip.get().id);
			fluidStripPopulator.createFluidStrip(world, strip.worldTileX, strip.worldTileY, leftStrip.get().getVolume() + strip.getVolume());
		}
		if(
			rightStrip.isPresent() &&
			Math.abs(rightStrip.get().getVolume() / rightStrip.get().width - strip.getVolume() / strip.width) < mergeTolerance
		) {
			world.fluids().removeFluidStrip(rightStrip.get().id);
			fluidStripPopulator.createFluidStrip(world, strip.worldTileX, strip.worldTileY, rightStrip.get().getVolume() + strip.getVolume());
		}
		if(
			leftStrip.isPresent() &&
			Math.abs(leftStrip.get().getVolume() / leftStrip.get().width - strip.getVolume() / strip.width) < mergeTolerance ||
			rightStrip.isPresent() &&
			Math.abs(rightStrip.get().getVolume() / rightStrip.get().width - strip.getVolume() / strip.width) < mergeTolerance
		) {
			return;
		}
		//Delete empty non-base strips
		try {
			if(
				strip.getVolume() == 0f
			) {
				if(
					world.getTopography().getTile(strip.worldTileX - 1, strip.worldTileY, true).isPassable() &&
					!world.fluids().getFluidStrip(strip.worldTileX - 1, strip.worldTileY).isPresent() ||
					world.getTopography().getTile(strip.worldTileX + strip.width, strip.worldTileY, true).isPassable() &&
					!world.fluids().getFluidStrip(strip.worldTileX + strip.width, strip.worldTileY).isPresent()
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
						world.fluids().removeFluidStrip(strip.id);
						return;
					}
				}
			}
		} catch (final NoTileFoundException e) {}

		transferFromStripToStripsBelow(world, strip);

		if(strip.getVolume() > 0) {
			spewFromBottomOfStrip(world, strip);
			spewFromLeftOfStrip(world, strip);
			spewFromRightOfStrip(world, strip);
		}

		transferFromStripToStripAbove(world, strip);
		equalizeLevels(world, strip);
	}
	
	
	public boolean areParticlesColliding(final FluidParticle particle1, final FluidParticle particle2) {

		float distanceToTouch = particle1.getRadius() + particle2.getRadius();
		if( //basic box check first, it's a little more performant to do this first i think
			Math.abs(particle1.getPosition().x - particle2.getPosition().x) <= distanceToTouch &&
			Math.abs(particle1.getPosition().y - particle2.getPosition().y) <= distanceToTouch
		) {
//			System.out.println("v1b4 = " + Math.round(particle1.getVelocity().x) + ", " + Math.round(particle1.getVelocity().y));
//			System.out.println("v1nxtb4 = " + Math.round(particle1.getNextVelocity().x) + ", " + Math.round(particle1.getNextVelocity().y));
			return particle1.getPosition().cpy().dst2(particle2.getPosition().cpy()) <= distanceToTouch * distanceToTouch;
		} else {
			return false;
		}
	}

	
	public void resolveCollision(final FluidParticle particle1, final FluidParticle particle2) {

		Vector2 positionDelta = particle1.getPosition().cpy().sub(particle2.getPosition().cpy());
        Vector2 velocityDelta = particle2.getVelocity().cpy().sub(particle1.getVelocity().cpy());
        float dotProduct = positionDelta.dot(velocityDelta);
        //used for checking if the particles are moving towards one another. This avoids particles getting stuck on each other and constantly colliding within each other.
        if(dotProduct >= 0) {
        	float angle = particle1.getVelocity().cpy().angle(particle2.getPosition().cpy().sub(particle1.getPosition().cpy()));
			int rotation = angle > 0 ? -1 : 1;
        	int scale = angle > -90 && angle < 90 ? 1 : -1;
        	Vector2 positionDifferenceAdjusted = angle > -90 && angle < 90 ? particle1.getPosition().cpy().sub(particle2.getPosition().cpy()) : particle2.getPosition().cpy().sub(particle1.getPosition().cpy());
        	Vector2 positionDifferenceFixed = particle2.getPosition().cpy().sub(particle1.getPosition().cpy());
        	//this works out the velocity which needs to be transfered from particle1 to particle2.
			Vector2 transfer = positionDifferenceAdjusted.cpy().scl(particle1.getVelocity().cpy().crs(positionDifferenceFixed.cpy()
					.rotate90(rotation).scl(1 / positionDifferenceAdjusted.cpy().crs(positionDifferenceFixed.cpy().rotate90(rotation)))));
			particle1.getNextVelocity().sub(transfer);
        	particle2.getNextVelocity().add(transfer);
        }
	}
	

	public void calculateParticleCollision(final World world, final FluidParticle particle1, final float delta) {
		for (final FluidParticle particle2 : world.getPositionalIndexTileMap().getNearbyEntities(FluidParticle.class, particle1.getPosition())) {
			if (particle1.getId() != particle2.getId()) {
				if (areParticlesColliding(particle1, particle2)) {
					resolveCollision(particle1, particle2);
				}
			}
		}
	}
	
	
	public void calculateParticleMovement(final World world, final FluidParticle particle, final float delta) {

//		try {

			particle.getNextPosition().add(particle.getNextVelocity().cpy().scl(0.016f));

			particle.getNextPosition().add(particle.getNextVelocity().cpy().scl(delta));
			
		
			
//			
//			final float gravity = world.getGravity();
//			if (particle1.getNextVelocity().len() > 2000f) {
//				particle1.getNextVelocity().add(0f, -gravity * delta).scl(0.95f);
//			} else {
//				particle1.getNextVelocity().add(0f, -gravity * delta);
//			}
//
//			final Tile tile = world.getTopography().getTile(particle1.getNextPosition().x,
//					particle1.getNextPosition().y - 1, true);
//			if (!tile.isPassable()) {
//				particle1.getNextVelocity().y = 0f;
//			}
//
//			final Tile tileUnder = world.getTopography().getTile(particle1.getNextPosition().x,
//					particle1.getNextPosition().y, true);
//			if (!tileUnder.isPassable()) {
//				final Vector2 trial = particle1.getNextPosition().cpy();
//				trial.y += -particle1.getNextVelocity().y * delta;
//
//				if (world.getTopography().getTile(trial.x, trial.y, true).isPassable()) {
//					particle1.getNextPosition().x = particle1.getNextPosition().x;
//					particle1.getNextPosition().y = particle1.getNextPosition().y;
//					particle1.getNextVelocity().y = -particle1.getNextVelocity().y * 0.25f;
//				} else {
//					particle1.getNextVelocity().x = -particle1.getNextVelocity().x;
//					particle1.getNextPosition().x = particle1.getNextPosition().x;
//					particle1.getNextPosition().y = particle1.getNextPosition().y;
//				}
//			}
			final Optional<FluidStrip> stripOn = world.fluids().getFluidStrip(
					Topography.convertToWorldTileCoord(particle.getPosition().x),
					Topography.convertToWorldTileCoord(particle.getPosition().y));
			if (stripOn.isPresent()) {
				if (particle.getPosition().y < convertToWorldCoord(stripOn.get().worldTileY, true)
						+ TILE_SIZE * stripOn.get().getVolume() / stripOn.get().width
						|| stripOn.get().getVolume() == 0f) {
					stripOn.get().addVolume(particle.getVolume());
					positionalIndexingService.removeFluidParticleIndex(particle);
					world.fluids().removeFluidParticle(particle.getId());
				}
			}
			
//		} catch (final NoTileFoundException e) {
//
//		}
	}
	
	
	/**
	 * Updates a {@link FluidParticle}
	 */
	public void updateParticle(final World world, final FluidParticle particle, final float delta) {
		positionalIndexingService.removeFluidParticleIndex(particle);
		calculateParticleMovement(world, particle, delta);
		particle.getPosition().set(particle.getNextPosition().cpy());
		particle.getVelocity().set(particle.getNextVelocity().cpy());
		positionalIndexingService.indexFluidParticle(particle);
	}


	private void transferFromStripToStripAbove(final World world, final FluidStrip strip) {
		if (strip.getVolume() > strip.width) {
			final Collection<Integer> stripsAbove = Sets.newConcurrentHashSet();
			for (int x = strip.worldTileX; x < strip.worldTileX + strip.width; x++) {
				final Optional<FluidStrip> tempStrip = world.fluids().getFluidStrip(x, strip.worldTileY + 1);
				//if there's a strip there already, use it
				if (tempStrip.isPresent()) {
					stripsAbove.add(tempStrip.get().id);
					x = tempStrip.get().worldTileX + tempStrip.get().width;
				//if not, try to make one
				} else {
					final Optional<FluidStrip> addedStrip = fluidStripPopulator.createFluidStrip(world, x, strip.worldTileY + 1, 0f);
					if(addedStrip.isPresent()) {
						stripsAbove.add(addedStrip.get().id);
						x = addedStrip.get().worldTileX + addedStrip.get().width;
					}
				}
			}

			final float extraFluid = strip.getVolume() - strip.width;
			if(!stripsAbove.isEmpty()) {
				for (final Integer key : stripsAbove) {
					final FluidStrip tempStrip = world.fluids().getFluidStrip(key).get();
					strip.addVolume(-tempStrip.addVolume(extraFluid/stripsAbove.size()));
				}
			}
		}
	}


	private void spewFromRightOfStrip(final World world, final FluidStrip strip) {
		try {
			if(world.getTopography().getTile(strip.worldTileX + strip.width, strip.worldTileY, true).isPassable()) {
				final Vector2 position = new Vector2(Topography.convertToWorldCoord(strip.worldTileX + strip.width, true) + 1f, Topography.convertToWorldCoord(strip.worldTileY, true) + 8f);
				final Vector2 velocity = new Vector2(Util.getRandom().nextFloat() * 200f, 0f).rotate(Util.getRandom().nextFloat() * 360f).add(200f,0f);
				final Optional<FluidStrip> rightStrip = world.fluids().getFluidStrip(strip.worldTileX + strip.width, strip.worldTileY);
				if(!rightStrip.isPresent() || strip.getVolume() / strip.width > rightStrip.get().getVolume() / rightStrip.get().width) {
					spewParticle(world, strip, position, velocity);
				}
			}
		} catch (final NoTileFoundException e) {}
	}


	private void spewFromLeftOfStrip(final World world, final FluidStrip strip) {
		try {
			if(world.getTopography().getTile(strip.worldTileX - 1, strip.worldTileY, true).isPassable()) {
				final Vector2 position = new Vector2(Topography.convertToWorldCoord(strip.worldTileX, true) - 1f, Topography.convertToWorldCoord(strip.worldTileY, true) + 8f);
				final Vector2 velocity = new Vector2(Util.getRandom().nextFloat() * 200f, 0f).rotate(Util.getRandom().nextFloat() * 360f).add(-200f,0f);
				final Optional<FluidStrip> leftStrip = world.fluids().getFluidStrip(strip.worldTileX - 1, strip.worldTileY);
				if(!leftStrip.isPresent() || strip.getVolume() / strip.width > leftStrip.get().getVolume() / leftStrip.get().width) {
					spewParticle(world, strip, position, velocity);
				}
			}
		} catch (final NoTileFoundException e) {}
	}


	private void spewFromBottomOfStrip(final World world, final FluidStrip strip) {
		for (int x = strip.worldTileX; x < strip.worldTileX + strip.width; x++) {
			final Optional<FluidStrip> tempStrip = world.fluids().getFluidStrip(x, strip.worldTileY - 1);
			if (!tempStrip.isPresent()) {
				try {
					if(world.getTopography().getTile(x, strip.worldTileY - 1, true).isPassable()) {
						//particles below this tile
						final Vector2 position = new Vector2(Topography.convertToWorldCoord(x, true) + Topography.TILE_SIZE / 2f, Topography.convertToWorldCoord(strip.worldTileY, true)-1);
						final Vector2 velocity = new Vector2(Util.getRandom().nextFloat() * 200f, 0f).rotate(Util.getRandom().nextFloat() * 360f).add(0f,-200f);
						spewParticle(world, strip, position, velocity);
					}
				} catch (final NoTileFoundException e) {}
			}
		}
	}


	private void transferFromStripToStripsBelow(final World world, final FluidStrip strip) {
		final Collection<Integer> stripsBelow = Sets.newConcurrentHashSet();
		for (int x = strip.worldTileX; x < strip.worldTileX + strip.width; x++) {
			final Optional<FluidStrip> tempStrip = world.fluids().getFluidStrip(x, strip.worldTileY-1);
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
			for (final Integer key : stripsBelow) {
				final FluidStrip tempBelow = world.fluids().getFluidStrip(key).get();
				if (tempBelow.getVolume() < tempBelow.width) {
					final Collection<FluidStrip> stripsAbove = getStripsAbove(world, tempBelow);
					if (stripsAbove.size() > 1) {
						float spaceBelow = tempBelow.width - tempBelow.getVolume();
						for(FluidStrip tempAbove : stripsAbove) {
							final float transferVolume = Math.min(spaceBelow,tempAbove.getVolume()/stripsBelow.size())/stripsAbove.size();
							tempBelow.addVolume(-tempAbove.addVolume(-transferVolume));
						}
					} else {
						final float transferVolume = Math.min(tempBelow.width - tempBelow.getVolume(),strip.getVolume()/stripsBelow.size());
						tempBelow.addVolume(-strip.addVolume(-transferVolume));
					}
				}
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
	private void spewParticle(final World world, final FluidStrip strip, final Vector2 position, final Vector2 velocity) {
		final int particlesToSpew = (int)(strip.pressureCounter + getPressure(world, strip));
		strip.pressureCounter = (strip.pressureCounter + getPressure(world, strip))%1;
		for(int p = particlesToSpew; p > 0; p--) {
			fluidParticlePopulator.createFluidParticle(position, velocity, -strip.addVolume(-MAX_PARTICLE_VOLUME), world);
		}
	}
	
	private void equalizeLevels(World world, FluidStrip strip) {
		if(strip.getVolume() - strip.width > -0.01f) {
			final Collection<Integer> stripsAbove = Sets.newConcurrentHashSet();
			for (int x = strip.worldTileX; x < strip.worldTileX + strip.width; x++) {
				final Optional<FluidStrip> tempStrip = world.fluids().getFluidStrip(x, strip.worldTileY + 1);
				// if there's a strip there already, use it
				if(tempStrip.isPresent()) {
					stripsAbove.add(tempStrip.get().id);
					x = tempStrip.get().worldTileX + tempStrip.get().width;
				//if not, try to make one
				} else {
					final Optional<FluidStrip> addedStrip = fluidStripPopulator.createFluidStrip(world, x, strip.worldTileY + 1, 0f);
					if(addedStrip.isPresent()) {
						stripsAbove.add(addedStrip.get().id);
						x = addedStrip.get().worldTileX + addedStrip.get().width;
					}
				}
			}
			if(stripsAbove.size() > 1) {
				Map<Integer, Float> depths = new HashMap<>();
				for (final Integer key : stripsAbove) {
					final FluidStrip tempStrip = world.fluids().getFluidStrip(key).get();
					depths.put(tempStrip.id, getDepth(world, tempStrip));
					
				}
				for (final Integer keyFrom : stripsAbove) {
					final FluidStrip tempStripFrom = world.fluids().getFluidStrip(keyFrom).get();
					for (final Integer keyTo : stripsAbove) {
						if(keyTo != keyFrom) {
							Float depthToTransfer = (depths.get(keyFrom) - depths.get(keyTo))/2;
							if(depthToTransfer > 0) {
								final FluidStrip tempStripTo = world.fluids().getFluidStrip(keyTo).get();
								tempStripTo.addVolume(-tempStripFrom.addVolume(-Math.min(tempStripFrom.getVolume(), depthToTransfer)/(stripsAbove.size() - 1)));
							}
						}
					}
				}
			}
		}
	}


	/**
	 * @param world
	 * @param strip
	 * @return The number of particles to spew.
	 */
	private float getPressure(final World world, final FluidStrip strip) {
		return getDepth(world, strip) * 3; //max 3 per tile depth
	}


	/**
	 * @param world
	 * @param strip
	 * @return the depth of the strips above the given strip at the deepest level.
	 */
	private float getDepth(final World world, final FluidStrip strip) {
		float depth = 0f;
		Comparator<FluidStrip> byVolume = new Comparator<FluidStrip>() {
			public int compare(FluidStrip strip1, FluidStrip strip2) {
				return Float.compare(strip1.getVolume(), strip2.getVolume());
			}
		};
		TreeSet<FluidStrip> currentStrips = Sets.newTreeSet(byVolume);
		currentStrips.add(strip);
		
		while(!currentStrips.isEmpty()) {
			depth += currentStrips.last().getVolume()/currentStrips.last().width;
			final TreeSet<FluidStrip> nextStrips = Sets.newTreeSet(byVolume);
			for(final FluidStrip currentStrip : currentStrips) {
				nextStrips.addAll(getStripsAbove(world, currentStrip)); // can have duplicates, this doesn't matter
			}
			currentStrips = nextStrips;
		}
		return depth;
	}


	/**
	 * @param world
	 * @param strip
	 * @return a collection of the Fluid Strips one tile above the given strip which are connected.
	 */
	private Collection<FluidStrip> getStripsAbove(final World world, final FluidStrip strip) {
		final Collection<FluidStrip> stripsAbove = Sets.newConcurrentHashSet();
		for (int x = strip.worldTileX; x < strip.worldTileX + strip.width; x++) {
			final Optional<FluidStrip> tempStrip = world.fluids().getFluidStrip(x, strip.worldTileY + 1);
			//if there's a strip there, put it in the list
			if (tempStrip.isPresent()) {
				stripsAbove.add(tempStrip.get());
				x = tempStrip.get().worldTileX + tempStrip.get().width;
			}
		}
		return stripsAbove;
	}
}