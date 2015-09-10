package bloodandmithril.core;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Code wiring utilities
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class Wiring {

	public static Injector injector = Guice.createInjector(new Module() {
		@Override
		public void configure(Binder binder) {
		}
	});
}
