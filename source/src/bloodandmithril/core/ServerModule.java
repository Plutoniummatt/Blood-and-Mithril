package bloodandmithril.core;

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
import bloodandmithril.playerinteraction.individual.service.IndividualAISupressionServiceServerImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualAttackOtherServiceServerImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualAttackRangedServiceServerImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualChangeNicknameServiceServerImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualFollowOtherServiceServerImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualSelectionServiceServerImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualToggleSpeakingServiceServerImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualTradeWithOtherServiceServerImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualUpdateDescriptionServiceServerImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualWalkRunToggleServiceServerImpl;

/**
 * Server specific {@link Module}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class ServerModule implements Module {

	@Override
	public void configure(final Binder binder) {
		binder.bind(IndividualSelectionService.class).to(IndividualSelectionServiceServerImpl.class);
		binder.bind(IndividualAISupressionService.class).to(IndividualAISupressionServiceServerImpl.class);
		binder.bind(IndividualWalkRunToggleService.class).to(IndividualWalkRunToggleServiceServerImpl.class);
		binder.bind(IndividualToggleSpeakingService.class).to(IndividualToggleSpeakingServiceServerImpl.class);
		binder.bind(IndividualTradeWithOtherService.class).to(IndividualTradeWithOtherServiceServerImpl.class);
		binder.bind(IndividualFollowOtherService.class).to(IndividualFollowOtherServiceServerImpl.class);
		binder.bind(IndividualChangeNicknameService.class).to(IndividualChangeNicknameServiceServerImpl.class);
		binder.bind(IndividualUpdateDescriptionService.class).to(IndividualUpdateDescriptionServiceServerImpl.class);
		binder.bind(IndividualAttackOtherService.class).to(IndividualAttackOtherServiceServerImpl.class);
		binder.bind(IndividualAttackRangedService.class).to(IndividualAttackRangedServiceServerImpl.class);
	}
}