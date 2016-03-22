package bloodandmithril.persistence;

import static bloodandmithril.persistence.PersistenceUtil.decode;
import static bloodandmithril.persistence.PersistenceUtil.encode;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.world.Domain;

/**
 * Responsible for persistence of {@link Parameters}
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2014")
public class ParameterPersistenceService {

	@Inject private GameSaver gameSaver;

	/** THE current set of {@link Parameters} */
	private Parameters parameters = loadParameters();

	/** THE current set of {@link Parameters} */
	public synchronized Parameters getParameters() {
		Parameters params = parameters == null ? loadParameters() : parameters;
		return params;
	}

	/** Loads and returns persisted parameters from disk */
	public synchronized Parameters loadParameters() {
		try {
			FileHandle file = Gdx.files.local(gameSaver.getSavePath() + "/parameters.txt");
			parameters = decode(file);
			return parameters;
		} catch (Exception e) {
			Logger.loaderDebug("No parameters found on disk, using new parameter set", LogLevel.DEBUG);
			parameters = new Parameters();
			return parameters;
		}
	}


	/** Saves the {@link Parameters} */
	public synchronized void saveParameters() {
		FileHandle file = Gdx.files.local(gameSaver.getSavePath() + "/parameters.txt");
		parameters.setActiveWorldId(Domain.getActiveWorldId());
		parameters.setSavedCameraPosition(ClientServerInterface.isClient() ? Maps.newHashMap(BloodAndMithrilClient.getWorldcamcoordinates()) : Maps.newHashMap());
		file.writeString(encode(parameters), false);
	}
}
