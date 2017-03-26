package bloodandmithril.core;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import bloodandmithril.control.Controls;
import bloodandmithril.persistence.ConfigPersistenceService;

/**
 * Common {@link Module} installed by client and server
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class CommonModule implements Module {

	@Override
	public void configure(Binder binder) {
	}
	
	
	@Provides
	@Singleton
	public Controls provideControls(ConfigPersistenceService configPersistenceService) {
		return configPersistenceService.getConfig().getKeyMappings();
	}
}