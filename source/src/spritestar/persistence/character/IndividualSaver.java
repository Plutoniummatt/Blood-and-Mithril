package spritestar.persistence.character;

import static spritestar.persistence.PersistenceUtil.encode;
import spritestar.character.Individual;
import spritestar.persistence.GameSaver;
import spritestar.world.GameWorld;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/**
 * Saves {@link Individual}s
 *
 * @author Matt
 */
public class IndividualSaver {

	/**
	 * Saves all {@link Individual}s
	 */
	public static void saveAll() {
		FileHandle individuals = Gdx.files.local(GameSaver.savePath + "/world/individuals.txt");
		individuals.writeString(encode(GameWorld.individuals), false);
	}
}