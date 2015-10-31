package bloodandmithril.core;

import static bloodandmithril.networking.ClientServerInterface.isServer;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import bloodandmithril.playerinteraction.individual.api.IndividualAISupressionService;
import bloodandmithril.playerinteraction.individual.api.IndividualAttackOtherService;
import bloodandmithril.playerinteraction.individual.api.IndividualChangeNicknameService;
import bloodandmithril.playerinteraction.individual.api.IndividualFollowOtherService;
import bloodandmithril.playerinteraction.individual.api.IndividualSelectionService;
import bloodandmithril.playerinteraction.individual.api.IndividualToggleSpeakingService;
import bloodandmithril.playerinteraction.individual.api.IndividualTradeWithOtherService;
import bloodandmithril.playerinteraction.individual.api.IndividualUpdateDescriptionService;
import bloodandmithril.playerinteraction.individual.api.IndividualWalkRunToggleService;
import bloodandmithril.playerinteraction.individual.service.IndividualAISupressionServiceClientImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualAISupressionServiceServerImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualAttackOtherServiceClientImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualAttackOtherServiceServerImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualChangeNicknameServiceClientImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualChangeNicknameServiceServerImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualFollowOtherServiceClientImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualFollowOtherServiceServerImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualSelectionServiceClientImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualSelectionServiceServerImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualToggleSpeakingServiceClientImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualToggleSpeakingServiceServerImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualTradeWithOtherServiceClientImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualTradeWithOtherServiceServerImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualUpdateDescriptionServiceClientImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualUpdateDescriptionServiceServerImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualWalkRunToggleServiceClientImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualWalkRunToggleServiceServerImpl;

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
			
			@Provides
			@Singleton
			public IndividualWalkRunToggleService provideIndividualWalkRunToggleService() {
				if (isServer()) {
					return new IndividualWalkRunToggleServiceServerImpl();
				} else {
					return new IndividualWalkRunToggleServiceClientImpl();
				}
			}
			
			@Provides
			@Singleton
			public IndividualToggleSpeakingService provideIndividualToggleSpeakingService() {
				if (isServer()) {
					return new IndividualToggleSpeakingServiceServerImpl();
				} else {
					return new IndividualToggleSpeakingServiceClientImpl();
				}
			}
			
			@Provides
			@Singleton
			public IndividualTradeWithOtherService provideIndividualTradeWithOtherService() {
				if (isServer()) {
					return new IndividualTradeWithOtherServiceServerImpl();
				} else {
					return new IndividualTradeWithOtherServiceClientImpl();
				}
			}
			
			@Provides
			@Singleton
			public IndividualFollowOtherService provideIndividualFollowOtherService() {
				if (isServer()) {
					return new IndividualFollowOtherServiceServerImpl();
				} else {
					return new IndividualFollowOtherServiceClientImpl();
				}
			}
			
			@Provides
			@Singleton
			public IndividualChangeNicknameService provideIndividualChangeNicknameService() {
				if (isServer()) {
					return new IndividualChangeNicknameServiceServerImpl();
				} else {
					return new IndividualChangeNicknameServiceClientImpl();
				}
			}
			
			@Provides
			@Singleton
			public IndividualUpdateDescriptionService provideIndividualUpdateDescriptionService() {
				if (isServer()) {
					return new IndividualUpdateDescriptionServiceServerImpl();
				} else {
					return new IndividualUpdateDescriptionServiceClientImpl();
				}
			}
			
			@Provides
			@Singleton
			public IndividualAttackOtherService provideIndividualAttackOtherService() {
				if (isServer()) {
					return new IndividualAttackOtherServiceServerImpl();
				} else {
					return new IndividualAttackOtherServiceClientImpl();
				}
			}
		});
	}
}
