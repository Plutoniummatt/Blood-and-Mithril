package bloodandmithril.csi.requests;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import bloodandmithril.csi.Response;
import bloodandmithril.item.items.Item;
import bloodandmithril.world.Domain;

import com.google.common.collect.Maps;

/**
 * {@link Response} to synchronize {@link Item}s in the world
 *
 * @author Matt
 */
public class SynchronizeItems implements Response {

	private HashMap<Integer, Item> items;

	public SynchronizeItems() {
		ConcurrentHashMap<Integer, Item> domainItems = Domain.getItems();
		synchronized (domainItems) {
			items = Maps.newHashMap(domainItems);
		}
	}


	@Override
	public void acknowledge() {
		Domain.getItems().clear();
		Domain.getItems().putAll(items);
	}


	@Override
	public int forClient() {
		return -1;
	}


	@Override
	public void prepare() {
	}
}