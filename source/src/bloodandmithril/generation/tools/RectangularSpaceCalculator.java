package bloodandmithril.generation.tools;

import bloodandmithril.core.Copyright;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.topography.Topography;

/**
 * Calculates available space (space is defined as chunks that have not yet been generated, or have any structures associated with them)
 *
 * @author Sam
 */
@Copyright("Matthew Peck 2014")
public class RectangularSpaceCalculator {

	/**
	 * Calculates a square of space available
	 *
	 * @param startingChunkX - The chunk coordinates to start calculating from.
	 * @param startingChunkY - The chunk coordinates to start calculating from.
	 * @param maxWidth - how far it can go in the x direction either way before stopping.
	 * @param maxHeight - how far it can go in the y direction either way before stopping.
	 * @param ceiling - chunk y coordinate of the ceiling of the confinement.
	 * @param floor - chunk y coordinate of the floor of the confinement.
	 * @return - Edges; Left, Right, Bottom, Top
	 */
	public static Boundaries calculateBoundariesConfineWithinTwoHeights(
			boolean superStructure,
			int startingChunkX,
			int startingChunkY,
			int maxWidth,
			int maxHeight,
			int ceiling,
			int floor,
			Topography topography) {

		//left T shape

	    int left = startingChunkX;
	    int lTop = startingChunkY;
	    int lBottom = startingChunkY;

		while(!topography.getStructures().structureExists(left - 1, startingChunkY, superStructure) && !topography.getChunkMap().doesChunkExist(left - 1, startingChunkY) && left > startingChunkX - maxWidth) {
			left--;
		}
		while(!topography.getStructures().structureExists(left, lTop + 1, superStructure) && !topography.getChunkMap().doesChunkExist(left, lTop + 1) && lTop < startingChunkY + maxHeight && lTop < ceiling) {
			lTop++;
		}
		while(!topography.getStructures().structureExists(left, lBottom - 1, superStructure) && !topography.getChunkMap().doesChunkExist(left, lBottom - 1) && lBottom > startingChunkY - maxHeight && lBottom > floor) {
			lBottom--;
		}

		//right T shape

	    int right = startingChunkX;
	    int rTop = startingChunkY;
	    int rBottom = startingChunkY;

		while(!topography.getStructures().structureExists(right + 1, startingChunkY, superStructure) && !topography.getChunkMap().doesChunkExist(right + 1, startingChunkY) && right < startingChunkX + maxWidth) {
			right++;
		}
		while(!topography.getStructures().structureExists(left, rTop + 1, superStructure) && !topography.getChunkMap().doesChunkExist(left, rTop + 1) && rTop < startingChunkY + maxHeight && rTop < ceiling) {
			rTop++;
		}
		while(!topography.getStructures().structureExists(left, rBottom - 1, superStructure) && !topography.getChunkMap().doesChunkExist(left, rBottom - 1) && rBottom > startingChunkY - maxHeight && rBottom > floor) {
			rBottom--;
		}

		//top T shape

	    int top = startingChunkY;
	    int tLeft = startingChunkX;
	    int tRight = startingChunkX;

		while(!topography.getStructures().structureExists(startingChunkX, top + 1, superStructure) && !topography.getChunkMap().doesChunkExist(startingChunkX, top + 1) && top < startingChunkY + maxHeight && top < ceiling) {
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

		while(!topography.getStructures().structureExists(startingChunkX, bottom - 1, superStructure) && !topography.getChunkMap().doesChunkExist(startingChunkX, bottom - 1) && bottom > startingChunkY - maxHeight && bottom > floor) {
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
	 * Calculates a square of space available, not caring about any boundary restrictions
	 *
	 * @param startingChunkX - The chunk coordinates to start calculating from.
	 * @param startingChunkY - The chunk coordinates to start calculating from.
	 * @param maxHeight - how far it can go in the x direction either way before stopping.
	 * @param maxWidth - how far it can go in the y direction either way before stopping.
	 * @return - Edges; Left, Right, Bottom, Top
	 */
	public static Boundaries calculateBoundariesForUnderground(
			boolean superStructure,
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
