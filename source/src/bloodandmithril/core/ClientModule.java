package bloodandmithril.core;

import static bloodandmithril.networking.ClientServerInterface.isServer;

import com.google.inject.Binder;
import com.google.inject.Module;

import bloodandmithril.playerinteraction.individual.api.IndividualAISupressionService;
import bloodandmithril.playerinteraction.individual.api.IndividualAttackOtherService;
import bloodandmithril.playerinteraction.individual.api.IndividualAttackRangedService;
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
import bloodandmithril.playerinteraction.individual.service.IndividualAttackRangedServiceClientImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualAttackRangedServiceServerImpl;
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
 * Client specific {@link Module}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class ClientModule implements Module {

	@Override
	public void configure(final Binder binder) {
		binder.bind(IndividualSelectionService.class).to(isServer() ? IndividualSelectionServiceServerImpl.class : IndividualSelectionServiceClientImpl.class);
		binder.bind(IndividualAISupressionService.class).to(isServer() ? IndividualAISupressionServiceServerImpl.class : IndividualAISupressionServiceClientImpl.class);
		binder.bind(IndividualWalkRunToggleService.class).to(isServer() ? IndividualWalkRunToggleServiceServerImpl.class : IndividualWalkRunToggleServiceClientImpl.class);
		binder.bind(IndividualToggleSpeakingService.class).to(isServer() ? IndividualToggleSpeakingServiceServerImpl.class : IndividualToggleSpeakingServiceClientImpl.class);
		binder.bind(IndividualTradeWithOtherService.class).to(isServer() ? IndividualTradeWithOtherServiceServerImpl.class : IndividualTradeWithOtherServiceClientImpl.class);
		binder.bind(IndividualFollowOtherService.class).to(isServer() ? IndividualFollowOtherServiceServerImpl.class : IndividualFollowOtherServiceClientImpl.class);
		binder.bind(IndividualChangeNicknameService.class).to(isServer() ? IndividualChangeNicknameServiceServerImpl.class : IndividualChangeNicknameServiceClientImpl.class);
		binder.bind(IndividualUpdateDescriptionService.class).to(isServer() ? IndividualUpdateDescriptionServiceServerImpl.class : IndividualUpdateDescriptionServiceClientImpl.class);
		binder.bind(IndividualAttackOtherService.class).to(isServer() ? IndividualAttackOtherServiceServerImpl.class : IndividualAttackOtherServiceClientImpl.class);
		binder.bind(IndividualAttackRangedService.class).to(isServer() ? IndividualAttackRangedServiceServerImpl.class : IndividualAttackRangedServiceClientImpl.class);
	}
}