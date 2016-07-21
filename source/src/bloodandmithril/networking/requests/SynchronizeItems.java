package bloodandmithril.networking.requests;

import java.util.HashMap;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.networking.Response;
import bloodandmithril.world.Domain;

import com.google.common.collect.Maps;

/**
 * {@link Response} to synchronize {@link Item}s in the world
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class SynchronizeItems implements Response {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1260091097982289461L;
	private HashMap<Integer, Item> items;
	private int worldId;

	public SynchronizeItems(int worldId) {
		this.worldId = worldId;
		items = Maps.newHashMap(Domain.getWorld(worldId).items().getItemsMap());
	}


	@Override
	public void acknowledge() {
		Domain.getWorld(worldId).items().getItemsMap().clear();
		Domain.getWorld(worldId).items().getItemsMap().putAll(items);;
	}


	@Override
	public int forClient() {
		return -1;
	}


	@Override
	public void prepare() {
	}
}