package bloodandmithril.persistence;

import static bloodandmithril.persistence.PersistenceUtil.decode;
import static bloodandmithril.util.Logger.loaderDebug;
import static com.badlogic.gdx.Gdx.files;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.faction.Faction;
import bloodandmithril.character.faction.FactionControlService;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.persistence.GameSaver.PersistenceMetaData;
import bloodandmithril.persistence.character.IndividualLoader;
import bloodandmithril.persistence.world.ChunkLoader;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.world.Domain;


/**
 * Loads a saved game
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2014")
public class GameLoader {

	@Inject private ParameterPersistenceService parameterPersistenceService;
	@Inject private ChunkLoader chunkLoader;
	@Inject private FactionControlService factionControlService;
	@Inject private GameSaver gameSaver;
	@Inject private IndividualLoader individualLoader;

	/**
	 * Loads a saved game
	 */
	public void load(PersistenceMetaData metadata, boolean newGame) {
		gameSaver.setPersistencePath("save/" + metadata.name);

		if (newGame) {
			gameSaver.mostRecentlyLoaded = null;
		} else {
			gameSaver.mostRecentlyLoaded = metadata;
			if (ClientServerInterface.isClient()) {
				loadCameraPosition();
			}
			parameterPersistenceService.loadParameters();
			Domain.setActiveWorld(parameterPersistenceService.getParameters().getActiveWorldId());
			chunkLoader.loadGenerationData();
			chunkLoader.loadWorlds();
			loadFactions();
			individualLoader.loadAll();
		}
	}


	@SuppressWarnings("unchecked")
	private void loadFactions() {
		try {
			ConcurrentHashMap<Integer, Faction> decoded = (ConcurrentHashMap<Integer, Faction>) decode(files.local(gameSaver.getSavePath() + "/world/factions.txt"));
			HashSet<Integer> controlled = (HashSet<Integer>) decode(files.local(gameSaver.getSavePath() + "/world/controlledfactions.txt"));
			Domain.setFactions(decoded);
			if (ClientServerInterface.isClient()) {
				for (Integer controlledId : controlled) {
					factionControlService.control(controlledId);
				}
			}
		} catch (Exception e) {
			loaderDebug("Failed to load factions", LogLevel.DEBUG);
		}
	}


	/** Loads and returns a collection of metadata of saved games */
	public Set<PersistenceMetaData> loadMetaData() {
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
	private void loadCameraPosition() {
		Map<Integer, Vector2> savedCameraPositions = parameterPersistenceService.getParameters().getSavedCameraPosition();
		if (savedCameraPositions != null) {
			BloodAndMithrilClient.getWorldcamcoordinates().clear();
			BloodAndMithrilClient.getWorldcamcoordinates().putAll(savedCameraPositions);
		}
	}
}
