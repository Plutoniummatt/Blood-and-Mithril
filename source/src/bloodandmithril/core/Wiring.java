package bloodandmithril.core;

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

	private static Injector injector;

	private Wiring() {
	}

	public static Injector injector() {
		return injector;
	}

	public static void setupInjector(Module... module) {
		injector = Guice.createInjector(module);
	}
}
