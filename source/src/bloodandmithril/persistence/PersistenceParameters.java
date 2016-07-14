package bloodandmithril.persistence;

import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.persistence.GameSaver.PersistenceMetaData;

/**
 * Holds persistence meta-data
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class PersistenceParameters {

	/** File path for saved games */
	private String savePath;

	/** Name to use for saved game */
	private String savedGameName = null;

	/** The meta data of the most recently loaded saved game */
	private PersistenceMetaData mostRecentlyLoaded;


	public String getSavePath() {
		return savePath;
	}


	public void setSavePath(final String savePath) {
		this.savePath = savePath;
	}


	public String getSavedGameName() {
		return savedGameName;
	}


	public void setSavedGameName(final String savedGameName) {
		this.savedGameName = savedGameName;
	}


	public PersistenceMetaData getMostRecentlyLoaded() {
		return mostRecentlyLoaded;
	}


	public void setMostRecentlyLoaded(final PersistenceMetaData mostRecentlyLoaded) {
		this.mostRecentlyLoaded = mostRecentlyLoaded;
	}
}