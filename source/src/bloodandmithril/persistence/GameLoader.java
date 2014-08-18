package bloodandmithril.persistence;

import java.util.Set;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.persistence.GameSaver.PersistenceMetaData;
import bloodandmithril.persistence.character.IndividualLoader;
import bloodandmithril.persistence.item.ItemLoader;
import bloodandmithril.persistence.prop.PropLoader;
import bloodandmithril.persistence.world.ChunkLoader;
import bloodandmithril.world.Epoch;
import bloodandmithril.world.WorldState;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Sets;


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
	public static void load(String name) {
		GameSaver.savePath = "save/" + name;
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


	/** Loads and returns a collection of metadata of saved games */
	public static Set<PersistenceMetaData> loadMetaData() {
		Set<PersistenceMetaData> data = Sets.newHashSet();

		FileHandle local = Gdx.files.local("save");
		for (FileHandle directory : local.list()) {
			if (directory.isDirectory()) {
				data.add(PersistenceUtil.decode(directory.child("metadata.txt")));
			}
		}

		return data;
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
