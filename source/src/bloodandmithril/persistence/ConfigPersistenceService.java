package bloodandmithril.persistence;

import static bloodandmithril.persistence.PersistenceUtil.decode;
import static bloodandmithril.persistence.PersistenceUtil.encode;
import static bloodandmithril.persistence.PersistenceUtil.readFile;

import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;

/**
 * Persistence service for {@link Config}
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2014")
public class ConfigPersistenceService {
	private Config config;

	@Inject
	public ConfigPersistenceService() {
		this.config = loadConfig();
	}

	/**
	 * @return the single instance of config
	 */
	public Config getConfig() {
		return config == null ? loadConfig() : config;
	}


	/**
	 * Loads config from disk
	 */
	private Config loadConfig() {
		try {
			return decode(readFile("config.txt"));
		} catch (IOException e) {
			return new Config();
		}
	}


	/**
	 * Saves the config to disk
	 */
	public void saveConfig() {
		PersistenceUtil.writeFile("config.txt", encode(config));
	}
}