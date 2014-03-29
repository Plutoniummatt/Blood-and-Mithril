package bloodandmithril.persistence.prop;

import static bloodandmithril.persistence.PersistenceUtil.encode;
import bloodandmithril.character.Individual;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/**
 * Handles the saving of props
 *
 * @author Matt
 */
public class PropSaver {

	/**
	 * Saves all {@link Individual}s
	 */
	public static void saveAll() {
		FileHandle props = Gdx.files.local(GameSaver.savePath + "/world/props.txt");
		props.writeString(encode(Domain.getProps()), false);
	}
}