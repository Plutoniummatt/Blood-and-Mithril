package bloodandmithril.persistence;

import static bloodandmithril.persistence.GameSaver.getSavePath;
import static bloodandmithril.persistence.PersistenceUtil.decode;
import static bloodandmithril.persistence.PersistenceUtil.encode;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;

/**
 * Responsible for persistence of {@link Parameters}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class ParameterPersistenceService {

	/** THE current set of {@link Parameters} */
	private static Parameters parameters = loadParameters();

	/** THE current set of {@link Parameters} */
	public synchronized static Parameters getParameters() {
		Parameters params = parameters == null ? loadParameters() : parameters;
		return params;
	}

	/** Loads and returns persisted parameters from disk */
	private synchronized static Parameters loadParameters() {
		try {
			FileHandle file = Gdx.files.local(getSavePath() + "/parameters.txt");
			parameters = decode(file);
			return parameters;
		} catch (Exception e) {
			Logger.loaderDebug("No parameters found on disk, using new parameter set", LogLevel.DEBUG);
			parameters = new Parameters();
			return parameters;
		}
	}


	/** Saves the {@link Parameters} */
	public synchronized static void saveParameters() {
		FileHandle file = Gdx.files.local(getSavePath() + "/parameters.txt");
		parameters.setSavedCameraPosition(ClientServerInterface.isClient() ? new Vector2(BloodAndMithrilClient.cam.position.x, BloodAndMithrilClient.cam.position.y) : new Vector2());
		file.writeString(encode(parameters), false);
	}
}
