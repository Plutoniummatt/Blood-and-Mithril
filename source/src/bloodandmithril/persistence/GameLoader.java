package bloodandmithril.persistence;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.persistence.character.IndividualLoader;
import bloodandmithril.persistence.item.ItemLoader;
import bloodandmithril.persistence.prop.PropLoader;
import bloodandmithril.persistence.world.ChunkLoader;
import bloodandmithril.world.Epoch;
import bloodandmithril.world.WorldState;

import com.badlogic.gdx.math.Vector2;


/**
 * Loads a saved game
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class GameLoader {

	/**
	 * Loads a saved game
	 */
	public static void load() {
		ChunkLoader.loadGenerationData();
		ChunkLoader.loadWorlds();
		IndividualLoader.loadAll();
		PropLoader.loadAll();
		ItemLoader.loadAll();
		if (ClientServerInterface.isClient()) {
			loadCameraPosition();
		}
		loadCurrentEpoch();
	}


	/** Sets current {@link Epoch} to a saved one */
	private static void loadCurrentEpoch() {
		Epoch epoch = ParameterPersistenceService.getParameters().getCurrentEpoch();
		if (epoch != null) {
			WorldState.setCurrentEpoch(epoch);
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
