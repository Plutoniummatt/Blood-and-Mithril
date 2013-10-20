package bloodandmithril.character.ai.task;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.world.GameWorld;

/**
 * {@link AITask} that instructs the {@link Individual} to wait for a specified amount of time
 *
 * @author Matt
 */
public class Wait extends AITask {
	private static final long serialVersionUID = 1131002096994485862L;

	/** Time to wait */
	private float time;

	/** Used for processing */
	private long systemTimeSinceLastUpdate = System.currentTimeMillis();

	/** {@link #execute()} will set this to true when the {@link #time} expires */
	private boolean complete = false;

	/**
	 * Constructor
	 */
	public Wait(Individual host, float time) {
		super(host.id);
		this.time = time;
	}


	@Override
	public void execute() {
		GameWorld.individuals.get(hostId.id).clearCommands();
		if (time < 0f) {
			complete = true;
			return;
		}
		time = time - (System.currentTimeMillis() - systemTimeSinceLastUpdate)/1000f;
		systemTimeSinceLastUpdate = System.currentTimeMillis();
	}


	@Override
	public String getDescription() {
		return "Waiting";
	}


	@Override
	public boolean isComplete() {
		return complete;
	}


	@Override
	public void uponCompletion() {
	}
}
