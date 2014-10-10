package bloodandmithril.performance;

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

		entities.addAll(get(x, y).getAllEntitiesForType(clazz));
		entities.addAll(get(x, y + 1).getAllEntitiesForType(clazz));
		entities.addAll(get(x + 1, y + 1).getAllEntitiesForType(clazz));
		entities.addAll(get(x + 1, y).getAllEntitiesForType(clazz));
		entities.addAll(get(x + 1, y - 1).getAllEntitiesForType(clazz));
		entities.addAll(get(x, y - 1).getAllEntitiesForType(clazz));
		entities.addAll(get(x - 1, y - 1).getAllEntitiesForType(clazz));
		entities.addAll(get(x - 1, y).getAllEntitiesForType(clazz));
		entities.addAll(get(x - 1, y + 1).getAllEntitiesForType(clazz));

		return entities;
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
}