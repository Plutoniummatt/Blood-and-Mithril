package bloodandmithril.persistence.prop;

import static bloodandmithril.persistence.GameSaver.savePath;
import static bloodandmithril.persistence.PersistenceUtil.decode;
import static bloodandmithril.util.Logger.loaderDebug;
import static com.badlogic.gdx.Gdx.files;

import java.util.concurrent.ConcurrentHashMap;

import bloodandmithril.core.Copyright;
import bloodandmithril.prop.Prop;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.world.Domain;

/**
 * Loads {@link Prop}s from disk
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class PropLoader {

	/**
	 * Loads all {@link Prop}s
	 */
	@SuppressWarnings("unchecked")
	public static void loadAll() {
		try {
			Domain.setProps((ConcurrentHashMap<Integer, Prop>) decode(files.local(savePath + "/world/props.txt")));
		} catch (Exception e) {
			loaderDebug("Failed to load props", LogLevel.WARN);
		}
	}
}