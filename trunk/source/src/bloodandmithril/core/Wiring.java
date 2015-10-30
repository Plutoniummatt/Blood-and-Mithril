package bloodandmithril.core;

import static bloodandmithril.networking.ClientServerInterface.isServer;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import bloodandmithril.playerinteraction.individual.api.IndividualAISupressionService;
import bloodandmithril.playerinteraction.individual.api.IndividualSelectionService;
import bloodandmithril.playerinteraction.individual.service.IndividualAISupressionServiceClientImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualAISupressionServiceServerImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualSelectionServiceClientImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualSelectionServiceServerImpl;

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

	public static void setupInjector() {
		injector = Guice.createInjector(new Module() {
			@Override
			public void configure(Binder binder) {
			}

			@Provides
			@Singleton
			public IndividualSelectionService provideIndividualSelectionService() {
				if (isServer()) {
					return new IndividualSelectionServiceServerImpl();
				} else {
					return new IndividualSelectionServiceClientImpl();
				}
			}

			@Provides
			@Singleton
			public IndividualAISupressionService provideIndividualAISupressionService() {
				if (isServer()) {
					return new IndividualAISupressionServiceServerImpl();
				} else {
					return new IndividualAISupressionServiceClientImpl();
				}
			}
		});
	}
}
