package bloodandmithril.persistence;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.persistence.character.IndividualLoader;
import bloodandmithril.persistence.world.ChunkLoaderImpl;
import bloodandmithril.world.Epoch;
import bloodandmithril.world.WorldState;

import com.badlogic.gdx.math.Vector2;


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
		if (ClientServerInterface.isClient()) {
			loadCameraPosition();
		}
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
			BloodAndMithrilClient.cam.position.x = savedCameraPosition.x;
			BloodAndMithrilClient.cam.position.y = savedCameraPosition.y;
		}
	}
}
