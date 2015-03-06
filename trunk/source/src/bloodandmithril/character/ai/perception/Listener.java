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


	/**
	 * @return whether this {@link Listener} reacts to {@link SoundStimulus} even if the source of the stimulus is visible.
	 */
	public boolean reactIfVisible(SoundStimulus stimulus);
}