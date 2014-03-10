package bloodandmithril.generation.tools;

import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.topography.Topography;

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
	public static Boundaries calculateBoundaries(
			boolean superStructure, 
			int startingChunkX, 
			int startingChunkY, 
			int maxWidth, 
			int maxHeight, 
			int maxStaticWidth, 
			int minStaticWidth, 
			int maxStaticHeight, 
			int minStaticHeight,
			Topography topography) {

		//left T shape

	    int left = startingChunkX;
	    int lTop = startingChunkY;
	    int lBottom = startingChunkY;
	    
		while(!topography.getStructures().structureExists(left - 1, startingChunkY, superStructure) && !topography.getChunkMap().doesChunkExist(left - 1, startingChunkY) && left > startingChunkX - maxWidth && left > minStaticWidth) {
			left--;
		}
		while(!topography.getStructures().structureExists(left, lTop + 1, superStructure) && !topography.getChunkMap().doesChunkExist(left, lTop + 1) && lTop < startingChunkY + maxHeight && lTop < maxStaticHeight) {
			lTop++;
		}
		while(!topography.getStructures().structureExists(left, lBottom - 1, superStructure) && !topography.getChunkMap().doesChunkExist(left, lBottom - 1) && lBottom > startingChunkY - maxHeight && lBottom > minStaticHeight) {
			lBottom--;
		}

		//right T shape

	    int right = startingChunkX;
	    int rTop = startingChunkY;
	    int rBottom = startingChunkY;

		while(!topography.getStructures().structureExists(right + 1, startingChunkY, superStructure) && !topography.getChunkMap().doesChunkExist(right + 1, startingChunkY) && right < startingChunkX + maxWidth && right < maxStaticWidth) {
			right++;
		}
		while(!topography.getStructures().structureExists(left, rTop + 1, superStructure) && !topography.getChunkMap().doesChunkExist(left, rTop + 1) && rTop < startingChunkY + maxHeight && rTop < maxStaticHeight) {
			rTop++;
		}
		while(!topography.getStructures().structureExists(left, rBottom - 1, superStructure) && !topography.getChunkMap().doesChunkExist(left, rBottom - 1) && rBottom > startingChunkY - maxHeight && rBottom > minStaticHeight) {
			rBottom--;
		}

		//top T shape

	    int top = startingChunkY;
	    int tLeft = startingChunkX;
	    int tRight = startingChunkX;

		while(!topography.getStructures().structureExists(startingChunkX, top + 1, superStructure) && !topography.getChunkMap().doesChunkExist(startingChunkX, top + 1) && top < startingChunkY + maxHeight && top < maxStaticHeight) {
			top++;
		}
		while(!topography.getStructures().structureExists(tLeft - 1, top, superStructure) && !topography.getChunkMap().doesChunkExist(tLeft - 1, top) && tLeft > startingChunkX - maxWidth && tLeft > minStaticWidth) {
			tLeft--;
		}
		while(!topography.getStructures().structureExists(tRight + 1, top, superStructure) && !topography.getChunkMap().doesChunkExist(tRight + 1, top) && tRight < startingChunkX + maxWidth && tRight < maxStaticWidth) {
			tRight++;
		}

		//bottom T shape

	    int bottom = startingChunkY;
	    int bLeft = startingChunkX;
	    int bRight = startingChunkX;

		while(!topography.getStructures().structureExists(startingChunkX, bottom - 1, superStructure) && !topography.getChunkMap().doesChunkExist(startingChunkX, bottom - 1) && bottom > startingChunkY - maxHeight && bottom > minStaticHeight) {
			bottom--;
		}
		while(!topography.getStructures().structureExists(bLeft - 1, bottom, superStructure) && !topography.getChunkMap().doesChunkExist(bLeft - 1, bottom) && bLeft > startingChunkX - maxWidth && bLeft > minStaticWidth) {
			bLeft--;
		}
		while(!topography.getStructures().structureExists(bRight + 1, bottom, superStructure) && !topography.getChunkMap().doesChunkExist(bRight + 1, bottom) && bRight < startingChunkX + maxWidth && bRight < maxStaticWidth) {
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
	public static Boundaries calculateBoundaries(
			boolean superStructure, 
			int startingChunkX, 
			int startingChunkY, 
			int maxWidth, 
			int maxHeight, 
			int maxStaticHeight, 
			int minStaticHeight,
			Topography topography) {

		//left T shape

	    int left = startingChunkX;
	    int lTop = startingChunkY;
	    int lBottom = startingChunkY;

		while(!topography.getStructures().structureExists(left - 1, startingChunkY, superStructure) && !topography.getChunkMap().doesChunkExist(left - 1, startingChunkY) && left > startingChunkX - maxWidth) {
			left--;
		}
		while(!topography.getStructures().structureExists(left, lTop + 1, superStructure) && !topography.getChunkMap().doesChunkExist(left, lTop + 1) && lTop < startingChunkY + maxHeight && lTop < maxStaticHeight) {
			lTop++;
		}
		while(!topography.getStructures().structureExists(left, lBottom - 1, superStructure) && !topography.getChunkMap().doesChunkExist(left, lBottom - 1) && lBottom > startingChunkY - maxHeight && lBottom > minStaticHeight) {
			lBottom--;
		}

		//right T shape

	    int right = startingChunkX;
	    int rTop = startingChunkY;
	    int rBottom = startingChunkY;

		while(!topography.getStructures().structureExists(right + 1, startingChunkY, superStructure) && !topography.getChunkMap().doesChunkExist(right + 1, startingChunkY) && right < startingChunkX + maxWidth) {
			right++;
		}
		while(!topography.getStructures().structureExists(left, rTop + 1, superStructure) && !topography.getChunkMap().doesChunkExist(left, rTop + 1) && rTop < startingChunkY + maxHeight && rTop < maxStaticHeight) {
			rTop++;
		}
		while(!topography.getStructures().structureExists(left, rBottom - 1, superStructure) && !topography.getChunkMap().doesChunkExist(left, rBottom - 1) && rBottom > startingChunkY - maxHeight && rBottom > minStaticHeight) {
			rBottom--;
		}

		//top T shape

	    int top = startingChunkY;
	    int tLeft = startingChunkX;
	    int tRight = startingChunkX;

		while(!topography.getStructures().structureExists(startingChunkX, top + 1, superStructure) && !topography.getChunkMap().doesChunkExist(startingChunkX, top + 1) && top < startingChunkY + maxHeight && top < maxStaticHeight) {
			top++;
		}
		while(!topography.getStructures().structureExists(tLeft - 1, top, superStructure) && !topography.getChunkMap().doesChunkExist(tLeft - 1, top) && tLeft > startingChunkX - maxWidth) {
			tLeft--;
		}
		while(!topography.getStructures().structureExists(tRight + 1, top, superStructure) && !topography.getChunkMap().doesChunkExist(tRight + 1, top) && tRight < startingChunkX + maxWidth) {
			tRight++;
		}

		//bottom T shape

	    int bottom = startingChunkY;
	    int bLeft = startingChunkX;
	    int bRight = startingChunkX;

		while(!topography.getStructures().structureExists(startingChunkX, bottom - 1, superStructure) && !topography.getChunkMap().doesChunkExist(startingChunkX, bottom - 1) && bottom > startingChunkY - maxHeight && bottom > minStaticHeight) {
			bottom--;
		}
		while(!topography.getStructures().structureExists(bLeft - 1, bottom, superStructure) && !topography.getChunkMap().doesChunkExist(bLeft - 1, bottom) && bLeft > startingChunkX - maxWidth) {
			bLeft--;
		}
		while(!topography.getStructures().structureExists(bRight + 1, bottom, superStructure) && !topography.getChunkMap().doesChunkExist(bRight + 1, bottom) && bRight < startingChunkX + maxWidth) {
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
	public static Boundaries calculateBoundaries(
			boolean 
			superStructure, 
			int startingChunkX, 
			int startingChunkY, 
			int maxWidth, 
			int maxHeight,
			Topography topography) {

		//left T shape

	    int left = startingChunkX;
	    int lTop = startingChunkY;
	    int lBottom = startingChunkY;

		while(!topography.getStructures().structureExists(left - 1, startingChunkY, superStructure) && !topography.getChunkMap().doesChunkExist(left - 1, startingChunkY) && left > startingChunkX - maxWidth) {
			left--;
		}
		while(!topography.getStructures().structureExists(left, lTop + 1, superStructure) && !topography.getChunkMap().doesChunkExist(left, lTop + 1) && lTop < startingChunkY + maxHeight) {
			lTop++;
		}
		while(!topography.getStructures().structureExists(left, lBottom - 1, superStructure) && !topography.getChunkMap().doesChunkExist(left, lBottom - 1) && lBottom > startingChunkY - maxHeight) {
			lBottom--;
		}

		//right T shape

	    int right = startingChunkX;
	    int rTop = startingChunkY;
	    int rBottom = startingChunkY;

		while(!topography.getStructures().structureExists(right + 1, startingChunkY, superStructure) && !topography.getChunkMap().doesChunkExist(right + 1, startingChunkY) && right < startingChunkX + maxWidth) {
			right++;
		}
		while(!topography.getStructures().structureExists(left, rTop + 1, superStructure) && !topography.getChunkMap().doesChunkExist(left, rTop + 1) && rTop < startingChunkY + maxHeight) {
			rTop++;
		}
		while(!topography.getStructures().structureExists(left, rBottom - 1, superStructure) && !topography.getChunkMap().doesChunkExist(left, rBottom - 1) && rBottom > startingChunkY - maxHeight) {
			rBottom--;
		}

		//top T shape

	    int top = startingChunkY;
	    int tLeft = startingChunkX;
	    int tRight = startingChunkX;

		while(!topography.getStructures().structureExists(startingChunkX, top + 1, superStructure) && !topography.getChunkMap().doesChunkExist(startingChunkX, top + 1) && top < startingChunkY + maxHeight) {
			top++;
		}
		while(!topography.getStructures().structureExists(tLeft - 1, top, superStructure) && !topography.getChunkMap().doesChunkExist(tLeft - 1, top) && tLeft > startingChunkX - maxWidth) {
			tLeft--;
		}
		while(!topography.getStructures().structureExists(tRight + 1, top, superStructure) && !topography.getChunkMap().doesChunkExist(tRight + 1, top) && tRight < startingChunkX + maxWidth) {
			tRight++;
		}

		//bottom T shape

	    int bottom = startingChunkY;
	    int bLeft = startingChunkX;
	    int bRight = startingChunkX;

		while(!topography.getStructures().structureExists(startingChunkX, bottom - 1, superStructure) && !topography.getChunkMap().doesChunkExist(startingChunkX, bottom - 1) && bottom > startingChunkY - maxHeight) {
			bottom--;
		}
		while(!topography.getStructures().structureExists(bLeft - 1, bottom, superStructure) && !topography.getChunkMap().doesChunkExist(bLeft - 1, bottom) && bLeft > startingChunkX - maxWidth) {
			bLeft--;
		}
		while(!topography.getStructures().structureExists(bRight + 1, bottom, superStructure) && !topography.getChunkMap().doesChunkExist(bRight + 1, bottom) && bRight < startingChunkX + maxWidth) {
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
