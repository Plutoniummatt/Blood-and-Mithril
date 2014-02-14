package bloodandmithril.util;

/**
 * Various static methods used for logging.
 *
 * @author Matt
 */
public class Logger {

	// Log levels
	private static final LogLevel loaderDebug = LogLevel.WARN;
	private static final LogLevel generalDebug = LogLevel.WARN;
	private static final LogLevel generationDebug = LogLevel.WARN;
	private static final LogLevel networkDebug = LogLevel.WARN;
	private static final LogLevel saverDebug = LogLevel.WARN;
	private static final LogLevel aiDebug = LogLevel.WARN;

	/**
	 * Prints a debug message
	 */
	public static void loaderDebug(String message, LogLevel level) {
		if (loaderDebug.value >= level.value) {
			System.out.println(message);
		}
	}


	/**
	 * Prints a debug message
	 */
	public static void generalDebug(String message, LogLevel level, Exception... es) {
		if (generalDebug.value >= level.value) {
			System.out.println(message);
			for (Exception e : es) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * Prints a debug message
	 */
	public static void generationDebug(String message, LogLevel level) {
		if (generationDebug.value >= level.value) {
			System.out.println(message);
		}
	}


	/**
	 * Prints a debug message
	 */
	public static void networkDebug(String message, LogLevel level) {
		if (networkDebug.value >= level.value) {
			System.out.println(message);
		} else if (level == LogLevel.OVERRIDE) {
			System.out.println(message);
		}
	}


	/**
	 * Prints a debug message
	 */
	public static void saverDebug(String message, LogLevel level) {
		if (saverDebug.value >= level.value) {
			System.out.println(message);
		}
	}


	/**
	 * Prints an AI debug message
	 */
	public static void aiDebug(String message, LogLevel level) {
		if (aiDebug.value >= level.value) {
			System.out.println(message);
		}
	}


	/**
	 * The Logging level.
	 *
	 * @author Matt
	 */
	public enum LogLevel {
		TRACE(4), DEBUG(3), INFO(2), WARN(1), OVERRIDE(0);
		public final int value;

		private LogLevel(int value) {
			this.value = value;
		}
	}


	/**
	 * Performs a task if in debug mode
	 */
	public static void debugTask(Task debugTask) {
		debugTask.execute();
	}
}
