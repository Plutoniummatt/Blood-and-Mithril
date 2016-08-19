package bloodandmithril.persistence;

import static bloodandmithril.persistence.PersistenceUtil.decode;
import static bloodandmithril.util.Logger.loaderDebug;
import static com.badlogic.gdx.Gdx.files;

import java.util.HashMap;
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
import bloodandmithril.control.CameraTracker;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.generation.Structure;
import bloodandmithril.generation.Structures;
import bloodandmithril.generation.patterns.GlobalLayers;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.persistence.GameSaver.PersistenceMetaData;
import bloodandmithril.persistence.character.IndividualLoader;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Topography;


/**
 * Loads a saved game
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2014")
public class GameLoader {

	@Inject private ParameterPersistenceService parameterPersistenceService;
	@Inject private FactionControlService factionControlService;
	@Inject private PersistenceParameters persistenceParameters;
	@Inject private IndividualLoader individualLoader;
	@Inject private GameClientStateTracker gameClientStateTracker;
	@Inject private Graphics graphics;
	@Inject private CameraTracker cameraTracker;

	/**
	 * Loads a saved game
	 */
	public void load(final PersistenceMetaData metadata, final boolean newGame) {
		persistenceParameters.setSavePath("save/" + metadata.name);

		if (newGame) {
			persistenceParameters.setMostRecentlyLoaded(null);
		} else {
			persistenceParameters.setMostRecentlyLoaded(metadata);
			parameterPersistenceService.setParameters(parameterPersistenceService.loadParameters());
			gameClientStateTracker.setActiveWorldId(parameterPersistenceService.getParameters().getActiveWorldId());
			loadGenerationData();
			loadWorlds();
			loadFactions();
			individualLoader.loadAll();

			if (ClientServerInterface.isClient()) {
				loadCameraPosition();
			}
			graphics.getCam().position.x = cameraTracker.getWorldcamcoordinates().get(parameterPersistenceService.getParameters().getActiveWorldId()).x;
			graphics.getCam().position.y = cameraTracker.getWorldcamcoordinates().get(parameterPersistenceService.getParameters().getActiveWorldId()).y;
		}
	}

	/**
	 * Loads generation data
	 **/
	public synchronized void loadGenerationData() {
		try {
			final ConcurrentHashMap<Integer, Structure> structures = decode(Gdx.files.local(persistenceParameters.getSavePath() + "/world/structures.txt"));
			Structures.setStructures(structures);
		} catch (final Exception e) {
			Logger.loaderDebug("Failed to load structures", LogLevel.DEBUG);
		}

		try {
			GlobalLayers.layers = decode(Gdx.files.local(persistenceParameters.getSavePath() + "/world/layers.txt"));
		} catch (final Exception e) {
			Logger.loaderDebug("Failed to load layers", LogLevel.DEBUG);
		}
	}


	/**
	 * Loads all worlds
	 */
	public void loadWorlds() {
		try {
			final HashMap<Integer, World> worlds = decode(Gdx.files.local(persistenceParameters.getSavePath() + "/world/worlds.txt"));

			worlds.values().stream().forEach(world -> {
				Domain.addWorld(world);
			});
		} catch (final Exception e) {
			Logger.loaderDebug("Failed to load worlds", LogLevel.DEBUG);
		}

		if (!Domain.getAllWorlds().isEmpty()) {
			for (final World world : Domain.getAllWorlds()) {
				world.setTopography(new Topography());

				try {
					final ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> keys = decode(Gdx.files.local(persistenceParameters.getSavePath() + "/world/world" + Integer.toString(world.getWorldId()) + "/superStructureKeys.txt"));
					world.getTopography().getStructures().setSuperStructureKeys(keys);
				} catch (final Exception e) {
					Logger.loaderDebug("Failed to load chunk super structure structure keys", LogLevel.DEBUG);
					final ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> map = new ConcurrentHashMap<>();
					world.getTopography().getStructures().setSuperStructureKeys(map);
				}

				try {
					final ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> keys = decode(Gdx.files.local(persistenceParameters.getSavePath() + "/world/world" + Integer.toString(world.getWorldId()) + "/subStructureKeys.txt"));
					world.getTopography().getStructures().setSubStructureKeys(keys);
				} catch (final Exception e) {
					Logger.loaderDebug("Failed to load chunk sub structure keys", LogLevel.DEBUG);
					final ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> map = new ConcurrentHashMap<>();
					world.getTopography().getStructures().setSubStructureKeys(map);
				}
			}
		}
	}


	@SuppressWarnings("unchecked")
	private void loadFactions() {
		try {
			final ConcurrentHashMap<Integer, Faction> decoded = (ConcurrentHashMap<Integer, Faction>) decode(files.local(persistenceParameters.getSavePath() + "/world/factions.txt"));
			final HashSet<Integer> controlled = (HashSet<Integer>) decode(files.local(persistenceParameters.getSavePath() + "/world/controlledfactions.txt"));

			decoded.values().stream().forEach(faction -> {
				Domain.addFaction(faction);
			});

			if (ClientServerInterface.isClient()) {
				for (final Integer controlledId : controlled) {
					factionControlService.control(controlledId);
				}
			}
		} catch (final Exception e) {
			loaderDebug("Failed to load factions", LogLevel.DEBUG);
		}
	}


	/** Loads and returns a collection of metadata of saved games */
	public Set<PersistenceMetaData> loadMetaData() {
		final Set<PersistenceMetaData> data = Sets.newHashSet();

		final FileHandle local = Gdx.files.local("save");
		for (final FileHandle directory : local.list()) {
			if (directory.isDirectory()) {
				data.add(PersistenceUtil.decode(directory.child("metadata.txt")));
			}
		}

		return data;
	}


	/** Sets current camera position to a saved position found in {@link Parameters} */
	private void loadCameraPosition() {
		final Map<Integer, Vector2> savedCameraPositions = parameterPersistenceService.getParameters().getSavedCameraPosition();
		if (savedCameraPositions != null) {
			cameraTracker.getWorldcamcoordinates().clear();
			cameraTracker.getWorldcamcoordinates().putAll(savedCameraPositions);
		}
	}
}
