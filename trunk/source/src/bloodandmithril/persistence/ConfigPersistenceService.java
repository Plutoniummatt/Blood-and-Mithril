package bloodandmithril.persistence;

import static bloodandmithril.persistence.PersistenceUtil.decode;
import static bloodandmithril.persistence.PersistenceUtil.encode;
import static bloodandmithril.persistence.PersistenceUtil.readFile;

import java.io.IOException;

/**
 * Persistence service for {@link Config}
 *
 * @author Matt
 */
public class ConfigPersistenceService {
	
	private static Config config = loadConfig();
	
	/**
	 * @return the single instance of config
	 */
	public static Config getConfig() {
		return config == null ? loadConfig() : config;
	}
	
	
	/**
	 * Loads config from disk
	 */
	private static Config loadConfig() {
		try {
			return decode(readFile("config.txt"));
		} catch (IOException e) {
			return new Config();
		}
	}
	
	
	/**
	 * Saves the config to disk
	 */
	public static void saveConfig() {
		PersistenceUtil.writeFile("config.txt", encode(config));
	}
}