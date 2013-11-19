package bloodandmithril.util;

import java.util.Random;

import bloodandmithril.Fortress;
import bloodandmithril.util.datastructure.Wrapper;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Util {

	/** Global random number generator */
	private static Random random = new Random();

	/**
	 * Creates an independent copy(clone) of the array.
	 *
	 * @param array The array to be cloned.
	 * @return An independent 'deep' structure clone of the array.
	 */
	public static <T> T[][] clone2DArray(T[][] array) {
	    int rows=array.length ;
	    //int rowIs=array[0].length ;

	    //clone the 'shallow' structure of array
	    T[][] newArray = array.clone();
	    //clone the 'deep' structure of array
	    for(int row=0;row<rows;row++){
	        newArray[row]= array[row].clone();
	    }

	    return newArray;
	}


	/**
	 * @return - The global instance of random.
	 */
	public synchronized static Random getRandom() {
		return random;
	}


	public static void draw(TextureRegion region, float x, float y, float angle) {
		Fortress.spriteBatch.draw(region, x, y, 0, 0, region.getRegionWidth(), region.getRegionHeight(), 1f, 1f, angle);
	}


	/**
	 * @return - A random {@link Object} from a list of {@link Object}s
	 */
	@SafeVarargs
	public static <T> T randomOneOf(T... objects) {
		return objects[random.nextInt(objects.length)];
	}


	/**
	 * @return - An {@link Object} from a list of {@link Object}s at a certain location
	 */
	@SafeVarargs
	public static <T> T get(int index, T... objects) {
		try {
			T t = objects[index];
			return t;
		} catch (ArrayIndexOutOfBoundsException e) {
			return objects[index % objects.length];
		}
	}


	/**
	 * @return - The first non-null object in the vararg
	 */
	@SafeVarargs
	public static <T> T firstNonNull(T... objects) {
		for (T object : objects) {
			if (object != null) {
				return object;
			}
		}
		return null;
	}


	/**
	 * Reformats a string to be able to fit inside a text input box.
	 */
	public static String fitToTextInputBox(String toPara, int length, int currentBeginningIndex) {
		return toPara.substring(currentBeginningIndex, Math.min(currentBeginningIndex + length / 9 - 3, toPara.length()));
	}


	/**
	 * Reformats a string to be able to fit inside a window.
	 */
	public static String fitToWindow(String toPara, int length, int maxLines) {
		String answer = "";
		String toChop = toPara;
		Wrapper<Integer> lineBeginIndex = new Wrapper<Integer>(new Integer(0));
		Wrapper<Integer> currentLine = new Wrapper<Integer>(new Integer(0));

		// Work out length of line
		int lineLength = length / 12;

		while (!toChop.isEmpty()) {
			int lineNumber = currentLine.t.intValue();

			answer = addWord(answer, getFirstWord(toChop, 1), lineLength, maxLines, lineBeginIndex, currentLine);

			if (lineNumber == currentLine.t) {
				toChop = removeFirstWord(toChop);
			}
		}

		return answer + (currentLine.t.equals(maxLines) ? "..." : "");
	}


	private static String addWord(String toAddTo, String word, int maxLength, int maxLines, Wrapper<Integer> lineBeginIndex, Wrapper<Integer> currentLine) {

		if (currentLine.t > maxLines - 1) {
			return toAddTo;
		}

		if ((toAddTo.substring(lineBeginIndex.t) + word).length() > maxLength) {
			lineBeginIndex.t = toAddTo.length() + 1;
			currentLine.t++;
			return toAddTo + "\n";
		} else {
			return toAddTo + word + " ";
		}
	}


	private static String removeFirstWord(String toRemoveFrom) {
		if (!toRemoveFrom.isEmpty()) {
			while (toRemoveFrom.startsWith(" ")) {
				toRemoveFrom = toRemoveFrom.substring(1);
			}

			if (toRemoveFrom.isEmpty()) {
				return "";
			}

			int length = getFirstWord(toRemoveFrom, 1).length();

			return toRemoveFrom.substring(length);
		}

		return "";
	}


	private static String getFirstWord(String string, int index) {
		while (string.startsWith(" ")) {
			string = string.substring(1);
		}

		try {
			if (string.substring(0, index).endsWith(" ")) {
				return string.substring(0, index - 1);
			} else {
				return getFirstWord(string, index + 1);
			}
		} catch (IndexOutOfBoundsException e) {
			return string.substring(0, index - 1);
		}
	}
}