package bloodandmithril.character;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.ai.AIProcessor.ReturnIndividualPosition;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.ui.UserInterface;

/**
 * Service to call to make a {@link Speaker} speak
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class SpeakService {

	@Inject private UserInterface userInterface;

	public void speak(final Speaker speaker, final String speech, final long duration) {
		if (speaker instanceof Individual) {
			final Individual indi = (Individual) speaker;

			if (!indi.isAlive() || indi.isShutUp() || indi.getSpeakTimer() > 0f) {
				return;
			}

			if (ClientServerInterface.isServer()) {
				userInterface.addTextBubble(
					speech,
					new ReturnIndividualPosition(indi),
					duration,
					0,
					(int) (indi.getHeight() * 1.3f)
				);
				indi.setSpeakTimer(duration / 1000f);
			}
		}
	}
}
