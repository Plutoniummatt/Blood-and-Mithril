package spritestar.persistence;

import static spritestar.persistence.PersistenceUtil.decode;
import static spritestar.persistence.PersistenceUtil.encode;
import spritestar.Fortress;
import spritestar.util.Logger;
import spritestar.util.Logger.LogLevel;
import spritestar.world.WorldState;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;

/**
 * Responsible for persistence of {@link Parameters}
 *
 * @author Matt
 */
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
			FileHandle file = Gdx.files.local(GameSaver.savePath + "/parameters.txt");
			parameters = decode(file);
			return parameters;
		} catch (Exception e) {
			Logger.loaderDebug("No parameters found on disk, using new parameter set", LogLevel.WARN);
			parameters = new Parameters();
			return parameters;
		}
	}


	/** Saves the {@link Parameters} */
	public synchronized static void saveParameters() {
		FileHandle file = Gdx.files.local(GameSaver.savePath + "/parameters.txt");
		parameters.setSavedCameraPosition(new Vector2(Fortress.cam.position.x, Fortress.cam.position.y));
		parameters.setCurrentEpoch(WorldState.currentEpoch);
		file.writeString(encode(parameters), false);
	}
}
