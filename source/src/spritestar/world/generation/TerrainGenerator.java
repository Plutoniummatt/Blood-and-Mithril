package spritestar.world.generation;

/**
 * Superclass which all terrain generators will implement
 *
 * @author Matt, Sam
 */
public abstract class TerrainGenerator {
	
	/**
	 * Takes the topography you want the generator to edit and the chunk
	 * coordidnates which will be generated.
	 */
	public abstract void generate(int x, int y);
}
