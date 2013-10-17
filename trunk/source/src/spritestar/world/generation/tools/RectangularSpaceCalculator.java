package spritestar.world.generation.tools;

import spritestar.util.datastructure.Boundaries;
import spritestar.world.generation.StructureMap;
import spritestar.world.topography.Topography;

/**
 * Calculates available space (space is defined as chunks that have not yet been generated, or have any structures associated with them)
 *
 * @author Sam
 */
public class RectangularSpaceCalculator {
	
	
	/**
	 * Calculates a square of space available
	 * 
	 * @param startingChunkX - The chunk coordinates to start calculating from.
	 * @param startingChunkY - The chunk coordinates to start calculating from.
	 * @param maxWidth - how far it can go in the x direction either way before stopping.
	 * @param maxHeight - how far it can go in the y direction either way before stopping.
	 * @param maxStaticWidth - chunk x coordinate of the minimum width the space can go to.
	 * @param minStaticWidth - chunk x coordinate of the maximum width the space can go to.
	 * @param maxStaticHeight - chunk y coordinate of the minimum height the space can go to.
	 * @param minStaticHeight - chunk y coordinate of the maximum height the space can go to.
	 * @return - Edges; Left, Right, Bottom, Top
	 */
	public static Boundaries calculateBoundaries(boolean superStructure, int startingChunkX, int startingChunkY, int maxWidth, int maxHeight, int maxStaticWidth, int minStaticWidth, int maxStaticHeight, int minStaticHeight) {

		//left T shape

	    int left = startingChunkX;
	    int lTop = startingChunkY;
	    int lBottom = startingChunkY;

		while(!StructureMap.doesStructureExist(left - 1, startingChunkY, superStructure) && !Topography.chunkMap.doesChunkExist(left - 1, startingChunkY) && left > startingChunkX - maxWidth && left > minStaticWidth) {
			left--;
		}
		while(!StructureMap.doesStructureExist(left, lTop + 1, superStructure) && !Topography.chunkMap.doesChunkExist(left, lTop + 1) && lTop < startingChunkY + maxHeight && lTop < maxStaticHeight) {
			lTop++;
		}
		while(!StructureMap.doesStructureExist(left, lBottom - 1, superStructure) && !Topography.chunkMap.doesChunkExist(left, lBottom - 1) && lBottom > startingChunkY - maxHeight && lBottom > minStaticHeight) {
			lBottom--;
		}

		//right T shape

	    int right = startingChunkX;
	    int rTop = startingChunkY;
	    int rBottom = startingChunkY;

		while(!StructureMap.doesStructureExist(right + 1, startingChunkY, superStructure) && !Topography.chunkMap.doesChunkExist(right + 1, startingChunkY) && right < startingChunkX + maxWidth && right < maxStaticWidth) {
			right++;
		}
		while(!StructureMap.doesStructureExist(left, rTop + 1, superStructure) && !Topography.chunkMap.doesChunkExist(left, rTop + 1) && rTop < startingChunkY + maxHeight && rTop < maxStaticHeight) {
			rTop++;
		}
		while(!StructureMap.doesStructureExist(left, rBottom - 1, superStructure) && !Topography.chunkMap.doesChunkExist(left, rBottom - 1) && rBottom > startingChunkY - maxHeight && rBottom > minStaticHeight) {
			rBottom--;
		}

		//top T shape

	    int top = startingChunkY;
	    int tLeft = startingChunkX;
	    int tRight = startingChunkX;

		while(!StructureMap.doesStructureExist(startingChunkX, top + 1, superStructure) && !Topography.chunkMap.doesChunkExist(startingChunkX, top + 1) && top < startingChunkY + maxHeight && top < maxStaticHeight) {
			top++;
		}
		while(!StructureMap.doesStructureExist(tLeft - 1, top, superStructure) && !Topography.chunkMap.doesChunkExist(tLeft - 1, top) && tLeft > startingChunkX - maxWidth && tLeft > minStaticWidth) {
			tLeft--;
		}
		while(!StructureMap.doesStructureExist(tRight + 1, top, superStructure) && !Topography.chunkMap.doesChunkExist(tRight + 1, top) && tRight < startingChunkX + maxWidth && tRight < maxStaticWidth) {
			tRight++;
		}

		//bottom T shape

	    int bottom = startingChunkY;
	    int bLeft = startingChunkX;
	    int bRight = startingChunkX;

		while(!StructureMap.doesStructureExist(startingChunkX, bottom - 1, superStructure) && !Topography.chunkMap.doesChunkExist(startingChunkX, bottom - 1) && bottom > startingChunkY - maxHeight && bottom > minStaticHeight) {
			bottom--;
		}
		while(!StructureMap.doesStructureExist(bLeft - 1, bottom, superStructure) && !Topography.chunkMap.doesChunkExist(bLeft - 1, bottom) && bLeft > startingChunkX - maxWidth && bLeft > minStaticWidth) {
			bLeft--;
		}
		while(!StructureMap.doesStructureExist(bRight + 1, bottom, superStructure) && !Topography.chunkMap.doesChunkExist(bRight + 1, bottom) && bRight < startingChunkX + maxWidth && bRight < maxStaticWidth) {
			bRight++;
		}

		// The bounds of the space
		int fLeft = Math.max(Math.max(tLeft, bLeft), left);
		int fRight = Math.min(Math.min(tRight, bRight), right);
		int fBottom = Math.max(Math.max(lBottom, rBottom), bottom);
		int fTop = Math.min(Math.min(lTop, rTop), top);

		return new Boundaries(fTop, fBottom, fLeft, fRight);
	}
	
	
	/**
	 * Calculates a square of space available
	 * 
	 * @param startingChunkX - The chunk coordinates to start calculating from.
	 * @param startingChunkY - The chunk coordinates to start calculating from.
	 * @param maxWidth - how far it can go in the x direction either way before stopping.
	 * @param maxHeight - how far it can go in the y direction either way before stopping.
	 * @param maxStaticHeight - chunk y coordinate of the minimum height the space can go to.
	 * @param minStaticHeight - chunk y coordinate of the maximum height the space can go to.
	 * @return - Edges; Left, Right, Bottom, Top
	 */
	public static Boundaries calculateBoundaries(boolean superStructure, int startingChunkX, int startingChunkY, int maxWidth, int maxHeight, int maxStaticHeight, int minStaticHeight) {

		//left T shape

	    int left = startingChunkX;
	    int lTop = startingChunkY;
	    int lBottom = startingChunkY;

		while(!StructureMap.doesStructureExist(left - 1, startingChunkY, superStructure) && !Topography.chunkMap.doesChunkExist(left - 1, startingChunkY) && left > startingChunkX - maxWidth) {
			left--;
		}
		while(!StructureMap.doesStructureExist(left, lTop + 1, superStructure) && !Topography.chunkMap.doesChunkExist(left, lTop + 1) && lTop < startingChunkY + maxHeight && lTop < maxStaticHeight) {
			lTop++;
		}
		while(!StructureMap.doesStructureExist(left, lBottom - 1, superStructure) && !Topography.chunkMap.doesChunkExist(left, lBottom - 1) && lBottom > startingChunkY - maxHeight && lBottom > minStaticHeight) {
			lBottom--;
		}

		//right T shape

	    int right = startingChunkX;
	    int rTop = startingChunkY;
	    int rBottom = startingChunkY;

		while(!StructureMap.doesStructureExist(right + 1, startingChunkY, superStructure) && !Topography.chunkMap.doesChunkExist(right + 1, startingChunkY) && right < startingChunkX + maxWidth) {
			right++;
		}
		while(!StructureMap.doesStructureExist(left, rTop + 1, superStructure) && !Topography.chunkMap.doesChunkExist(left, rTop + 1) && rTop < startingChunkY + maxHeight && rTop < maxStaticHeight) {
			rTop++;
		}
		while(!StructureMap.doesStructureExist(left, rBottom - 1, superStructure) && !Topography.chunkMap.doesChunkExist(left, rBottom - 1) && rBottom > startingChunkY - maxHeight && rBottom > minStaticHeight) {
			rBottom--;
		}

		//top T shape

	    int top = startingChunkY;
	    int tLeft = startingChunkX;
	    int tRight = startingChunkX;

		while(!StructureMap.doesStructureExist(startingChunkX, top + 1, superStructure) && !Topography.chunkMap.doesChunkExist(startingChunkX, top + 1) && top < startingChunkY + maxHeight && top < maxStaticHeight) {
			top++;
		}
		while(!StructureMap.doesStructureExist(tLeft - 1, top, superStructure) && !Topography.chunkMap.doesChunkExist(tLeft - 1, top) && tLeft > startingChunkX - maxWidth) {
			tLeft--;
		}
		while(!StructureMap.doesStructureExist(tRight + 1, top, superStructure) && !Topography.chunkMap.doesChunkExist(tRight + 1, top) && tRight < startingChunkX + maxWidth) {
			tRight++;
		}

		//bottom T shape

	    int bottom = startingChunkY;
	    int bLeft = startingChunkX;
	    int bRight = startingChunkX;

		while(!StructureMap.doesStructureExist(startingChunkX, bottom - 1, superStructure) && !Topography.chunkMap.doesChunkExist(startingChunkX, bottom - 1) && bottom > startingChunkY - maxHeight && bottom > minStaticHeight) {
			bottom--;
		}
		while(!StructureMap.doesStructureExist(bLeft - 1, bottom, superStructure) && !Topography.chunkMap.doesChunkExist(bLeft - 1, bottom) && bLeft > startingChunkX - maxWidth) {
			bLeft--;
		}
		while(!StructureMap.doesStructureExist(bRight + 1, bottom, superStructure) && !Topography.chunkMap.doesChunkExist(bRight + 1, bottom) && bRight < startingChunkX + maxWidth) {
			bRight++;
		}

		// The bounds of the space
		int fLeft = Math.max(Math.max(tLeft, bLeft), left);
		int fRight = Math.min(Math.min(tRight, bRight), right);
		int fBottom = Math.max(Math.max(lBottom, rBottom), bottom);
		int fTop = Math.min(Math.min(lTop, rTop), top);

		return new Boundaries(fTop, fBottom, fLeft, fRight);
	}
	
	
	/**
	 * Calculates a square of space available
	 * 
	 * @param startingChunkX - The chunk coordinates to start calculating from.
	 * @param startingChunkY - The chunk coordinates to start calculating from.
	 * @param maxHeight - how far it can go in the x direction either way before stopping.
	 * @param maxWidth - how far it can go in the y direction either way before stopping.
	 * @param minStaticHeight - chunk y coordinate of the maximum height the space can go to.
	 * @param maxStaticHeight - chunk y coordinate of the minimum height the space can go to.
	 * @return - Edges; Left, Right, Bottom, Top
	 */
	public static Boundaries calculateBoundaries(boolean superStructure, int startingChunkX, int startingChunkY, int maxWidth, int maxHeight) {

		//left T shape

	    int left = startingChunkX;
	    int lTop = startingChunkY;
	    int lBottom = startingChunkY;

		while(!StructureMap.doesStructureExist(left - 1, startingChunkY, superStructure) && !Topography.chunkMap.doesChunkExist(left - 1, startingChunkY) && left > startingChunkX - maxWidth) {
			left--;
		}
		while(!StructureMap.doesStructureExist(left, lTop + 1, superStructure) && !Topography.chunkMap.doesChunkExist(left, lTop + 1) && lTop < startingChunkY + maxHeight) {
			lTop++;
		}
		while(!StructureMap.doesStructureExist(left, lBottom - 1, superStructure) && !Topography.chunkMap.doesChunkExist(left, lBottom - 1) && lBottom > startingChunkY - maxHeight) {
			lBottom--;
		}

		//right T shape

	    int right = startingChunkX;
	    int rTop = startingChunkY;
	    int rBottom = startingChunkY;

		while(!StructureMap.doesStructureExist(right + 1, startingChunkY, superStructure) && !Topography.chunkMap.doesChunkExist(right + 1, startingChunkY) && right < startingChunkX + maxWidth) {
			right++;
		}
		while(!StructureMap.doesStructureExist(left, rTop + 1, superStructure) && !Topography.chunkMap.doesChunkExist(left, rTop + 1) && rTop < startingChunkY + maxHeight) {
			rTop++;
		}
		while(!StructureMap.doesStructureExist(left, rBottom - 1, superStructure) && !Topography.chunkMap.doesChunkExist(left, rBottom - 1) && rBottom > startingChunkY - maxHeight) {
			rBottom--;
		}

		//top T shape

	    int top = startingChunkY;
	    int tLeft = startingChunkX;
	    int tRight = startingChunkX;

		while(!StructureMap.doesStructureExist(startingChunkX, top + 1, superStructure) && !Topography.chunkMap.doesChunkExist(startingChunkX, top + 1) && top < startingChunkY + maxHeight) {
			top++;
		}
		while(!StructureMap.doesStructureExist(tLeft - 1, top, superStructure) && !Topography.chunkMap.doesChunkExist(tLeft - 1, top) && tLeft > startingChunkX - maxWidth) {
			tLeft--;
		}
		while(!StructureMap.doesStructureExist(tRight + 1, top, superStructure) && !Topography.chunkMap.doesChunkExist(tRight + 1, top) && tRight < startingChunkX + maxWidth) {
			tRight++;
		}

		//bottom T shape

	    int bottom = startingChunkY;
	    int bLeft = startingChunkX;
	    int bRight = startingChunkX;

		while(!StructureMap.doesStructureExist(startingChunkX, bottom - 1, superStructure) && !Topography.chunkMap.doesChunkExist(startingChunkX, bottom - 1) && bottom > startingChunkY - maxHeight) {
			bottom--;
		}
		while(!StructureMap.doesStructureExist(bLeft - 1, bottom, superStructure) && !Topography.chunkMap.doesChunkExist(bLeft - 1, bottom) && bLeft > startingChunkX - maxWidth) {
			bLeft--;
		}
		while(!StructureMap.doesStructureExist(bRight + 1, bottom, superStructure) && !Topography.chunkMap.doesChunkExist(bRight + 1, bottom) && bRight < startingChunkX + maxWidth) {
			bRight++;
		}

		// The bounds of the space
		int fLeft = Math.max(Math.max(tLeft, bLeft), left);
		int fRight = Math.min(Math.min(tRight, bRight), right);
		int fBottom = Math.max(Math.max(lBottom, rBottom), bottom);
		int fTop = Math.min(Math.min(lTop, rTop), top);

		return new Boundaries(fTop, fBottom, fLeft, fRight);
	}
}
