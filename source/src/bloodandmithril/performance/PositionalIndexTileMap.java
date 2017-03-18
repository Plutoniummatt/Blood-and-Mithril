package bloodandmithril.performance;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static bloodandmithril.world.topography.Topography.convertToWorldTileCoord;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.util.datastructure.ConcurrentDualKeyHashMap;
import bloodandmithril.world.Domain;
import bloodandmithril.world.fluids.FluidParticle;

/**
 * A map that holds {@link PositionalIndexTileNode}s, maps tiles to indexes
 *
 * @author Sam
 */
@Copyright("Matthew Peck 2014")
public class PositionalIndexTileMap implements Serializable {
	private static final long serialVersionUID = 3198970349534676023L;
	private final int worldId;

	/** Index datastructure */
	private ConcurrentDualKeyHashMap<Integer, Integer, PositionalIndexTileNode> indexes = new ConcurrentDualKeyHashMap<>();

	/**
	 * Constructor
	 */
	public PositionalIndexTileMap(int worldId) {
		this.worldId = worldId;
	}

	
	/**
	 * @return a {@link Collection} of nearby entities of the same type, nearby meaning in the same or adjacent/diagonal tile
	 */
	public Collection<Long> getNearbyEntityIds(Class<?> clazz, float x, float y) {
		LinkedList<Long> entities = Lists.newLinkedList();

		int i = TILE_SIZE;
		entities.addAll(get(x, y).getAllEntitiesForType(clazz));
		entities.addAll(get(x, y + i).getAllEntitiesForType(clazz));
		entities.addAll(get(x + i, y + i).getAllEntitiesForType(clazz));
		entities.addAll(get(x + i, y).getAllEntitiesForType(clazz));
		entities.addAll(get(x + i, y - i).getAllEntitiesForType(clazz));
		entities.addAll(get(x, y - i).getAllEntitiesForType(clazz));
		entities.addAll(get(x - i, y - i).getAllEntitiesForType(clazz));
		entities.addAll(get(x - i, y).getAllEntitiesForType(clazz));
		entities.addAll(get(x - i, y + i).getAllEntitiesForType(clazz));

		return entities;
	}
	
	
	/**
	 * @return a {@link Collection} of entities of the same type, that are on screen
	 */
	public Collection<Long> getOnScreenEntities(Class<?> clazz, Graphics graphics) {
		return getEntitiesWithinBounds(
			clazz,
			graphics.getCam().position.x - graphics.getWidth(),
			graphics.getCam().position.x + graphics.getWidth(),
			graphics.getCam().position.y + graphics.getHeight(),
			graphics.getCam().position.y - graphics.getHeight()
		);
	}
	
	
	/**
	 * @return a {@link Collection} of {@link PositionalIndexTileNode} that are on screen
	 */
	public Collection<PositionalIndexTileNode> getOnScreenNodes(Graphics graphics) {
		return getNodesWithinBounds(
			graphics.getCam().position.x - graphics.getWidth(),
			graphics.getCam().position.x + graphics.getWidth(),
			graphics.getCam().position.y + graphics.getHeight(),
			graphics.getCam().position.y - graphics.getHeight()
		);
	}
	
	
	/**
	 * @return a {@link Collection} of {@link PositionalIndexTileNode}s of that are contained (roughly) within a defined box.
	 *
	 * Roughly because the indexing nodes are quantised.
	 */
	public Collection<PositionalIndexTileNode> getNodesWithinBounds(float left, float right, float top, float bottom) {
		int i = TILE_SIZE;

		int xSteps = (int)(right - left) / i + 1;
		int ySteps = (int)(top - bottom) / i + 1;
		
		Collection<PositionalIndexTileNode> nodes = Lists.newLinkedList();

		for (int x = 0; x <= xSteps; x++) {
			for (int y = 0; y <= ySteps; y++) {
				nodes.add(get(left + x * i, bottom + y * i));
			}
		}

		return nodes;
	}


	/**
	 * @return a {@link Collection} of entities of that are contained (roughly) within a defined box.
	 *
	 * Roughly because the indexing nodes are quantised.
	 */
	public List<Long> getEntitiesWithinBounds(Class<?> clazz, float left, float right, float top, float bottom) {
		LinkedList<Long> entities = Lists.newLinkedList();

		int i = TILE_SIZE;

		int xSteps = (int)(right - left) / i + 1;
		int ySteps = (int)(top - bottom) / i + 1;

		for (int x = 0; x <= xSteps; x++) {
			for (int y = 0; y <= ySteps; y++) {
				entities.addAll(get(left + x * i, bottom + y * i).getAllEntitiesForType(clazz));
			}
		}

		return entities;
	}


	public Collection<PositionalIndexTileNode> getNearbyNodes(float x, float y) {
		LinkedList<PositionalIndexTileNode> nodes = Lists.newLinkedList();

		int i = TILE_SIZE;
		nodes.add(get(x, y));
		nodes.add(get(x, y + i));
		nodes.add(get(x + i, y + i));
		nodes.add(get(x + i, y));
		nodes.add(get(x + i, y - i));
		nodes.add(get(x, y - i));
		nodes.add(get(x - i, y - i));
		nodes.add(get(x - i, y));
		nodes.add(get(x - i, y + i));

		return nodes;
	}


	/**
	 * @return a {@link Collection} of nearby entities of the same type, nearby meaning in the same or adjacent/diagonal tile
	 */
	public Collection<Long> getNearbyEntityIds(Class<?> clazz, Vector2 position) {
		return getNearbyEntityIds(clazz, position.x, position.y);
	}


	/**
	 * @return a {@link Collection} of nearby entities of the same type, nearby meaning in the same or adjacent/diagonal tile
	 */
	public <T> Collection<T> getNearbyEntities(Class<T> clazz, Vector2 position) {
		return getNearbyEntities(clazz, position.x, position.y);
	}


	/**
	 * @return a {@link Collection} of nearby entities of the same type, nearby meaning in the same or adjacent/diagonal tile
	 */
	@SuppressWarnings("unchecked")
	public <T> Collection<T> getNearbyEntities(Class<T> clazz, float x, float y) {
		Collection<T> ts = Lists.newLinkedList();

		Function<Long, T> function;
		if (clazz.equals(FluidParticle.class)) {
			function = id -> {
				if(Domain.getWorld(worldId).fluids().getFluidParticle(id).isPresent()) {
					return (T) Domain.getWorld(worldId).fluids().getFluidParticle(id).get();
				} else {
					return null;
				}
			};
		}
		
		 else {
			throw new RuntimeException("Unrecongized class : " + clazz.getSimpleName());
		}

		for (long id : getNearbyEntityIds(clazz, x, y)) {
			if(function.apply(id) != null) {
				ts.add(function.apply(id));
			}
		}
			
		return ts;
	}


	/**
	 * @return the {@link PositionalIndexTileNode} given the world coords
	 */
	public PositionalIndexTileNode get(float x, float y) {
		return getWithTileCoords(
			convertToWorldTileCoord(x), 
			convertToWorldTileCoord(y)
		);
	}
	
	
	/**
	 * @return the {@link PositionalIndexTileNode} given the world coords
	 */
	public PositionalIndexTileNode getWithTileCoords(int tileX, int tileY) {
		PositionalIndexTileNode positionalIndex = indexes.get(tileX, tileY);

		if (positionalIndex == null) {
			PositionalIndexTileNode value = new PositionalIndexTileNode();
			indexes.put(tileX, tileY, value);
			return value;
		}

		return positionalIndex;
	}


	public Collection<PositionalIndexTileNode> getAllNodes() {
		return indexes.getAllValues();
	}
}