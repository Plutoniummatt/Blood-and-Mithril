package bloodandmithril.world.topography.tile;

import static bloodandmithril.world.topography.Topography.CHUNK_SIZE;

import java.io.Serializable;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.character.ai.task.minetile.MineTile;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.world.topography.ChunkMap;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.tiles.brick.YellowBrickPlatform;
import bloodandmithril.world.topography.tile.tiles.brick.YellowBrickTile;
import bloodandmithril.world.topography.tile.tiles.glass.ClearGlassTile;
import bloodandmithril.world.topography.tile.tiles.sedimentary.SandTile;
import bloodandmithril.world.topography.tile.tiles.soil.DryDirtTile;
import bloodandmithril.world.topography.tile.tiles.soil.StandardSoilTile;
import bloodandmithril.world.topography.tile.tiles.stone.GraniteTile;
import bloodandmithril.world.topography.tile.tiles.stone.SandStoneTile;

/**
 * A FUCKING TILE!
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class Tile implements Serializable {
	private static final long serialVersionUID = -2331827246047876705L;
	
	/** For fast calculation of tile orientation */
	private static Orientation[] orientationArray = new Orientation[] {
		Orientation.SINGLE, Orientation.PETRUDING_LEFT, Orientation.PETRUDING_RIGHT, Orientation.HORIZONTAL,
		Orientation.PETRUDING_TOP, Orientation.TOP_LEFT, Orientation.TOP_RIGHT, Orientation.TOP_MIDDLE,
		Orientation.PETRUDING_BOTTOM, Orientation.BOTTOM_LEFT, Orientation.BOTTOM_RIGHT, Orientation.BOTTOM_MIDDLE,
		Orientation.VERTICAL, Orientation.LEFT, Orientation.RIGHT, Orientation.MIDDLE
	};
	
	private static int[][] edgeOrientationArray = new int[][] {
		{8,0},
		{8,0},
		{9,2},
		{9,2},
		{8,0},
		{8,0},
		{9,2},
		{9,2},
		{9,1},
		{9,1},
		{11,0},
		{1,2},
		{9,1},
		{9,1},
		{11,0},
		{1,2},
		{9,3},
		{9,3},
		{11,1},
		{11,1},
		{9,3},
		{9,3},
		{1,3},
		{1,3},
		{10,1},
		{10,1},
		{12,0},
		{6,1},
		{10,1},
		{10,1},
		{7,3},
		{2,2},
		{8,0},
		{8,0},
		{9,2},
		{9,2},
		{8,0},
		{8,0},
		{9,2},
		{9,2},
		{9,1},
		{9,1},
		{11,0},
		{1,2},
		{9,1},
		{9,1},
		{11,0},
		{1,2},
		{9,3},
		{9,3},
		{11,1},
		{11,1},
		{9,3},
		{9,3},
		{1,3},
		{1,3},
		{10,1},
		{10,1},
		{12,0},
		{6,1},
		{10,1},
		{10,1},
		{7,3},
		{2,2},
		{9,0},
		{9,0},
		{10,0},
		{10,0},
		{9,0},
		{9,0},
		{10,0},
		{10,0},
		{11,3},
		{11,3},
		{12,3},
		{7,2},
		{11,3},
		{11,3},
		{12,3},
		{7,2},
		{11,2},
		{11,2},
		{12,1},
		{12,1},
		{11,2},
		{11,2},
		{6,2},
		{6,2},
		{12,2},
		{12,2},
		{13,0},
		{14,2},
		{12,2},
		{12,2},
		{14,3},
		{5,2},
		{9,0},
		{9,0},
		{10,0},
		{10,0},
		{9,0},
		{9,0},
		{10,0},
		{10,0},
		{1,1},
		{1,1},
		{6,0},
		{2,1},
		{1,1},
		{1,1},
		{6,0},
		{2,1},
		{11,2},
		{11,2},
		{12,1},
		{12,1},
		{11,2},
		{11,2},
		{6,2},
		{6,2},
		{7,1},
		{7,1},
		{14,1},
		{5,1},
		{7,1},
		{7,1},
		{4,0},
		{3,2},
		{8,0},
		{8,0},
		{9,2},
		{9,2},
		{8,0},
		{8,0},
		{9,2},
		{9,2},
		{9,1},
		{9,1},
		{11,0},
		{1,2},
		{9,1},
		{9,1},
		{11,0},
		{1,2},
		{9,3},
		{9,3},
		{11,1},
		{11,1},
		{9,3},
		{9,3},
		{1,3},
		{1,3},
		{10,1},
		{10,1},
		{12,0},
		{6,1},
		{10,1},
		{10,1},
		{7,3},
		{2,2},
		{8,0},
		{8,0},
		{9,2},
		{9,2},
		{8,0},
		{8,0},
		{9,2},
		{9,2},
		{9,1},
		{9,1},
		{11,0},
		{1,2},
		{9,1},
		{9,1},
		{11,0},
		{1,2},
		{9,3},
		{9,3},
		{11,1},
		{11,1},
		{9,3},
		{9,3},
		{1,3},
		{1,3},
		{10,1},
		{10,1},
		{12,0},
		{6,1},
		{10,1},
		{10,1},
		{7,3},
		{2,2},
		{9,0},
		{9,0},
		{10,0},
		{10,0},
		{9,0},
		{9,0},
		{10,0},
		{10,0},
		{11,3},
		{11,3},
		{12,3},
		{7,2},
		{11,3},
		{11,3},
		{12,3},
		{7,2},
		{1,0},
		{1,0},
		{7,0},
		{7,0},
		{1,0},
		{1,0},
		{2,3},
		{2,3},
		{6,3},
		{6,3},
		{14,0},
		{4,1},
		{6,3},
		{6,3},
		{5,3},
		{3,3},
		{9,0},
		{9,0},
		{10,0},
		{10,0},
		{9,0},
		{9,0},
		{10,0},
		{10,0},
		{1,1},
		{1,1},
		{6,0},
		{2,1},
		{1,1},
		{1,1},
		{6,0},
		{2,1},
		{1,0},
		{1,0},
		{7,0},
		{7,0},
		{1,0},
		{1,0},
		{2,3},
		{2,3},
		{2,0},
		{2,0},
		{5,0},
		{3,1},
		{2,0},
		{2,0},
		{3,0},
		{-1,-1}
	};
	

	/**
	 * The orientation of the tile
	 *
	 * @author Matt
	 */
	public enum Orientation {
		TOP_LEFT, 
		TOP_MIDDLE, 
		TOP_RIGHT, 
		LEFT, 
		MIDDLE, 
		RIGHT, 
		BOTTOM_LEFT, 
		BOTTOM_MIDDLE, 
		BOTTOM_RIGHT, 
		PETRUDING_LEFT, 
		PETRUDING_TOP, 
		PETRUDING_RIGHT, 
		PETRUDING_BOTTOM, 
		VERTICAL, 
		HORIZONTAL, 
		SINGLE
	}

	/** The {@link Orientation} of the tile */
	private Orientation orientation;

	/** Whether this is a platform tile */
	public final boolean isPlatformTile;

	/** Whether this is a Stair tile */
	protected boolean isStair = false;

	/** Whether this is a smoothed ceiling tile */
	protected boolean smoothCeiling = false;
	
	/** An 'edge' tile is a tile that is adjascent to at least one {@link EmptyTile}, adjascency is defined as, right next to, or diagonal to */
	public boolean edge = false;
	
	/** Which tile texture index we should use for this edge tile, and how many CW 90 degree rotations should be applied */
	public int edgeIndex, edgeRotation;

	/**
	 * Protected constructor
	 */
	protected Tile(final boolean isPlatformTile) {
		this.isPlatformTile = isPlatformTile;
	}

	/**
	 * The color of the particles emitted when this is mined
	 */
	public abstract Color getMineExplosionColor();

	/**
	 * Returns the x component of the texture coordinate of the top left vertex
	 */
	public final float getTexCoordX(final boolean foreGround) {

		int texX = 0;
		if (orientation != null) {
			switch (orientation) {
			case TOP_LEFT: texX = isStair ? 16 : 0;
				break;
			case TOP_MIDDLE: texX = 1;
				break;
			case TOP_RIGHT: texX = isStair ? 17 : 2;
				break;
			case LEFT: texX = 3;
				break;
			case MIDDLE: texX = 4;
				break;
			case RIGHT: texX = 5;
				break;
			case BOTTOM_LEFT: texX = smoothCeiling ? 18 : 6;
				break;
			case BOTTOM_MIDDLE: texX = 7;
				break;
			case BOTTOM_RIGHT: texX = smoothCeiling ? 19 : 8;
				break;
			case PETRUDING_LEFT: texX = 9;
				break;
			case PETRUDING_TOP: texX = 10;
				break;
			case PETRUDING_RIGHT: texX = 11;
				break;
			case PETRUDING_BOTTOM: texX = 12;
				break;
			case VERTICAL: texX = 13;
				break;
			case HORIZONTAL: texX = 14;
				break;
			case SINGLE: texX = 15;
				break;
			default:
				throw new RuntimeException("Tile type is not defined");
			}
		}

		return foreGround ? texX * Topography.TEXTURE_COORDINATE_QUANTIZATION : (texX + 20) * Topography.TEXTURE_COORDINATE_QUANTIZATION;
	}


	/**
	 * Returns the y component of the texture coordinate of the top left vertex
	 *
	 *  <p> 1		{@link EmptyTile}               </p>
	 *  <p> 2		{@link DebugTile}               </p>
	 *  <p> 3		{@link StandardSoilTile}        </p>
	 *  <p> 4		{@link GraniteTile}             </p>
	 *  <p> 5		{@link SandTile}          </p>
	 *  <p> 6		{@link SandStoneTile}           </p>
	 *  <p> 7		{@link DryDirtTile}             </p>
	 *  <p> 8		{@link YellowBrickTile}         </p>
	 *  <p> 9		{@link ClearGlassTile}          </p>
	 *  <p> 10		{@link YellowBrickPlatform}		</p>
	 */
	public final float getTexCoordY() {
		return getTexCoordYSpecific() * Topography.TEXTURE_COORDINATE_QUANTIZATION;
	}


	/** Which y-coord should we use for this tile? 1-indexed */
	protected abstract float getTexCoordYSpecific();


	/**
	 * @return {@link #orientation}.
	 */
	public final Orientation getOrientation() {
		return orientation;
	}


	/**
	 * Whether or not this can be a passable tile
	 */
	public final boolean isPassable() {
		return this instanceof EmptyTile || isPlatformTile;
	}


	/**
	 * Whether or not this can be seen through
	 */
	public abstract boolean isTransparent();


	/**
	 * Changes this tile to a {@link #isStair} tile
	 */
	public abstract void changeToStair();


	/**
	 * Changes this tile to a {@link #smoothCeiling} tile
	 */
	public abstract void changeToSmoothCeiling();


	/**
	 * @return an {@link Item} that is obtained when this {@link Tile} is mined. See {@link MineTile}
	 */
	public abstract Item mine();


	/**
	 * @return true if {@link #isStair}
	 */
	public final boolean isStair() {
		return isStair;
	}


	@Override
	public final String toString() {
		return "Tile: " + Integer.toHexString(hashCode()) + "\n" + this.getClass().getSimpleName() + ", " + orientation;
	}


	/**
	 * Sets the {@link #orientation}.
	 *
	 * @param tileX - the tile x-coordinate
	 * @param tileX - the tile y-coordinate
	 * @param chunkX - the chunk x-coordinate
	 * @param chunkY - the chunk y-coordinate
	 */
	public final void calculateOrientationAndEdge(final int chunkX, final int chunkY, final int tileX, final int tileY, final boolean foreGround, final ChunkMap map) {

		Tile left;
		Tile right;
		Tile above;
		Tile below;
		Tile topLeft;
		Tile topRight;
		Tile bottomleft;
		Tile bottomRight;

		// Get the tile to the left
		if (tileX == 0) {
			if (map.get(chunkX - 1) == null || map.get(chunkX - 1).get(chunkY) == null) {
				left = new EmptyTile();
			} else {
				left = map.get(chunkX - 1).get(chunkY).getTile(Topography.CHUNK_SIZE - 1, tileY, foreGround);
			}
		} else {
			left = map.get(chunkX).get(chunkY).getTile(tileX - 1, tileY, foreGround);
		}

		// Get the tile to the right
		if (tileX == Topography.CHUNK_SIZE - 1) {
			if (map.get(chunkX + 1) == null || map.get(chunkX + 1).get(chunkY) == null) {
				right = new EmptyTile();
			} else {
				right = map.get(chunkX + 1).get(chunkY).getTile(0, tileY, foreGround);
			}
		} else {
			right = map.get(chunkX).get(chunkY).getTile(tileX + 1, tileY, foreGround);
		}

		// Get the tile above
		if (tileY == Topography.CHUNK_SIZE - 1) {
			if (map.get(chunkX).get(chunkY + 1) == null) {
				above = new EmptyTile();
			} else {
				above = map.get(chunkX).get(chunkY + 1).getTile(tileX, 0, foreGround);
			}
		} else {
			above = map.get(chunkX).get(chunkY).getTile(tileX, tileY + 1, foreGround);
		}

		// Get the tile below
		if (tileY == 0) {
			if (map.get(chunkX).get(chunkY - 1) == null) {
				below = new EmptyTile();
			} else {
				below = map.get(chunkX).get(chunkY - 1).getTile(tileX, Topography.CHUNK_SIZE - 1, foreGround);
			}
		} else {
			below = map.get(chunkX).get(chunkY).getTile(tileX, tileY - 1, foreGround);
		}
		
		// Get the tile to the top left
		if (tileX != 0 && tileY != Topography.CHUNK_SIZE - 1) {
			topLeft = map.get(chunkX).get(chunkY).getTile(tileX - 1, tileY + 1, foreGround);
		} else {
			if (tileX == 0 && tileY == Topography.CHUNK_SIZE - 1) {
				if (map.get(chunkX - 1) == null || map.get(chunkX - 1).get(chunkY + 1) == null) {
					topLeft = new EmptyTile();
				} else {
					topLeft = map.get(chunkX - 1).get(chunkY + 1).getTile(Topography.CHUNK_SIZE - 1, 0, foreGround);
				}
			} else if (tileX == 0) {
				if (map.get(chunkX - 1) == null || map.get(chunkX - 1).get(chunkY) == null) {
					topLeft = new EmptyTile();
				} else {
					topLeft = map.get(chunkX - 1).get(chunkY).getTile(Topography.CHUNK_SIZE - 1, tileY + 1, foreGround);
				}
			} else {
				if (map.get(chunkX).get(chunkY + 1) == null) {
					topLeft = new EmptyTile();
				} else {
					topLeft = map.get(chunkX).get(chunkY + 1).getTile(tileX - 1, 0, foreGround);
				}
			}
		}
		
		// Get the tile to the top right
		if (tileX != CHUNK_SIZE - 1 && tileY != CHUNK_SIZE - 1) {
			topRight = map.get(chunkX).get(chunkY).getTile(tileX + 1, tileY + 1, foreGround);
		} else {
			
			if (tileX == CHUNK_SIZE - 1 && tileY == CHUNK_SIZE - 1) {
				if (map.get(chunkX + 1) == null || map.get(chunkX + 1).get(chunkY + 1) == null) {
					topRight = new EmptyTile();
				} else {
					topRight = map.get(chunkX + 1).get(chunkY + 1).getTile(0, 0, foreGround);
				}
			} else if (tileX == CHUNK_SIZE - 1) {
				if (map.get(chunkX + 1) == null || map.get(chunkX + 1).get(chunkY) == null) {
					topRight = new EmptyTile();
				} else {
					topRight = map.get(chunkX + 1).get(chunkY).getTile(0, tileY + 1, foreGround);
				}
			} else {
				if (map.get(chunkX).get(chunkY + 1) == null) {
					topRight = new EmptyTile();
				} else {
					topRight = map.get(chunkX).get(chunkY + 1).getTile(tileX + 1, 0, foreGround);
				}
			}
		}
		
		// Get the tile to the bottom left
		if (tileX != 0 && tileY != 0) {
			bottomleft = map.get(chunkX).get(chunkY).getTile(tileX - 1, tileY - 1, foreGround);
		} else {
			if (tileX == 0 && tileY == 0) {
				if (map.get(chunkX - 1) == null || map.get(chunkX - 1).get(chunkY - 1) == null) {
					bottomleft = new EmptyTile();
				} else {
					bottomleft = map.get(chunkX - 1).get(chunkY - 1).getTile(Topography.CHUNK_SIZE - 1, Topography.CHUNK_SIZE - 1, foreGround);
				}
			} else if (tileX == 0) {
				if (map.get(chunkX - 1) == null || map.get(chunkX - 1).get(chunkY) == null) {
					bottomleft = new EmptyTile();
				} else {
					bottomleft = map.get(chunkX - 1).get(chunkY).getTile(CHUNK_SIZE - 1, tileY - 1, foreGround);
				}
			} else {
				if (map.get(chunkX).get(chunkY - 1) == null) {
					bottomleft = new EmptyTile();
				} else {
					bottomleft = map.get(chunkX).get(chunkY - 1).getTile(tileX - 1, CHUNK_SIZE - 1, foreGround);
				}
			}
		}
		
		// Get the tile to the bottom right
		if (tileX != CHUNK_SIZE - 1 && tileY != 0) {
			bottomRight = map.get(chunkX).get(chunkY).getTile(tileX + 1, tileY - 1, foreGround);
		} else {
			if (tileX == CHUNK_SIZE - 1 && tileY == 0) {
				if (map.get(chunkX + 1) == null || map.get(chunkX + 1).get(chunkY - 1) == null) {
					bottomRight = new EmptyTile();
				} else {
					bottomRight = map.get(chunkX + 1).get(chunkY - 1).getTile(0, Topography.CHUNK_SIZE - 1, foreGround);
				}
			} else if (tileX == CHUNK_SIZE - 1) {
				if (map.get(chunkX + 1) == null || map.get(chunkX + 1).get(chunkY) == null) {
					bottomRight = new EmptyTile();
				} else {
					bottomRight = map.get(chunkX + 1).get(chunkY).getTile(0, tileY - 1, foreGround);
				}
			} else {
				if (map.get(chunkX).get(chunkY - 1) == null) {
					bottomRight = new EmptyTile();
				} else {
					bottomRight = map.get(chunkX).get(chunkY - 1).getTile(tileX + 1, CHUNK_SIZE - 1, foreGround);
				}
			}
		}

		final int aboveValue = above.getClass().equals(this.getClass()) ? 8 : 0;
		final int belowValue = below.getClass().equals(this.getClass()) ? 4 : 0;
		final int leftValue = left.getClass().equals(this.getClass()) ? 2 : 0;
		final int rightValue = right.getClass().equals(this.getClass()) ? 1 : 0;
		
		final int aboveEdge = above.isPassable() ? 2 : 0;
		final int belowEdge = below.isPassable() ? 64 : 0;
		final int leftEdge = left.isPassable() ? 8 : 0;
		final int rightEdge = right.isPassable() ? 16 : 0;
		final int topLeftEdge = topLeft.isPassable() ? 1 : 0;
		final int topRightEdge = topRight.isPassable() ? 4 : 0;
		final int bottomLeftEdge = bottomleft.isPassable() ? 32 : 0;
		final int bottomRightEdge = bottomRight.isPassable() ? 128 : 0;

		orientation = orientationArray[aboveValue + belowValue + leftValue + rightValue];
		
		edge = 	!getClass().equals(EmptyTile.class) && (
				above.getClass().equals(EmptyTile.class) || 
				below.getClass().equals(EmptyTile.class) ||
				left.getClass().equals(EmptyTile.class) ||
				right.getClass().equals(EmptyTile.class) ||
				topLeft.getClass().equals(EmptyTile.class) ||
				topRight.getClass().equals(EmptyTile.class) ||
				bottomleft.getClass().equals(EmptyTile.class) ||
				bottomRight.getClass().equals(EmptyTile.class));
		
		if (edge) {
			int[] edge = edgeOrientationArray[255 - (aboveEdge + belowEdge + leftEdge + rightEdge + topLeftEdge + topRightEdge + bottomLeftEdge + bottomRightEdge)];
			this.edgeIndex = edge[0];
			this.edgeRotation = edge[1];
		} else {
			this.edgeIndex = -1;
			this.edgeRotation = -1;
		}
	}


	/**
	 * A FUCKING DEBUG TILE!
	 *
	 * @author Matt
	 */
	public static final class DebugTile extends Tile {
		private static final long serialVersionUID = 4735245982678945958L;

		/**
		 * Constructor
		 */
		public DebugTile() {
			super(false);
		}


		@Override
		protected final float getTexCoordYSpecific() {
			return 2;
		}


		@Override
		public final void changeToStair() {
		}


		@Override
		public final void changeToSmoothCeiling() {
		}


		@Override
		public final Item mine() {
			throw new RuntimeException("Can not mine a debug tile");
		}


		@Override
		public final boolean isTransparent() {
			return true;
		}


		@Override
		public Color getMineExplosionColor() {
			return Color.RED;
		}
	}


	/**
	 * A FUCKING EMPTY TILE!
	 *
	 * @author Matt
	 */
	public static final class EmptyTile extends Tile {
		private static final long serialVersionUID = -1388867188698896998L;

		/**
		 * Constructor
		 */
		public EmptyTile() {
			super(false);
		}


		@Override
		protected final float getTexCoordYSpecific() {
			return 1;
		}


		@Override
		public final void changeToStair() {
		}


		@Override
		public final void changeToSmoothCeiling() {
		}


		@Override
		public final Item mine() {
			throw new RuntimeException("Can not mine an empty tile");
		}


		@Override
		public final boolean isTransparent() {
			return true;
		}


		@Override
		public Color getMineExplosionColor() {
			return Color.BLACK.mul(0);
		}
	}


	public final boolean isSmoothCeiling() {
		return smoothCeiling;
	}
}
