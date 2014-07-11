package bloodandmithril.persistence.item;

import static bloodandmithril.persistence.PersistenceUtil.encode;
import bloodandmithril.item.items.Item;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/**
 * Handles the saving of items
 *
 * @author Matt
 */
public class ItemSaver {

	/**
	 * Saves all {@link Item}s at {@link Domain#getItems()}
	 */
	public static void saveAll() {
		FileHandle props = Gdx.files.local(GameSaver.savePath + "/world/items.txt");
		props.writeString(encode(Domain.getItems()), false);
	}
}