package spritestar.persistence.character;

import static spritestar.persistence.PersistenceUtil.decode;
import spritestar.character.Individual;
import spritestar.persistence.GameSaver;
import spritestar.util.Logger;
import spritestar.util.Logger.LogLevel;
import spritestar.world.GameWorld;

import com.badlogic.gdx.Gdx;

/**
 * Loads {@link Individual}s
 *
 * @author Matt
 */
public class IndividualLoader {

	/**
	 * Loads all {@link Individual}s
	 */
	public static void loadAll() {
		try {
			GameWorld.individuals = decode(Gdx.files.local(GameSaver.savePath + "/world/individuals.txt"));
		} catch (Exception e) {
			Logger.loaderDebug("Failed to load individuals", LogLevel.WARN);
		}

		for (Individual indi : GameWorld.individuals.values()) {
			if (indi.selected) {
				GameWorld.selectedIndividuals.add(indi);
			}
		}
	}
}