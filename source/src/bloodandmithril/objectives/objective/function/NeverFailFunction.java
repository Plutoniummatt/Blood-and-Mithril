package bloodandmithril.objectives.objective.function;

import bloodandmithril.core.Copyright;
import bloodandmithril.util.SerializableFunction;

/**
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class NeverFailFunction implements SerializableFunction<Boolean> {
	private static final long serialVersionUID = -4325383156490027000L;

	@Override
	public Boolean call() {
		return false;
	}
}