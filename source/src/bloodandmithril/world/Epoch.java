package bloodandmithril.world;

import static java.lang.Math.exp;
import static java.lang.Math.floor;
import static java.lang.Math.pow;

import java.io.Serializable;

import bloodandmithril.core.Copyright;

/**
 * Epoch representing an instance in time.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public final class Epoch implements Serializable {
	private static final long serialVersionUID = -5123582798866732144L;

	/** Length of one day, in real minutes */
	private static final float lengthOfDay = 90;

	private float timeOfDay;
	public int dayOfMonth, monthOfYear, year;

	/**
	 * Constructor
	 */
	public Epoch(float time, int day, int month, int year) {
		this.timeOfDay = time;
		this.dayOfMonth = day;
		this.monthOfYear = month;
		this.year = year;
	}


	public final Epoch copy() {
		return new Epoch(timeOfDay, dayOfMonth, monthOfYear, year);
	}


	/**
	 * @param delta - To increment, in seconds
	 */
	public final void incrementTime(float delta) {
		float toIncrementBy = 24 * delta / lengthOfDay / 60f;
		float trialTime = timeOfDay + toIncrementBy;

		if (trialTime > 24f) {
			timeOfDay = toIncrementBy - (24f - timeOfDay);
			incrementDay();
		} else {
			timeOfDay = trialTime;
		}
	}


	/**
	 * @param delta - To increment, in seconds
	 */
	public final void incrementGameTime(float time) {
		float trialTime = timeOfDay + time;

		if (trialTime > 24f) {
			timeOfDay = time - (24f - timeOfDay);
			incrementDay();
		} else {
			timeOfDay = trialTime;
		}
	}


	public final boolean isLaterThan(Epoch other) {
		if (year > other.year) {
			return true;
		} else if (year == other.year) {
			if (monthOfYear > other.monthOfYear) {
				return true;
			} else if (monthOfYear == other.monthOfYear) {
				if (dayOfMonth > other.dayOfMonth) {
					return true;
				} else if (dayOfMonth == other.dayOfMonth) {
					if (timeOfDay > other.timeOfDay) {
						return true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}


	/**
	 * Increments the day
	 */
	public final void incrementDay() {
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
	public final float getTime() {
		return timeOfDay;
	}


	/**
	 * Increments the month.
	 */
	private final void incrementMonth() {
		if (monthOfYear == 12) {
			monthOfYear = 1;
			year++;
		} else {
			monthOfYear++;
		}
	}


	public final void setTimeOfDay(float time) {
		this.timeOfDay = time;
	}


	/**
	 * @return the current time of day as a string
	 */
	public static final String getTimeString(float time) {
		if (time > 24f) {
			time -= 24f;
		}

		String hour = floor(time) < 10 ? "0" + Integer.toString((int)floor(time)) : Integer.toString((int)floor(time));
		String minute = (int)((time - (float)floor(time)) * 60f) < 10 ? "0" + Integer.toString((int)((time - (float)floor(time)) * 60f)) : Integer.toString((int)((time - (float)floor(time)) * 60f));
		return hour + ":" + minute;
	}


	/**
	 * See {@link #getTimeString(Epoch)}
	 */
	public final String getTimeString() {
		return getTimeString(getTime());
	}


	/**
	 * @return the current date as a string
	 */
	public final String getDateString() {
		return Integer.toString(dayOfMonth) + "/" + Integer.toString(monthOfYear) + "/" + Integer.toString(year);
	}


	/**
	 * @return the daylight alpha
	 */
	public final float dayLight() {
		if (timeOfDay > 0f && timeOfDay < 8f) {
			return (float) exp(-pow(timeOfDay - 8f, 2));
		} else if (timeOfDay > 8f && timeOfDay < 16f) {
			return 1f;
		} else {
			return (float) exp(-pow(timeOfDay - 16f, 2));
		}
	}
}
