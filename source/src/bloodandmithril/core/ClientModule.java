package bloodandmithril.core;

import com.google.inject.Binder;
import com.google.inject.Module;

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
import bloodandmithril.playerinteraction.individual.service.IndividualAttackOtherServiceClientImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualChangeNicknameServiceClientImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualFollowOtherServiceClientImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualSelectionServiceClientImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualToggleSpeakingServiceClientImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualTradeWithOtherServiceClientImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualUpdateDescriptionServiceClientImpl;
import bloodandmithril.playerinteraction.individual.service.IndividualWalkRunToggleServiceClientImpl;

/**
 * Client specific {@link Module}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class ClientModule implements Module {

	@Override
	public void configure(Binder binder) {
		binder.bind(IndividualSelectionService.class).to(IndividualSelectionServiceClientImpl.class);
		binder.bind(IndividualAISupressionService.class).to(IndividualAISupressionServiceClientImpl.class);
		binder.bind(IndividualWalkRunToggleService.class).to(IndividualWalkRunToggleServiceClientImpl.class);
		binder.bind(IndividualToggleSpeakingService.class).to(IndividualToggleSpeakingServiceClientImpl.class);
		binder.bind(IndividualTradeWithOtherService.class).to(IndividualTradeWithOtherServiceClientImpl.class);
		binder.bind(IndividualFollowOtherService.class).to(IndividualFollowOtherServiceClientImpl.class);
		binder.bind(IndividualChangeNicknameService.class).to(IndividualChangeNicknameServiceClientImpl.class);
		binder.bind(IndividualUpdateDescriptionService.class).to(IndividualUpdateDescriptionServiceClientImpl.class);
		binder.bind(IndividualAttackOtherService.class).to(IndividualAttackOtherServiceClientImpl.class);
	}
}