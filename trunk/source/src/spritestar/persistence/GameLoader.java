package spritestar.persistence;

import com.badlogic.gdx.math.Vector2;

import spritestar.Fortress;
import spritestar.persistence.character.IndividualLoader;
import spritestar.persistence.world.ChunkLoaderImpl;
import spritestar.world.Epoch;
import spritestar.world.WorldState;

/**
 * Loads a saved game
 *
 * @author Matt
 */
public class GameLoader {

	/**
	 * Loads a saved game
	 */
	public static void load() {
		ChunkLoaderImpl.loadGenerationData();
		IndividualLoader.loadAll();
		loadCameraPosition();
		loadCurrentEpoch();
	}

	
	/** Sets current {@link Epoch} to a saved one */
	private static void loadCurrentEpoch() {
		Epoch epoch = ParameterPersistenceService.getParameters().getCurrentEpoch();
		if (epoch != null) {
			WorldState.currentEpoch = epoch;
		}
	}


	/** Sets current camera position to a saved position found in {@link Parameters} */
	private static void loadCameraPosition() {
		Vector2 savedCameraPosition = ParameterPersistenceService.getParameters().getSavedCameraPosition();
		if (savedCameraPosition != null) {
			Fortress.cam.position.x = savedCameraPosition.x;
			Fortress.cam.position.y = savedCameraPosition.y;
		}
	}
}
