package bloodandmithril.world.topography.tile;

import java.io.Serializable;

import bloodandmithril.util.datastructure.BinaryTree;
import bloodandmithril.world.topography.ChunkMap;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.tiles.brick.YellowBrickPlatform;
import bloodandmithril.world.topography.tile.tiles.brick.YellowBrickTile;
import bloodandmithril.world.topography.tile.tiles.glass.ClearGlassTile;
import bloodandmithril.world.topography.tile.tiles.sedimentary.YellowSandTile;
import bloodandmithril.world.topography.tile.tiles.soil.DryDirtTile;
import bloodandmithril.world.topography.tile.tiles.soil.StandardSoilTile;
import bloodandmithril.world.topography.tile.tiles.stone.GraniteTile;
import bloodandmithril.world.topography.tile.tiles.stone.SandStoneTile;


/**
 * A FUCKING TILE!
 *
 * @author Matt
 */
public abstract class Tile implements Serializable {
	private static final long serialVersionUID = -2331827246047876705L;

	/** Binary tree structure for fast calculation of tile orientation */
	public static BinaryTree<Orientation> orientationTree = new BinaryTree<Tile.Orientation>(4,
			Orientation.C, Orientation.PL, Orientation.PR, Orientation.H,
			Orientation.PT, Orientation.TL, Orientation.TR, Orientation.TM,
			Orientation.PB, Orientation.BL, Orientation.BR, Orientation.BM,
			Orientation.V, Orientation.L, Orientation.R, Orientation.M);


	/**
	 * The orientation of the tile
	 *
	 * @author Matt
	 */
	public enum Orientation {
		TL, TM, TR, L, M, R, BL, BM, BR, PL, PT, PR, PB, V, H, C
	}

	/** The {@link Orientation} of the tile */
	private Orientation orientation;

	/** Whether this is a platform tile */
	public final boolean isPlatformTile;

	/** Whether this is a Stair tile */
	protected boolean isStair = false;

	/**
	 * Protected constructor
	 */
	protected Tile(boolean isPlatformTile) {
		this.isPlatformTile = isPlatformTile;
	}


	/**
	 * Returns the x component of the texture coordinate of the top left vertex
	 */
	public float getTexCoordX(boolean foreGround) {

		int texX = 0;
		if (orientation != null) {
			switch (orientation) {
			case TL: texX = isStair ? 16 : 0;
				break;
			case TM: texX = 1;
				break;
			case TR: texX = isStair ? 17 : 2;
				break;
			case L: texX = 3;
				break;
			case M: texX = 4;
				break;
			case R: texX = 5;
				break;
			case BL: texX = 6;
				break;
			case BM: texX = 7;
				break;
			case BR: texX = 8;
				break;
			case PL: texX = 9;
				break;
			case PT: texX = 10;
				break;
			case PR: texX = 11;
				break;
			case PB: texX = 12;
				break;
			case V: texX = 13;
				break;
			case H: texX = 14;
				break;
			case C: texX = 15;
				break;
			default:
				throw new RuntimeException("Tile type is not defined");
			}
		}

		return foreGround ? texX * Topography.textureCoordinateQuantization : (texX + 20) * Topography.textureCoordinateQuantization;
	}


	/**
	 * Returns the y component of the texture coordinate of the top left vertex
	 *
	 *  <p> 1		{@link EmptyTile}               </p>
	 *  <p> 2		{@link DebugTile}               </p>
	 *  <p> 3		{@link StandardSoilTile}        </p>
	 *  <p> 4		{@link GraniteTile}             </p>
	 *  <p> 5		{@link YellowSandTile}          </p>
	 *  <p> 6		{@link SandStoneTile}           </p>
	 *  <p> 7		{@link DryDirtTile}             </p>
	 *  <p> 8		{@link YellowBrickTile}         </p>
	 *  <p> 9		{@link ClearGlassTile}          </p>
	 *  <p> 10		{@link YellowBrickPlatform}		</p>
	 */
	public float getTexCoordY() {
		return getTexCoordYSpecific() * Topography.textureCoordinateQuantization;
	}


	/** Which y-coord should we use for this tile? 1-indexed */
	protected abstract float getTexCoordYSpecific();


	/**
	 * @return {@link #orientation}.
	 */
	public Orientation getOrientation() {
		return orientation;
	}


	/**
	 * Whether or not this can be a passable tile
	 */
	public boolean isPassable() {
		return this instanceof EmptyTile || isPlatformTile;
	}


	/**
	 * Changes this tile to a {@link #isStair} tile
	 */
	public abstract void changeToStair();


	/**
	 * @return true if {@link #isStair}
	 */
	public boolean isStair() {
		return isStair;
	}


	@Override
	public String toString() {
		return "Tile: " + Integer.toHexString(hashCode()) + "\n" + this.getClass().getSimpleName() + ", " + orientation;
	}


	/**
	 * Sets the {@link #orientation}.
	 *
	 * @param x - the tile x-coordinate
	 * @param x - the tile y-coordinate
	 * @param chunkX - the chunk x-coordinate
	 * @param chunkY - the chunk y-coordinate
	 */
	public void calculateOrientation(int chunkX, int chunkY, int x, int y, boolean foreGround) {

		ChunkMap map = Topography.chunkMap;

		Tile left;
		Tile right;
		Tile above;
		Tile below;

		// Get the tile to the left
		if (x == 0) {
			if (map.get(chunkX - 1) == null || map.get(chunkX - 1).get(chunkY) == null) {
				left = new EmptyTile();
			} else {
				left = map.get(chunkX - 1).get(chunkY).getTile(Topography.CHUNK_SIZE - 1, y, foreGround);
			}
		} else {
			left = map.get(chunkX).get(chunkY).getTile(x - 1, y, foreGround);
		}

		// Get the tile to the right
		if (x == Topography.CHUNK_SIZE - 1) {
			if (map.get(chunkX + 1) == null || map.get(chunkX + 1).get(chunkY) == null) {
				right = new EmptyTile();
			} else {
				right = map.get(chunkX + 1).get(chunkY).getTile(0, y, foreGround);
			}
		} else {
			right = map.get(chunkX).get(chunkY).getTile(x + 1, y, foreGround);
		}

		// Get the tile above
		if (y == Topography.CHUNK_SIZE - 1) {
			if (map.get(chunkX).get(chunkY + 1) == null) {
				above = new EmptyTile();
			} else {
				above = map.get(chunkX).get(chunkY + 1).getTile(x, 0, foreGround);
			}
		} else {
			above = map.get(chunkX).get(chunkY).getTile(x, y + 1, foreGround);
		}

		// Get the tile below
		if (y == 0) {
			if (map.get(chunkX).get(chunkY - 1) == null) {
				below = new EmptyTile();
			} else {
				below = map.get(chunkX).get(chunkY - 1).getTile(x, Topography.CHUNK_SIZE - 1, foreGround);
			}
		} else {
			below = map.get(chunkX).get(chunkY).getTile(x, y - 1, foreGround);
		}

		boolean aboveSame = above.getClass().equals(this.getClass());
		boolean belowSame = below.getClass().equals(this.getClass());
		boolean leftSame = left.getClass().equals(this.getClass());
		boolean rightSame = right.getClass().equals(this.getClass());

		orientation = orientationTree.get(aboveSame, belowSame, leftSame, rightSame);
	}


	/**
	 * A FUCKING DEBUG TILE!
	 *
	 * @author Matt
	 */
	public static class DebugTile extends Tile {
		private static final long serialVersionUID = 4735245982678945958L;

		/**
		 * Constructor
		 */
		public DebugTile() {
			super(false);
			if (!"true".equals(System.getProperty("debug"))) {
				throw new RuntimeException();
			}
		}


		@Override
		protected float getTexCoordYSpecific() {
			return 2;
		}


		@Override
		public void changeToStair() {
		}
	}


	/**
	 * A FUCKING EMPTY TILE!
	 *
	 * @author Matt
	 */
	public static class EmptyTile extends Tile {
		private static final long serialVersionUID = -1388867188698896998L;

		/**
		 * Constructor
		 */
		public EmptyTile() {
			super(false);
		}


		@Override
		protected float getTexCoordYSpecific() {
			return 1;
		}


		@Override
		public void changeToStair() {
		}
	}
}
