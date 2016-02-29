package bloodandmithril.util;

import static bloodandmithril.core.BloodAndMithrilClient.getGraphics;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.base.Function;
import com.google.common.collect.Sets;

import bloodandmithril.core.Copyright;
import bloodandmithril.util.datastructure.Wrapper;

@Copyright("Matthew Peck 2014")
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
	    int rows = array.length ;

	    //clone the 'shallow' structure of array
	    T[][] newArray = array.clone();

	    //clone the 'deep' structure of array
	    for(int row = 0 ; row < rows; row++){
	        newArray[row] = array[row].clone();
	    }

	    return newArray;
	}


	/**
	 * @return - The global instance of random.
	 */
	public static Random getRandom() {
		return random;
	}


	/**
	 * @return - The truncated string appended with ...
	 */
	public static String truncate(String toTruncate, int length) {
		if (toTruncate.length() < length) {
			return toTruncate;
		} else {
			return toTruncate.substring(0, length) + "...";
		}
	}


	public static void draw(TextureRegion region, float x, float y, float angle) {
		getGraphics().getSpriteBatch().draw(region, x, y, 0, 0, region.getRegionWidth(), region.getRegionHeight(), 1f, 1f, angle);
	}


	public static boolean roll(float probability) {
		return random.nextFloat() < probability;
	}


	public static <A, B> Set<B> transformSet(Set<A> setOfA, Function<A, B> transformationFunction) {
		HashSet<B> newHashSet = Sets.newHashSet();

		for (A a : setOfA) {
			if (!newHashSet.add(transformationFunction.apply(a))) {
				throw new RuntimeException("Transformation function can not transform all elements of " + a.getClass().getSimpleName() + " uniquely.");
			}
		}

		return newHashSet;
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
	public static String fitToTextInputBox(String toPara, int length, int currentBeginningIndex, boolean renderCursor) {
		return toPara.substring(currentBeginningIndex, Math.min(currentBeginningIndex + length / 12 - 3, toPara.length())) +
		       (currentBeginningIndex + length / 12 - 3 < toPara.length() ? "..." : renderCursor ? "|" : "");
	}


	/**
	 * Reformats a string to be able to fit inside a window.
	 *
	 * Use {@link TextBounds} instead
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


	public static float round2dp(float toRound) {
		return Float.parseFloat(String.format("%.2f", toRound));
	}


	/**
	 * Class to hold some static {@link Color}s
	 *
	 * @author Matt
	 */
	public static class Colors {
		public static final Color DARK_RED = new Color(0.5f, 0f, 0f, 1f);
		public static final Color UI_DARK_ORANGE = new Color(0.8f, 0.6f, 0.0f, 1f);
		public static final Color UI_GRAY = new Color(0.8f, 0.8f, 0.8f, 1f);
		public static final Color UI_DARK_GRAY = new Color(0.55f, 0.55f, 0.55f, 1f);
		public static final Color UI_DARKER_GRAY = new Color(0.25f, 0.25f, 0.25f, 1f);
		public static final Color UI_DARK_BLUE = new Color(0f, 0f, 0.6f, 1f);
		public static final Color UI_DARK_PURPLE = new Color(0.8f, 0f, 0.6f, 1f);
		public static final Color UI_DARK_GREEN = new Color(0f, 0.5f, 0f, 1f);
		public static final Color UI_DARK_PURPLE_INACTIVE = new Color(0.45f, 0f, 0.32f, 0.6f);


		public static final Color ACID = new Color(0f, 0.5f, 0f, 0.85f);
		public static final Color WATER = new Color(0f, 0.3f, 1f, 0.8f);
		public static final Color BLOOD = new Color(0.45f, 0.0f, 0f, 1f);
		public static final Color MILK = new Color(1f, 0.98f, 0.96f, 1f);
		public static final Color OIL = new Color(0.08f, 0.035f, 0f, 1f);

		public static final Color DARK_SMOKE = new Color(0f, 0f, 0f, 0.9f);
		public static final Color LIGHT_SMOKE = new Color(0.1f, 0.1f, 0.1f, 0.35f);
		public static final Color FIRE_START = new Color(0.7f, 0.4f, 0f, 1.0f);
		public static final Color FIRE_END = new Color(0.85f, 0.2f, 0f, 1.0f);

		/**
		 * @return A {@link Color} with an adjusted alpha value.
		 */
		public static Color modulateAlpha(Color color, float alphaFactor) {
			Color toReturn = new Color(color);
			toReturn.a = toReturn.a * alphaFactor;

			return toReturn;
		}


		/**
		 * @return A {@link Color} with an adjusted alpha value.
		 */
		public static Color modulateAlpha(SerializableColor color, float alphaFactor) {
			Color toReturn = new Color(color.r, color.g, color.b, color.a);
			toReturn.a = toReturn.a * alphaFactor;

			return toReturn;
		}


		public static Color randomColor() {
			return new Color(
				getRandom().nextFloat(),
				getRandom().nextFloat(),
				getRandom().nextFloat(),
				1f
			);
		}


		public static Color lightColor() {
			return new Color(
				getRandom().nextFloat() / 2f + 0.5f,
				getRandom().nextFloat() / 2f + 0.5f,
				getRandom().nextFloat() / 2f + 0.5f,
				1f
			);
		}


		public static Color lightSkinColor() {
			return new Color(
				getRandom().nextFloat() / 5f + 0.8f,
				getRandom().nextFloat() / 20f + 0.8f,
				getRandom().nextFloat() / -7f + 0.8f,
				1f
			);
		}
	}
}