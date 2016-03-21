package bloodandmithril.core;

import static bloodandmithril.persistence.ConfigPersistenceService.getConfig;

import com.google.inject.Binder;
import com.google.inject.Module;

import bloodandmithril.control.Controls;

/**
 * Common {@link Module} installed by client and server
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class CommonModule implements Module {

	@Override
	public void configure(Binder binder) {
		binder.bind(Controls.class).toInstance(getConfig().getKeyMappings());
	}
}