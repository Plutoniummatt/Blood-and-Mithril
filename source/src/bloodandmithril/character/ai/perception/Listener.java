package bloodandmithril.character.ai.perception;

import bloodandmithril.core.Copyright;

/**
 * The ability to sense sound
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public interface Listener {

	/**
	 * Listens to a {@link SoundStimulus}
	 */
	public void listen(SoundStimulus stimulus);
}