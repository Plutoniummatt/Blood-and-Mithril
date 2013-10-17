package spritestar.world;

import static java.lang.Math.exp;
import static java.lang.Math.floor;
import static java.lang.Math.pow;

import java.io.Serializable;

/**
 * Epoch representing an instance in time.
 *
 * @author Matt
 */
public class Epoch implements Serializable {
	private static final long serialVersionUID = -5123582798866732144L;
	
	private float timeOfDay;
	public int dayOfMonth, monthOfYear, year;
	
	
	/**
	 * Constructor
	 */
	public Epoch(float time, int day, int month, int year) {
		timeOfDay = time;
		dayOfMonth = day;
		monthOfYear = month;
		this.year = year;
	}
	
	
	/**
	 * @param delta - To increment
	 */
	public void incrementTime(float delta) {
		if (timeOfDay > 24f) {
			timeOfDay = 0;
			incrementDay();
		}
		timeOfDay += delta;
	}
	
	
	/**
	 * Increments the day
	 */
	private void incrementDay() {
		if (dayOfMonth == 30) {
			dayOfMonth = 1;
			incrementMonth();
		} else {
			dayOfMonth++;
		}
	}
	
	
	/**
	 * @return the time of day
	 */
	public float getTime() {
		return timeOfDay;
	}
	
	
	/**
	 * Increments the month.
	 */
	private void incrementMonth() {
		if (monthOfYear == 12) {
			monthOfYear = 1;
			year++;
		} else {
			monthOfYear++;
		}
	}
	

	/**
	 * @return the current time of day as a string
	 */
	public String getTimeString() {
		String hour = floor(timeOfDay) < 10 ? "0" + Integer.toString((int)floor(timeOfDay)) : Integer.toString((int)floor(timeOfDay));
		String minute = (int)((timeOfDay - (float)floor(timeOfDay)) * 60f) < 10 ? "0" + Integer.toString((int)((timeOfDay - (float)floor(timeOfDay)) * 60f)) : Integer.toString((int)((timeOfDay - (float)floor(timeOfDay)) * 60f));
		return hour + ":" + minute;
	}
	
	
	/**
	 * @return the current date as a string
	 */
	public String getDateString() {
		return Integer.toString(dayOfMonth) + "/" + Integer.toString(monthOfYear) + "/" + Integer.toString(year);
	}
	
	
	/**
	 * @return the daylight alpha
	 */
	public float dayLight() {
		if (timeOfDay > 0f && timeOfDay < 8f) {
			return (float) exp(-pow(timeOfDay - 8f, 2));
		} else if (timeOfDay > 6f && timeOfDay < 18f) {
			return 1f;
		} else {
			return (float) exp(-pow(timeOfDay - 18f, 2));
		}
	}
}
