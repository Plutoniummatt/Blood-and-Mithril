package bloodandmithril.character;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;

/**
 * Enables something to speak via {@link SpeakService}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public interface Speaker {

	public default void speak(final String speech, final long duration) {
		Wiring.injector().getInstance(SpeakService.class).speak(this, speech, duration);
	}
}
