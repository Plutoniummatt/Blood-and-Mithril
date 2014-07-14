package bloodandmithril.util;

public class Countdown implements SerializableFunction<Boolean> {
	private static final long serialVersionUID = -5761537304910257687L;
	private final long startTime;
	private final long duration;

	public Countdown(long duration) {
		this.duration = duration;
		this.startTime = System.currentTimeMillis();
	}

	@Override
	public Boolean call() {
		return System.currentTimeMillis() - duration >= startTime;
	}
}