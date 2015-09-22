package bloodandmithril.character.ai.task;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;

/**
 * Instructs {@link Individual} to speak
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class Speak extends AITask {
	private static final long serialVersionUID = -5210580892146755047L;
	private String text;
	private long duration;
	private boolean spoken = false;

	/**
	 * @param Constructor
	 */
	public Speak(Individual host, String text, long duration) {
		super(host.getId());
		this.text = text;
		this.duration = duration;
	}


	@Override
	public String getShortDescription() {
		return "Speaking";
	}


	@Override
	public boolean isComplete() {
		return spoken;
	}


	@Override
	public boolean uponCompletion() {
		return false;
	}


	@Override
	public void execute(float delta) {
		if (!spoken) {
			getHost().speak(text, duration);
			spoken = true;
		}
	}


	@Override
	public String getDetailedDescription() {
		return getHost().getId().getSimpleName() + " says \"" + text + "\"";
	}
}