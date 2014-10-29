package bloodandmithril.performance;

import static bloodandmithril.world.topography.Topography.CHUNK_SIZE;
import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static bloodandmithril.world.topography.Topography.convertToChunkCoord;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

import bloodandmithril.core.Copyright;
import bloodandmithril.util.datastructure.ConcurrentDualKeyHashMap;

import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;

/**
 * A map that holds {@link PositionalIndexNode}s, maps chunks to indexes
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class PositionalIndexMap implements Serializable {
	private static final long serialVersionUID = 3198970349534676023L;

	/** Index datastructure */
	private ConcurrentDualKeyHashMap<Integer, Integer, PositionalIndexNode> indexes = new ConcurrentDualKeyHashMap<>();

	/**
	 * Constructor
	 */
	public PositionalIndexMap() {}


	/**
	 * @return a {@link Collection} of nearby entities of the same type, nearby meaning in the same or adjacent/diagonal chunk
	 */
	public Collection<Integer> getNearbyEntities(Class<?> clazz, float x, float y) {
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
	public Collection<Integer> getNearbyEntities(Class<?> clazz, Vector2 position) {
		return getNearbyEntities(clazz, position.x, position.y);
	}


	/**
	 * @return the {@link PositionalIndexNode} given the world coords
	 */
	public PositionalIndexNode get(float x, float y) {
		int chunkX = convertToChunkCoord(x);
		int chunkY = convertToChunkCoord(y);
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