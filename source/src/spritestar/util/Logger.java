package spritestar.util;

/**
 * Various static methods used for logging.
 *
 * @author Matt
 */
public class Logger {

	/**
	 * Prints a debug message
	 */
	public static void loaderDebug(String message, LogLevel level) {
		if (System.getProperty("loaderDebug") != null && LogLevel.valueOf(System.getProperty("loaderDebug")).value >= level.value) {
			System.out.println(message);
		}
	}
	
	
	/**
	 * Prints a debug message
	 */
	public static void generalDebug(String message, LogLevel level) {
		if (System.getProperty("generalDebug") != null && LogLevel.valueOf(System.getProperty("generalDebug")).value >= level.value) {
			System.out.println(message);
		}
	}
	
	
	/**
	 * Prints a debug message
	 */
	public static void saverDebug(String message, LogLevel level) {
		if (System.getProperty("saverDebug") != null && LogLevel.valueOf(System.getProperty("saverDebug")).value >= level.value) {
			System.out.println(message);
		}
	}
	
	
	/**
	 * Prints an AI debug message
	 */
	public static void aiDebug(String message, LogLevel level) {
		if (System.getProperty("aiDebug") != null && LogLevel.valueOf(System.getProperty("aiDebug")).value >= level.value) {
			System.out.println(message);
		}
	}
	
	
	/**
	 * The Logging level.
	 *
	 * @author Matt
	 */
	public enum LogLevel {
		TRACE(4), DEBUG(3), INFO(2), WARN(1);
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
