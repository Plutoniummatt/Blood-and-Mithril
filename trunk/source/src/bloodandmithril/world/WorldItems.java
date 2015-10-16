package bloodandmithril.world;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.badlogic.gdx.math.Vector2;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.util.Util;

/**
 * Encapsulation class to organise logic related to {@link Item}s that exist on a {@link World}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public final class WorldItems implements Serializable {
	private static final long serialVersionUID = 560317877803658591L;

	/** Every {@link Item} that exists that is not stored in a {@link Container} */
	private final ConcurrentHashMap<Integer, Item> items = new ConcurrentHashMap<>();
	private final int worldId;

	/**
	 * Constructor
	 */
	public WorldItems(int worldId) {
		this.worldId = worldId;
	}

	public final int addItem(Item item, Vector2 position, Vector2 velocity) {
		if (item.rotates()) {
			item.setAngularVelocity((Util.getRandom().nextFloat() - 0.5f) * 40f);
		}

		item.setWorldId(worldId);
		item.setId(ParameterPersistenceService.getParameters().getNextItemId());
		item.setPosition(position);
		item.setVelocity(velocity);
		items.put(item.getId(), item);

		return item.getId();
	}


	public final Item getItem(int id) {
		return items.get(id);
	}


	public final boolean hasItem(int id) {
		return items.containsKey(id);
	}


	public final void removeItem(Integer id) {
		Item item = items.get(id);
		Domain.getWorld(worldId).getPositionalIndexMap().get(item.getPosition().x, item.getPosition().y).removeItem(id);
		items.remove(id);
	}


	public final Collection<Item> getItems() {
		return items.values();
	}


	public final Map<Integer, Item> getItemsMap() {
		return items;
	}
}
