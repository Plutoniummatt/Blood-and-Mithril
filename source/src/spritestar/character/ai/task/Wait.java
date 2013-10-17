package spritestar.character.ai.task;

import spritestar.character.Individual;
import spritestar.character.ai.AITask;
import spritestar.world.GameWorld;

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
	private long systemTimeSinceLastUpdate;

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
