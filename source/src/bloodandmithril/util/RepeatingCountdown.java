package bloodandmithril.util;

import bloodandmithril.core.Copyright;

/**
 * A repeating countdown timer
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class RepeatingCountdown implements SerializableFunction<Boolean> {
	private static final long serialVersionUID = -5370347149225786054L;
	private long startTime;
	private final long duration;

	public RepeatingCountdown(long duration) {
		this.duration = duration;
		this.startTime = System.currentTimeMillis();
	}

	@Override
	public Boolean call() {
		if (System.currentTimeMillis() - duration >= startTime) {
			startTime = System.currentTimeMillis();
			return true;
		}
		return false;
	}
}
