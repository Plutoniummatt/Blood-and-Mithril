package bloodandmithril.util;

import java.util.Random;

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
	public static Random getRandom() {
		return random;
	}


	/**
	 * @return - A random {@link Object} from a list of {@link Object}s
	 */
	@SafeVarargs
  public static <T> T randomOneOf(T... objects) {
		return objects[random.nextInt(objects.length)];
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
}
