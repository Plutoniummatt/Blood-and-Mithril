package bloodandmithril.world.topography.tile;

import static bloodandmithril.world.topography.Topography.CHUNK_SIZE;
import static bloodandmithril.world.topography.Topography.TEXTURE_COORDINATE_QUANTIZATION;

import java.io.Serializable;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.character.ai.task.minetile.MineTile;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.world.topography.ChunkMap;

/**
 * A FUCKING TILE!
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class Tile implements Serializable {
	private static final long serialVersionUID = -2331827246047876705L;
	private static final int UNIQUE_TILE_TEXTURES = 14;
	
	public enum CornerType {
		NONE,
		SLOPE_UP,
		SLOPE_DOWN,
		SLOPE_UP_THEN_DOWN
	}
	
	/**
	 * The 'orientation' of edge tiles
	 */
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
		{3,0}
	};
	
	/** Whether this is a platform tile */
	public final boolean isPlatformTile;

	/** Whether this is a smoothed ceiling tile */
	protected boolean smoothCeiling = false;
	
	/** An 'edge' tile is a tile that is adjascent to at least one {@link EmptyTile}, adjascency is defined as, right next to, or diagonal to */
	public transient boolean edge = false;
	
	/**
	 * The corner type controls how entities move over the tile
	 * 
	 * Type 0 - Not a corner tile, entities treat this as 
	 * Type 1 - Slope up from left 
	 * Type 2 - Slope down from left 
	 * Type 3 - Slope up then down 
	 */
	private transient byte cornerType = 0;
	
	/** Which tile texture index we should use for this edge tile, and how many CW 90 degree rotations should be applied */
	public transient int edgeIndex, edgeRotation;

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
		int texX = (edge ? edgeIndex : 0) + edgeRotation * UNIQUE_TILE_TEXTURES;

		return (texX + (foreGround ? 0 : 59)) * TEXTURE_COORDINATE_QUANTIZATION;
	}


	/**
	 * Returns the y component of the texture coordinate of the top left vertex
	 */
	public final float getTexCoordY() {
		return getTexCoordYSpecific() * TEXTURE_COORDINATE_QUANTIZATION;
	}


	/** Which y-coord should we use for this tile? 1-indexed */
	protected abstract float getTexCoordYSpecific();


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
	 * Changes this tile to a {@link #smoothCeiling} tile
	 */
	public abstract void changeToSmoothCeiling();


	/**
	 * @return an {@link Item} that is obtained when this {@link Tile} is mined. See {@link MineTile}
	 */
	public abstract Item mine();


	@Override
	public final String toString() {
		return "Tile: " + Integer.toHexString(hashCode()) + "\n" + this.getClass().getSimpleName();
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
				left = map.get(chunkX - 1).get(chunkY).getTile(CHUNK_SIZE - 1, tileY, foreGround);
			}
		} else {
			left = map.get(chunkX).get(chunkY).getTile(tileX - 1, tileY, foreGround);
		}

		// Get the tile to the right
		if (tileX == CHUNK_SIZE - 1) {
			if (map.get(chunkX + 1) == null || map.get(chunkX + 1).get(chunkY) == null) {
				right = new EmptyTile();
			} else {
				right = map.get(chunkX + 1).get(chunkY).getTile(0, tileY, foreGround);
			}
		} else {
			right = map.get(chunkX).get(chunkY).getTile(tileX + 1, tileY, foreGround);
		}

		// Get the tile above
		if (tileY == CHUNK_SIZE - 1) {
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
				below = map.get(chunkX).get(chunkY - 1).getTile(tileX, CHUNK_SIZE - 1, foreGround);
			}
		} else {
			below = map.get(chunkX).get(chunkY).getTile(tileX, tileY - 1, foreGround);
		}
		
		// Get the tile to the top left
		if (tileX != 0 && tileY != CHUNK_SIZE - 1) {
			topLeft = map.get(chunkX).get(chunkY).getTile(tileX - 1, tileY + 1, foreGround);
		} else {
			if (tileX == 0 && tileY == CHUNK_SIZE - 1) {
				if (map.get(chunkX - 1) == null || map.get(chunkX - 1).get(chunkY + 1) == null) {
					topLeft = new EmptyTile();
				} else {
					topLeft = map.get(chunkX - 1).get(chunkY + 1).getTile(CHUNK_SIZE - 1, 0, foreGround);
				}
			} else if (tileX == 0) {
				if (map.get(chunkX - 1) == null || map.get(chunkX - 1).get(chunkY) == null) {
					topLeft = new EmptyTile();
				} else {
					topLeft = map.get(chunkX - 1).get(chunkY).getTile(CHUNK_SIZE - 1, tileY + 1, foreGround);
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
					bottomleft = map.get(chunkX - 1).get(chunkY - 1).getTile(CHUNK_SIZE - 1, CHUNK_SIZE - 1, foreGround);
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
					bottomRight = map.get(chunkX + 1).get(chunkY - 1).getTile(0, CHUNK_SIZE - 1, foreGround);
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
		
		final int aboveEdge = ((!isPlatformTile && above.isPassable()) || (isPlatformTile)) ? 2 : 0;
		final int belowEdge = ((!isPlatformTile && below.isPassable()) || (isPlatformTile)) ? 64 : 0;
		final int leftEdge = ((!isPlatformTile && left.isPassable()) || (!left.isPlatformTile && isPlatformTile)) ? 8 : 0;
		final int rightEdge = ((!isPlatformTile && right.isPassable()) || (!right.isPlatformTile && isPlatformTile)) ? 16 : 0;
		final int topLeftEdge = ((!isPlatformTile && topLeft.isPassable()) || (isPlatformTile)) ? 1 : 0;
		final int topRightEdge = ((!isPlatformTile && topRight.isPassable()) || (isPlatformTile)) ? 4 : 0;
		final int bottomLeftEdge = ((!isPlatformTile && bottomleft.isPassable()) || (isPlatformTile)) ? 32 : 0;
		final int bottomRightEdge = ((!isPlatformTile && bottomRight.isPassable()) || (isPlatformTile)) ? 128 : 0;

		if (isPlatformTile) {
			edge = true;
		} else {
			edge = 	!getClass().equals(EmptyTile.class) && (
					above.isPassable() || 
					below.isPassable() ||
					left.isPassable() ||
					right.isPassable() ||
					topLeft.isPassable() ||
					topRight.isPassable() ||
					bottomleft.isPassable() ||
					bottomRight.isPassable());
		}

		
		if (isPlatformTile) {
			cornerType = determineCornerType(
				above.isPlatformTile, 
				below.isPlatformTile, 
				left.isPlatformTile, 
				right.isPlatformTile,
				topLeft.isPlatformTile,
				topRight.isPlatformTile,
				bottomleft.isPlatformTile,
				bottomRight.isPlatformTile
			);
		} else {
			cornerType = determineCornerType(
				above.isPassable(), 
				below.isPassable(), 
				left.isPassable(), 
				right.isPassable(),
				topLeft.isPassable(),
				topRight.isPassable(),
				bottomleft.isPassable(),
				bottomRight.isPassable()
			);
		}

		if (edge) {
			int[] edge = edgeOrientationArray[255 - (aboveEdge + belowEdge + leftEdge + rightEdge + topLeftEdge + topRightEdge + bottomLeftEdge + bottomRightEdge)];
			this.edgeIndex = edge[0];
			this.edgeRotation = edge[1];
		} else {
			this.edgeIndex = 0;
			this.edgeRotation = 0;
		}
	}


	private byte determineCornerType(
		boolean above, 
		boolean below, 
		boolean left, 
		boolean right, 
		boolean topLeft, 
		boolean topRight, 
		boolean bottomleft, 
		boolean bottomRight
	) {
		if (left && above && !right && !bottomleft && topLeft) {
			return 1;
		}
		
		if (left && above && topLeft && !bottomleft && !topRight) {
			return 1;
		}
		
		if (right && above && !left && !bottomRight && topRight) {
			return 2;
		}
		
		if (right && above && topRight && !topLeft && !bottomRight) {
			return 2;
		}
		
		if (above && left && right && (!bottomleft || !bottomRight) && topLeft && topRight) {
			return 3;
		}
		
		return 0;
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
	
	
	public CornerType getCornerType() {
		switch (cornerType) {
		case 0:
			return CornerType.NONE;
		case 1:
			return CornerType.SLOPE_UP;
		case 2:
			return CornerType.SLOPE_DOWN;
		case 3:
			return CornerType.SLOPE_UP_THEN_DOWN;
		default:
			throw new IllegalStateException();
		}
	}
}
