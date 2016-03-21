package bloodandmithril.playerinteraction.individual.service;

import com.google.inject.Singleton;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.playerinteraction.individual.api.IndividualToggleSpeakingService;

/**
 * Server side implementation of {@link IndividualToggleSpeakingService}
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2015")
public class IndividualToggleSpeakingServiceServerImpl implements IndividualToggleSpeakingService {

	@Override
	public void setSpeaking(Individual individual, boolean speaking) {
		if (speaking) {
			individual.setShutUp(false);
			individual.speak("I will speak", 1000);
		} else {
			individual.speak("I will be quiet", 1000);
			individual.setShutUp(true);
		}
	}
}