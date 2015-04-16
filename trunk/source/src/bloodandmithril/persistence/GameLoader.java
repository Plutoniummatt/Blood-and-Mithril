package bloodandmithril.persistence;

import static bloodandmithril.persistence.GameSaver.getSavePath;
import static bloodandmithril.persistence.PersistenceUtil.decode;
import static bloodandmithril.util.Logger.loaderDebug;
import static com.badlogic.gdx.Gdx.files;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import bloodandmithril.character.faction.Faction;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.persistence.GameSaver.PersistenceMetaData;
import bloodandmithril.persistence.character.IndividualLoader;
import bloodandmithril.persistence.world.ChunkLoader;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.world.Domain;

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
	public static void load(PersistenceMetaData metadata, boolean newGame) {
		GameSaver.setPersistencePath("save/" + metadata.name);

		if (newGame) {
			GameSaver.mostRecentlyLoaded = null;
		} else {
			GameSaver.mostRecentlyLoaded = metadata;
		}

		ChunkLoader.loadGenerationData();
		ChunkLoader.loadWorlds();
		loadFactions();
		IndividualLoader.loadAll();
		if (ClientServerInterface.isClient()) {
			loadCameraPosition();
		}
	}


	@SuppressWarnings("unchecked")
	private static void loadFactions() {
		try {
			ConcurrentHashMap<Integer, Faction> decoded = (ConcurrentHashMap<Integer, Faction>) decode(files.local(getSavePath() + "/world/factions.txt"));
			HashSet<Integer> controlled = (HashSet<Integer>) decode(files.local(getSavePath() + "/world/controlledfactions.txt"));
			Domain.setFactions(decoded);
			if (ClientServerInterface.isClient()) {
				for (Integer controlledId : controlled) {
					BloodAndMithrilClient.controlledFactions.add(controlledId);
				}
			}
		} catch (Exception e) {
			loaderDebug("Failed to load factions", LogLevel.DEBUG);
		}
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


	/** Sets current camera position to a saved position found in {@link Parameters} */
	private static void loadCameraPosition() {
		Vector2 savedCameraPosition = ParameterPersistenceService.getParameters().getSavedCameraPosition();
		if (savedCameraPosition != null) {
			BloodAndMithrilClient.cam.position.x = savedCameraPosition.x;
			BloodAndMithrilClient.cam.position.y = savedCameraPosition.y;
		}
	}
}
