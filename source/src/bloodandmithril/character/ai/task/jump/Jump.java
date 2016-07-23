package bloodandmithril.character.ai.task.jump;

import com.badlogic.gdx.math.Vector2;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ExecutedBy;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.SerializableFunction;

/**
 * {@link AITask} for jumping
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@ExecutedBy(JumpExecutor.class)
public class Jump extends AITask {
	private static final long serialVersionUID = -3954194589753060040L;

	final SerializableFunction<Vector2> from;
	final Vector2 to;
	boolean jumped;

	/**
	 * Constructor
	 */
	public Jump(final IndividualIdentifier hostId, final SerializableFunction<Vector2> from, final Vector2 to) {
		super(hostId);
		this.from = from;
		this.to = to;
	}


	public Vector2 getDestination() {
		return to.cpy();
	}


	@Override
	public String getShortDescription() {
		return "Jumping";
	}
}