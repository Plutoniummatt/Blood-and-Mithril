package bloodandmithril.performance;

import static bloodandmithril.world.topography.Topography.CHUNK_SIZE;
import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static bloodandmithril.world.topography.Topography.convertToChunkCoord;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.MouseOverable;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.item.items.Item;
import bloodandmithril.prop.Prop;
import bloodandmithril.util.datastructure.ConcurrentDualKeyHashMap;
import bloodandmithril.world.Domain;
import bloodandmithril.world.fluids.FluidStrip;

/**
 * A map that holds {@link PositionalIndexNode}s, maps chunks to indexes
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class PositionalIndexMap implements Serializable {
	private static final long serialVersionUID = 3198970349534676023L;
	private final int worldId;

	/** Index datastructure */
	private ConcurrentDualKeyHashMap<Integer, Integer, PositionalIndexNode> indexes = new ConcurrentDualKeyHashMap<>();

	/**
	 * Constructor
	 */
	public PositionalIndexMap(int worldId) {
		this.worldId = worldId;
	}


	/**
	 * @return a {@link Collection} of nearby entities of the same type, nearby meaning in the same or adjacent/diagonal chunk
	 */
	public Collection<Integer> getNearbyEntityIds(Class<?> clazz, float x, float y) {
		LinkedList<Integer> entities = Lists.newLinkedList();

		int i = CHUNK_SIZE * TILE_SIZE;
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
	public Collection<Integer> getOnScreenEntities(Class<?> clazz, Graphics graphics) {
		return getEntitiesWithinBounds(
			clazz,
			graphics.getCam().position.x - graphics.getWidth(),
			graphics.getCam().position.x + graphics.getWidth(),
			graphics.getCam().position.y + graphics.getHeight(),
			graphics.getCam().position.y - graphics.getHeight()
		);
	}
	
	
	/**
	 * @return a {@link Collection} of {@link PositionalIndexNode} that are on screen
	 */
	public Collection<PositionalIndexNode> getOnScreenNodes(Graphics graphics) {
		return getNodesWithinBounds(
			graphics.getCam().position.x - graphics.getWidth(),
			graphics.getCam().position.x + graphics.getWidth(),
			graphics.getCam().position.y + graphics.getHeight(),
			graphics.getCam().position.y - graphics.getHeight()
		);
	}
	
	
	/**
	 * @return a {@link Collection} of {@link PositionalIndexNode}s of that are contained (roughly) within a defined box.
	 *
	 * Roughly because the indexing nodes are quantised.
	 */
	public Collection<PositionalIndexNode> getNodesWithinBounds(float left, float right, float top, float bottom) {
		int i = CHUNK_SIZE * TILE_SIZE;

		int xSteps = (int)(right - left) / i + 1;
		int ySteps = (int)(top - bottom) / i + 1;
		
		Collection<PositionalIndexNode> nodes = Lists.newLinkedList();

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
	public List<Integer> getEntitiesWithinBounds(Class<?> clazz, float left, float right, float top, float bottom) {
		LinkedList<Integer> entities = Lists.newLinkedList();

		int i = CHUNK_SIZE * TILE_SIZE;

		int xSteps = (int)(right - left) / i + 1;
		int ySteps = (int)(top - bottom) / i + 1;

		for (int x = 0; x <= xSteps; x++) {
			for (int y = 0; y <= ySteps; y++) {
				entities.addAll(get(left + x * i, bottom + y * i).getAllEntitiesForType(clazz));
			}
		}

		return entities;
	}


	public Collection<PositionalIndexNode> getNearbyNodes(float x, float y) {
		LinkedList<PositionalIndexNode> nodes = Lists.newLinkedList();

		int i = CHUNK_SIZE * TILE_SIZE;
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
	 * @return a {@link Collection} of nearby entities of the same type, nearby meaning in the same or adjacent/diagonal chunk
	 */
	public Collection<Integer> getNearbyEntityIds(Class<?> clazz, Vector2 position) {
		return getNearbyEntityIds(clazz, position.x, position.y);
	}


	/**
	 * @return a {@link Collection} of nearby entities of the same type, nearby meaning in the same or adjacent/diagonal chunk
	 */
	public <T extends MouseOverable> Collection<T> getNearbyEntities(Class<T> clazz, Vector2 position) {
		return getNearbyEntities(clazz, position.x, position.y);
	}


	/**
	 * @return a {@link Collection} of nearby entities of the same type, nearby meaning in the same or adjacent/diagonal chunk
	 */
	@SuppressWarnings("unchecked")
	public <T extends MouseOverable> Collection<T> getNearbyEntities(Class<T> clazz, float x, float y) {
		Collection<T> ts = Lists.newLinkedList();

		Function<Integer, T> function;
		if (clazz.equals(Individual.class)) {
			function = id -> (T) Domain.getIndividual(id);
		} else if (clazz.equals(Prop.class)) {
			function = id -> (T) Domain.getWorld(worldId).props().getProp(id);
		} else if (clazz.equals(Item.class)) {
			function = id -> (T) Domain.getWorld(worldId).items().getItem(id);
		} else if (clazz.equals(FluidStrip.class)) {
			function = id -> (T) Domain.getWorld(worldId).fluids().getFluidStrip(id);
		} else {
			throw new RuntimeException("Unrecongized class : " + clazz.getSimpleName());
		}

		for (int id : getNearbyEntityIds(clazz, x, y)) {
			ts.add(function.apply(id));
		}

		return ts;
	}


	/**
	 * @return the {@link PositionalIndexNode} given the world coords
	 */
	public PositionalIndexNode get(float x, float y) {
		return getWithChunkCoords(
			convertToChunkCoord(x), 
			convertToChunkCoord(y)
		);
	}
	
	
	/**
	 * @return the {@link PositionalIndexNode} given the world coords
	 */
	public PositionalIndexNode getWithTileCoords(int tileX, int tileY) {
		return getWithChunkCoords(
			convertToChunkCoord(tileX), 
			convertToChunkCoord(tileY)
		);
	}
	
	
	/**
	 * @return the {@link PositionalIndexNode} given the world coords
	 */
	public PositionalIndexNode getWithChunkCoords(int chunkX, int chunkY) {
		PositionalIndexNode positionalIndex = indexes.get(chunkX, chunkY);

		if (positionalIndex == null) {
			PositionalIndexNode value = new PositionalIndexNode();
			indexes.put(chunkX, chunkY, value);
			return value;
		}

		return positionalIndex;
	}


	public Collection<PositionalIndexNode> getAllNodes() {
		return indexes.getAllValues();
	}
}