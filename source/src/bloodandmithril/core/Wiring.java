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

	public static Injector injector;

	private Wiring() {
	}

	/**
	 * Sets up the injector
	 */
	public static void setup(Module... modules) {
		injector = Guice.createInjector(modules);
	}
}
