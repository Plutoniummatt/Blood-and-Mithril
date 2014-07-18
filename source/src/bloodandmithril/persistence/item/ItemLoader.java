package bloodandmithril.persistence.item;

import static bloodandmithril.persistence.GameSaver.savePath;
import static bloodandmithril.persistence.PersistenceUtil.decode;
import static bloodandmithril.util.Logger.loaderDebug;
import static com.badlogic.gdx.Gdx.files;

import java.util.concurrent.ConcurrentHashMap;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.prop.Prop;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.world.Domain;

/**
 * Loads {@link Item}s from disk
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class ItemLoader {

	/**
	 * Loads all {@link Prop}s
	 */
	@SuppressWarnings("unchecked")
	public static void loadAll() {
		try {
			Domain.setItems((ConcurrentHashMap<Integer, Item>) decode(files.local(savePath + "/world/items.txt")));
		} catch (Exception e) {
			loaderDebug("Failed to load items", LogLevel.WARN);
		}
	}
}