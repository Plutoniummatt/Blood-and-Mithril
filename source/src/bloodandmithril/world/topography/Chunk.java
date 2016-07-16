package bloodandmithril.world.topography;

import static bloodandmithril.world.topography.Topography.CHUNK_SIZE;
import static bloodandmithril.world.topography.Topography.TEXTURE_COORDINATE_QUANTIZATION;
import static bloodandmithril.world.topography.Topography.TILE_SIZE;

import java.io.Serializable;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;

import bloodandmithril.core.Copyright;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.tile.Tile;

/**
 * The legendary and mythical Chunk
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public final class Chunk {

	/**
	 * <p> The mesh this chunk is responsible for. </p>
	 * <p> This contains a bunch of vertex attributes. </p>
	 * <p> The vertex coordinates are in fact world coordinates. </p>
	 */
	private Mesh fMesh;
	private Mesh bMesh;

	/** The vertex attributes. */
	private final float[] fVertexAttributes = new float[16 * Topography.CHUNK_SIZE * Topography.CHUNK_SIZE];
	private final float[] bVertexAttributes = new float[16 * Topography.CHUNK_SIZE * Topography.CHUNK_SIZE];

	/**	The chunk data */
	private final ChunkData fData;
	private final ChunkData bData;

	/**	Rendering size of tiles */
	private static final int tileRenderSize = 32;

	/**
	 * Constructor
	 */
	public Chunk(Tile[][] fTiles, Tile[][] bTiles, int x, int y, int worldId) {
		this.fData = new ChunkData(Util.clone2DArray(fTiles), x, y, worldId, true);
		this.bData = new ChunkData(Util.clone2DArray(bTiles), x, y, worldId, false);
	}


	/**
	 * Overloaded Constructor - Be weary of array cloning
	 *
	 * <p><b> DON'T USE ME, THIS IS ONLY USED FOR PERSISTENCE </p></b>
	 */
	public Chunk(ChunkData fData, ChunkData bData) {
		this.fData = fData;
		this.bData = bData;
	}


	/**
	 * Constructs the mesh.
	 */
	private final void constructMesh(ChunkData data, float[] vertexAttributes, boolean foreGround) {


		Mesh mesh = new Mesh(false, Topography.CHUNK_SIZE * Topography.CHUNK_SIZE * 4, 0, new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"),
				new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0"));

		for (int x = 0; x != Topography.CHUNK_SIZE; x++) {
			for (int y = 0; y != Topography.CHUNK_SIZE; y++) {


				float texX = fData.tiles[x][y].getTexCoordX(foreGround);
				float texY = fData.tiles[x][y].getTexCoordY();

				populateQuadVertexAttributes(x, y, texX, texY, data, vertexAttributes);

			}
		}
		mesh.setVertices(fVertexAttributes);

		if (foreGround) {
			fMesh = mesh;
		} else {
			bMesh = mesh;
		}
	}


	/**
	 * Checks if the mesh has been constructed, if not, construct it and calculate orientations of tiles
	 */
	public final void checkMesh() {
		if (fMesh == null) {
			constructMesh(fData, fVertexAttributes, true);
			calculateChunkOrientations(true);
		}
		if (bMesh == null) {
			constructMesh(bData, bVertexAttributes, false);
			calculateChunkOrientations(false);
		}
	}


	/**
	 * Populates the vertex attributes of a quad.
	 */
	private final void populateQuadVertexAttributes(int x, int y, float texX, float texY, ChunkData data, float[] vertexAttributes) {
		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + 0] = data.xChunkCoord * CHUNK_SIZE * TILE_SIZE + x * TILE_SIZE + TILE_SIZE/2 - tileRenderSize/2;
		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + 1] = data.yChunkCoord * CHUNK_SIZE * TILE_SIZE + y * TILE_SIZE + TILE_SIZE/2 - tileRenderSize/2;
		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + 2] = texX;
		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + 3] = texY;

		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + 4] = data.xChunkCoord * CHUNK_SIZE * TILE_SIZE + x * TILE_SIZE + TILE_SIZE/2 + tileRenderSize/2;
		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + 5] = data.yChunkCoord * CHUNK_SIZE * TILE_SIZE + y * TILE_SIZE + TILE_SIZE/2 - tileRenderSize/2;
		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + 6] = texX + TEXTURE_COORDINATE_QUANTIZATION;
		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + 7] = texY;

		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + 8] = data.xChunkCoord * CHUNK_SIZE * TILE_SIZE + x * TILE_SIZE + TILE_SIZE/2 + tileRenderSize/2;
		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + 9] = data.yChunkCoord * CHUNK_SIZE * TILE_SIZE + y * TILE_SIZE + TILE_SIZE/2 + tileRenderSize/2;
		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + 10] = texX + TEXTURE_COORDINATE_QUANTIZATION;
		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + 11] = texY - TEXTURE_COORDINATE_QUANTIZATION;

		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + 12] = data.xChunkCoord * CHUNK_SIZE * TILE_SIZE + x * TILE_SIZE + TILE_SIZE/2 - tileRenderSize/2;
		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + 13] = data.yChunkCoord * CHUNK_SIZE * TILE_SIZE + y * TILE_SIZE + TILE_SIZE/2 + tileRenderSize/2;
		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + 14] = texX;
		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + 15] = texY - TEXTURE_COORDINATE_QUANTIZATION;
	}


	@Override
	public final String toString() {
		return "Chunk: " + Integer.toHexString(hashCode()) + "\n" + "x - " + Integer.toString(fData.xChunkCoord) + "\n" + "y - " + Integer.toString(fData.yChunkCoord);
	}


	/**
	 * @param x - the x-coordinate of the tile you want to get.
	 * @param y - the y-coordinate of the tile you want to get.
	 * @return the tile you wanted to get
	 */
	public final Tile getTile(int x, int y, boolean foreGround) {
		return foreGround ? fData.tiles[x][y] : bData.tiles[x][y];
	}


	/**
	 * @return a column of tiles
	 */
	public final Tile[] getColumn(int x, boolean foreGround) {
		return foreGround ? fData.tiles[x] : bData.tiles[x];
	}


	/**
	 * @return a row of tiles
	 */
	public final Tile[] getRow(int y, boolean foreGround) {
		Tile[] row = new Tile[Topography.CHUNK_SIZE];
		for (int x = 0; x < Topography.CHUNK_SIZE; x++) {
			row[x] = foreGround ? fData.tiles[x][y] : bData.tiles[x][y];
		}

		return row;
	}


	/**
	 * Class representing the data that is stored on the chunk.
	 *
	 * @author Matt
	 */
	public static final class ChunkData implements Serializable {
		private static final long serialVersionUID = 4819937128429782914L;

		/**
		 * Constructor
		 */
		public ChunkData(Tile[][] tiles, int xChunkCoord, int yChunkCoord, int worldId, boolean foreground) {
			this.tiles = tiles;
			this.xChunkCoord = xChunkCoord;
			this.yChunkCoord = yChunkCoord;
			this.worldId = worldId;
			this.foreground = foreground;
		}

		/**
		 * No-arg Constructor
		 */
		public ChunkData() {
		}

		/** An array of tiles - purely for logic purposes */
		public Tile[][] tiles;

		/** The x-coordinate of the bottom left corner of the chunk */
		public int xChunkCoord;

		/** The y-coordinate of the bottom left corner of the chunk */
		public int yChunkCoord;

		/** Unique id of the {@link World} this chunk data relates to */
		public int worldId;

		/** Whether this is a foreground chunk data */
		public boolean foreground;
	}


	/**
	 * Repopupates the texture coordinates of this tile in the mesh
	 */
	public final void repopulateTextureCoordinates(int x, int y, boolean foreGround) {
		Tile tile = foreGround ? fData.tiles[x][y] : bData.tiles[x][y];
		if (foreGround) {
			populateQuadVertexAttributes(x, y, tile.getTexCoordX(foreGround), tile.getTexCoordY(), fData, fVertexAttributes);
		} else {
			populateQuadVertexAttributes(x, y, tile.getTexCoordX(foreGround), tile.getTexCoordY(), bData, bVertexAttributes);
		}
	}


	/**
	 * Refreshes the mesh.
	 */
	public final void refreshMesh() {
		if (fMesh != null) {
			fMesh.setVertices(fVertexAttributes);
		}
		if (bMesh != null) {
			bMesh.setVertices(bVertexAttributes);
		}
	}


	/**
	 * @return {@link #fData}
	 */
	public final ChunkData getChunkData(boolean foreGround) {
		return foreGround ? fData : bData;
	}


	/**
	 * Calculates the orientation of the tiles based on which tiles next to it
	 * are different.
	 *
	 * @param chunkX - the x coordinate of the chunk to calculate for
	 * @param chunkY - the y coordinate of the chunk to calculate for
	 */
	private final void calculateChunkOrientations(boolean foreGround) {
		int chunkX = fData.xChunkCoord;
		int chunkY = fData.yChunkCoord;

		ChunkMap chunkMap = Domain.getWorld(fData.worldId).getTopography().getChunkMap();

		if (chunkMap.get(chunkX).get(chunkY + 1) != null) {
			int x = 0;
			for (Tile tile : chunkMap.get(chunkX).get(chunkY + 1).getRow(0, foreGround)) {
				tile.calculateOrientation(chunkX, chunkY + 1, x, 0, foreGround, chunkMap);
				chunkMap.get(chunkX).get(chunkY + 1).repopulateTextureCoordinates(x, 0, foreGround);
				x++;
			}
			chunkMap.get(chunkX).get(chunkY + 1).refreshMesh();
		}

		if (chunkMap.get(chunkX).get(chunkY - 1) != null) {
			int x = 0;
			for (Tile tile : chunkMap.get(chunkX).get(chunkY - 1).getRow(Topography.CHUNK_SIZE - 1, foreGround)) {
				tile.calculateOrientation(chunkX, chunkY - 1, x, Topography.CHUNK_SIZE - 1, foreGround, chunkMap);
				chunkMap.get(chunkX).get(chunkY - 1).repopulateTextureCoordinates(x, Topography.CHUNK_SIZE - 1, foreGround);
				x++;
			}
			chunkMap.get(chunkX).get(chunkY - 1).refreshMesh();
		}

		if (chunkMap.get(chunkX - 1) != null && chunkMap.get(chunkX - 1).get(chunkY) != null) {
			int y = 0;
			for (Tile tile : chunkMap.get(chunkX - 1).get(chunkY).getColumn(Topography.CHUNK_SIZE - 1, foreGround)) {
				tile.calculateOrientation(chunkX - 1, chunkY, Topography.CHUNK_SIZE - 1, y, foreGround, chunkMap);
				chunkMap.get(chunkX - 1).get(chunkY).repopulateTextureCoordinates(Topography.CHUNK_SIZE - 1, y, foreGround);
				y++;
			}
			chunkMap.get(chunkX - 1).get(chunkY).refreshMesh();
		}

		if (chunkMap.get(chunkX + 1) != null && chunkMap.get(chunkX + 1).get(chunkY) != null) {
			int y = 0;
			for (Tile tile : chunkMap.get(chunkX + 1).get(chunkY).getColumn(0, foreGround)) {
				tile.calculateOrientation(chunkX + 1, chunkY, 0, y, foreGround, chunkMap);
				chunkMap.get(chunkX + 1).get(chunkY).repopulateTextureCoordinates(0, y, foreGround);
				y++;
			}
			chunkMap.get(chunkX + 1).get(chunkY).refreshMesh();
		}

		for (int x = 0; x < Topography.CHUNK_SIZE; x++) {
			for (int y = 0; y < Topography.CHUNK_SIZE; y++) {
				chunkMap.get(chunkX).get(chunkY).getTile(x, y, foreGround).calculateOrientation(chunkX, chunkY, x, y, foreGround, chunkMap);
				chunkMap.get(chunkX).get(chunkY).repopulateTextureCoordinates(x, y, foreGround);
			}
		}
		chunkMap.get(chunkX).get(chunkY).refreshMesh();
	}


	/**
	 * Deletes a tile on the chunk
	 */
	public final void deleteTile(int tileX, int tileY, boolean foreGround) {
		if (foreGround) {
			fData.tiles[tileX][tileY] = new Tile.EmptyTile();
		} else {
			bData.tiles[tileX][tileY] = new Tile.EmptyTile();
		}
		repopulateTextureCoordinates(tileX, tileY, foreGround);
		recalculateOrientationsForTile(tileX, tileY, foreGround);
	}


	/**
	 * Changes a tile on the chunk
	 */
	public final void changeTile(int tileX, int tileY, boolean foreGround, Class<? extends Tile> toChangeTo) {
		Tile newTile;
		try {
			newTile = toChangeTo.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		changeTile(tileX, tileY, foreGround, newTile);
	}


	/**
	 * Changes a tile on the chunk
	 */
	public final void changeTile(int tileX, int tileY, boolean foreGround, Tile toChangeTo) {
		if (foreGround) {
			fData.tiles[tileX][tileY] = toChangeTo;
		} else {
			bData.tiles[tileX][tileY] = toChangeTo;
		}
		repopulateTextureCoordinates(tileX, tileY, foreGround);
		calculateChunkOrientations(foreGround);
	}
	
	
	public Mesh getFMesh() {
		return fMesh;
	}
	
	
	public Mesh getBMesh() {
		return bMesh;
	}
	
	
	public ChunkData getFData() {
		return fData;
	}
	
	
	public ChunkData getBData() {
		return bData;
	}


	/**
	 * This method is a lie, it doesn't do anything....Just kidding this thing calculates the orientation of ONE tile within *this* chunk
	 */
	private final void recalculateOrientationsForTile(int x, int y, boolean foreGround) {

		ChunkMap map = Domain.getWorld(fData.worldId).getTopography().getChunkMap();

		Tile left;
		Tile right;
		Tile above;
		Tile below;

		// Get the tile to the left
		if (x == 0) {
			if (map.doesChunkExist(fData.xChunkCoord - 1, fData.yChunkCoord)) {
				Chunk chunk = map.get(fData.xChunkCoord - 1).get(fData.yChunkCoord);
				left = chunk.getTile(Topography.CHUNK_SIZE - 1, y, foreGround);
				left.calculateOrientation(fData.xChunkCoord - 1, fData.yChunkCoord, Topography.CHUNK_SIZE - 1, y, foreGround, map);

				chunk.populateQuadVertexAttributes(
					Topography.CHUNK_SIZE - 1,
					y,
					left.getTexCoordX(foreGround),
					left.getTexCoordY(),
					foreGround ? chunk.fData : chunk.bData,
					foreGround ? chunk.fVertexAttributes : chunk.bVertexAttributes
				);

				chunk.refreshMesh();
			} else {
				left = null;
			}
		} else {
			left = map.get(fData.xChunkCoord).get(fData.yChunkCoord).getTile(x - 1, y, foreGround);
			left.calculateOrientation(fData.xChunkCoord, fData.yChunkCoord, x - 1, y, foreGround, map);

			map.get(fData.xChunkCoord).get(fData.yChunkCoord).populateQuadVertexAttributes(
				x - 1,
				y,
				left.getTexCoordX(foreGround),
				left.getTexCoordY(),
				foreGround ? fData : bData,
				foreGround ? fVertexAttributes : bVertexAttributes
			);

			map.get(fData.xChunkCoord).get(fData.yChunkCoord).refreshMesh();
		}

		// Get the tile to the right
		if (x == Topography.CHUNK_SIZE - 1) {
			if (map.doesChunkExist(fData.xChunkCoord + 1, fData.yChunkCoord)) {
				Chunk chunk = map.get(fData.xChunkCoord + 1).get(fData.yChunkCoord);
				right = chunk.getTile(0, y, foreGround);
				right.calculateOrientation(fData.xChunkCoord + 1, fData.yChunkCoord, 0, y, foreGround, map);

				chunk.populateQuadVertexAttributes(
					0,
					y,
					right.getTexCoordX(foreGround),
					right.getTexCoordY(),
					foreGround ? chunk.fData : chunk.bData,
					foreGround ? chunk.fVertexAttributes : chunk.bVertexAttributes
				);

				chunk.refreshMesh();
			} else {
				right = null;
			}
		} else {
			right = map.get(fData.xChunkCoord).get(fData.yChunkCoord).getTile(x + 1, y, foreGround);
			right.calculateOrientation(fData.xChunkCoord, fData.yChunkCoord, x + 1, y, foreGround, map);

			map.get(fData.xChunkCoord).get(fData.yChunkCoord).populateQuadVertexAttributes(
				x + 1,
				y,
				right.getTexCoordX(foreGround),
				right.getTexCoordY(),
				foreGround ? fData : bData,
				foreGround ? fVertexAttributes : bVertexAttributes
			);

			map.get(fData.xChunkCoord).get(fData.yChunkCoord).refreshMesh();
		}

		// Get the tile above
		if (y == Topography.CHUNK_SIZE - 1) {
			if (map.doesChunkExist(fData.xChunkCoord, fData.yChunkCoord + 1)) {
				Chunk chunk = map.get(fData.xChunkCoord).get(fData.yChunkCoord + 1);
				above = chunk.getTile(x, 0, foreGround);
				above.calculateOrientation(fData.xChunkCoord, fData.yChunkCoord + 1, x, 0, foreGround, map);

				chunk.populateQuadVertexAttributes(
					x,
					0,
					above.getTexCoordX(foreGround),
					above.getTexCoordY(),
					foreGround ? chunk.fData : chunk.bData,
					foreGround ? chunk.fVertexAttributes : chunk.bVertexAttributes
				);

				chunk.refreshMesh();
			} else {
				above = null;
			}
		} else {
			above = map.get(fData.xChunkCoord).get(fData.yChunkCoord).getTile(x, y + 1, foreGround);
			above.calculateOrientation(fData.xChunkCoord, fData.yChunkCoord, x, y + 1, foreGround, map);

			map.get(fData.xChunkCoord).get(fData.yChunkCoord).populateQuadVertexAttributes(
				x,
				y + 1,
				above.getTexCoordX(foreGround),
				above.getTexCoordY(),
				foreGround ? fData : bData,
				foreGround ? fVertexAttributes : bVertexAttributes
			);

			map.get(fData.xChunkCoord).get(fData.yChunkCoord).refreshMesh();
		}

		// Get the tile below
		if (y == 0) {
			if (map.doesChunkExist(fData.xChunkCoord, fData.yChunkCoord - 1)) {
				Chunk chunk = map.get(fData.xChunkCoord).get(fData.yChunkCoord - 1);
				below = chunk.getTile(x, Topography.CHUNK_SIZE - 1, foreGround);
				below.calculateOrientation(fData.xChunkCoord, fData.yChunkCoord - 1, x, Topography.CHUNK_SIZE - 1, foreGround, map);

				chunk.populateQuadVertexAttributes(
					x,
					Topography.CHUNK_SIZE - 1,
					below.getTexCoordX(foreGround),
					below.getTexCoordY(),
					foreGround ? chunk.fData : chunk.bData,
					foreGround ? chunk.fVertexAttributes : chunk.bVertexAttributes
				);

				chunk.refreshMesh();
			} else {
				below = null;
			}
		} else {
			below = map.get(fData.xChunkCoord).get(fData.yChunkCoord).getTile(x, y - 1, foreGround);
			below.calculateOrientation(fData.xChunkCoord, fData.yChunkCoord, x, y - 1, foreGround, map);

			map.get(fData.xChunkCoord).get(fData.yChunkCoord).populateQuadVertexAttributes(
				x,
				y - 1,
				below.getTexCoordX(foreGround),
				below.getTexCoordY(),
				foreGround ? fData : bData,
				foreGround ? fVertexAttributes : bVertexAttributes
			);

			map.get(fData.xChunkCoord).get(fData.yChunkCoord).refreshMesh();
		}

		fData.tiles[x][y].calculateOrientation(fData.xChunkCoord,fData.yChunkCoord, x, y, foreGround, map);
	}
}
