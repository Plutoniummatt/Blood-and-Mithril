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
	private static final int tileRenderSize = 16;

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
		Mesh mesh = new Mesh(
			false, 
			Topography.CHUNK_SIZE * Topography.CHUNK_SIZE * 4, 
			0, 
			new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"),
			new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0")
		);

		for (int x = 0; x != Topography.CHUNK_SIZE; x++) {
			for (int y = 0; y != Topography.CHUNK_SIZE; y++) {


				Tile tile = fData.tiles[x][y];
				float texX = tile.getTexCoordX(foreGround);
				float texY = tile.getTexCoordY();

				populateQuadVertexAttributes(x, y, texX, texY, data, vertexAttributes, tile.edgeRotation);

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
	public final void checkMesh(boolean force) {
		if (fMesh == null || force) {
			constructMesh(fData, fVertexAttributes, true);
			calculateChunkOrientations(true);
		}
		if (bMesh == null || force) {
			constructMesh(bData, bVertexAttributes, false);
			calculateChunkOrientations(false);
		}
	}


	/**
	 * Populates the vertex attributes of a quad.
	 */
	private final void populateQuadVertexAttributes(int x, int y, float texX, float texY, ChunkData data, float[] vertexAttributes, int rotation) {
		int i  = (4 - rotation) * 4;
		
		// Top left
		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + 0] = data.xChunkCoord * CHUNK_SIZE * TILE_SIZE + x * TILE_SIZE + TILE_SIZE/2 - tileRenderSize/2;
		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + 1] = data.yChunkCoord * CHUNK_SIZE * TILE_SIZE + y * TILE_SIZE + TILE_SIZE/2 - tileRenderSize/2;
		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + (2 + i) % 16] = texX;
		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + (3 + i) % 16] = texY;

		// Top right
		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + 4] = data.xChunkCoord * CHUNK_SIZE * TILE_SIZE + x * TILE_SIZE + TILE_SIZE/2 + tileRenderSize/2;
		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + 5] = data.yChunkCoord * CHUNK_SIZE * TILE_SIZE + y * TILE_SIZE + TILE_SIZE/2 - tileRenderSize/2;
		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + (6 + i) % 16] = texX + TEXTURE_COORDINATE_QUANTIZATION;
		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + (7 + i) % 16] = texY;

		// Bottom right
		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + 8] = data.xChunkCoord * CHUNK_SIZE * TILE_SIZE + x * TILE_SIZE + TILE_SIZE/2 + tileRenderSize/2;
		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + 9] = data.yChunkCoord * CHUNK_SIZE * TILE_SIZE + y * TILE_SIZE + TILE_SIZE/2 + tileRenderSize/2;
		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + (10 + i) % 16] = texX + TEXTURE_COORDINATE_QUANTIZATION;
		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + (11 + i) % 16] = texY - TEXTURE_COORDINATE_QUANTIZATION;

		// Bottom left
		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + 12] = data.xChunkCoord * CHUNK_SIZE * TILE_SIZE + x * TILE_SIZE + TILE_SIZE/2 - tileRenderSize/2;
		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + 13] = data.yChunkCoord * CHUNK_SIZE * TILE_SIZE + y * TILE_SIZE + TILE_SIZE/2 + tileRenderSize/2;
		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + (14 + i) % 16] = texX;
		vertexAttributes[16 * x * CHUNK_SIZE + y * 16 + (15 + i) % 16] = texY - TEXTURE_COORDINATE_QUANTIZATION;
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
			populateQuadVertexAttributes(x, y, tile.getTexCoordX(foreGround), tile.getTexCoordY(), fData, fVertexAttributes, tile.edgeRotation);
		} else {
			populateQuadVertexAttributes(x, y, tile.getTexCoordX(foreGround), tile.getTexCoordY(), bData, bVertexAttributes, tile.edgeRotation);
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
				tile.calculateOrientationAndEdge(chunkX, chunkY + 1, x, 0, foreGround, chunkMap);
				chunkMap.get(chunkX).get(chunkY + 1).repopulateTextureCoordinates(x, 0, foreGround);
				x++;
			}
			chunkMap.get(chunkX).get(chunkY + 1).refreshMesh();
		}

		if (chunkMap.get(chunkX).get(chunkY - 1) != null) {
			int x = 0;
			for (Tile tile : chunkMap.get(chunkX).get(chunkY - 1).getRow(Topography.CHUNK_SIZE - 1, foreGround)) {
				tile.calculateOrientationAndEdge(chunkX, chunkY - 1, x, Topography.CHUNK_SIZE - 1, foreGround, chunkMap);
				chunkMap.get(chunkX).get(chunkY - 1).repopulateTextureCoordinates(x, Topography.CHUNK_SIZE - 1, foreGround);
				x++;
			}
			chunkMap.get(chunkX).get(chunkY - 1).refreshMesh();
		}

		if (chunkMap.get(chunkX - 1) != null && chunkMap.get(chunkX - 1).get(chunkY) != null) {
			int y = 0;
			for (Tile tile : chunkMap.get(chunkX - 1).get(chunkY).getColumn(Topography.CHUNK_SIZE - 1, foreGround)) {
				tile.calculateOrientationAndEdge(chunkX - 1, chunkY, Topography.CHUNK_SIZE - 1, y, foreGround, chunkMap);
				chunkMap.get(chunkX - 1).get(chunkY).repopulateTextureCoordinates(Topography.CHUNK_SIZE - 1, y, foreGround);
				y++;
			}
			chunkMap.get(chunkX - 1).get(chunkY).refreshMesh();
		}

		if (chunkMap.get(chunkX + 1) != null && chunkMap.get(chunkX + 1).get(chunkY) != null) {
			int y = 0;
			for (Tile tile : chunkMap.get(chunkX + 1).get(chunkY).getColumn(0, foreGround)) {
				tile.calculateOrientationAndEdge(chunkX + 1, chunkY, 0, y, foreGround, chunkMap);
				chunkMap.get(chunkX + 1).get(chunkY).repopulateTextureCoordinates(0, y, foreGround);
				y++;
			}
			chunkMap.get(chunkX + 1).get(chunkY).refreshMesh();
		}
		
		
		if (chunkMap.get(chunkX + 1) != null && chunkMap.get(chunkX + 1).get(chunkY + 1) != null) {
			chunkMap.get(chunkX + 1).get(chunkY + 1).getTile(0, 0, foreGround).calculateOrientationAndEdge(chunkX + 1, chunkY + 1, 0, 0, foreGround, chunkMap);
			chunkMap.get(chunkX + 1).get(chunkY + 1).repopulateTextureCoordinates(0, 0, foreGround);
			chunkMap.get(chunkX + 1).get(chunkY + 1).refreshMesh();
		}
		
		
		if (chunkMap.get(chunkX + 1) != null && chunkMap.get(chunkX + 1).get(chunkY - 1) != null) {
			chunkMap.get(chunkX + 1).get(chunkY - 1).getTile(0, CHUNK_SIZE - 1, foreGround).calculateOrientationAndEdge(chunkX + 1, chunkY - 1, 0, CHUNK_SIZE - 1, foreGround, chunkMap);
			chunkMap.get(chunkX + 1).get(chunkY - 1).repopulateTextureCoordinates(0, CHUNK_SIZE - 1, foreGround);
			chunkMap.get(chunkX + 1).get(chunkY - 1).refreshMesh();
		}
		
		
		if (chunkMap.get(chunkX - 1) != null && chunkMap.get(chunkX - 1).get(chunkY + 1) != null) {
			chunkMap.get(chunkX - 1).get(chunkY + 1).getTile(CHUNK_SIZE - 1, 0, foreGround).calculateOrientationAndEdge(chunkX - 1, chunkY + 1, CHUNK_SIZE - 1, 0, foreGround, chunkMap);
			chunkMap.get(chunkX - 1).get(chunkY + 1).repopulateTextureCoordinates(CHUNK_SIZE - 1, 0, foreGround);
			chunkMap.get(chunkX - 1).get(chunkY + 1).refreshMesh();
		}
		
		
		if (chunkMap.get(chunkX - 1) != null && chunkMap.get(chunkX - 1).get(chunkY - 1) != null) {
			chunkMap.get(chunkX - 1).get(chunkY - 1).getTile(CHUNK_SIZE - 1, CHUNK_SIZE - 1, foreGround).calculateOrientationAndEdge(chunkX - 1, chunkY - 1, CHUNK_SIZE - 1, CHUNK_SIZE - 1, foreGround, chunkMap);
			chunkMap.get(chunkX - 1).get(chunkY - 1).repopulateTextureCoordinates(CHUNK_SIZE - 1, CHUNK_SIZE - 1, foreGround);
			chunkMap.get(chunkX - 1).get(chunkY - 1).refreshMesh();
		}

		for (int x = 0; x < CHUNK_SIZE; x++) {
			for (int y = 0; y < CHUNK_SIZE; y++) {
				Tile tile = chunkMap.get(chunkX).get(chunkY).getTile(x, y, foreGround);
				tile.calculateOrientationAndEdge(chunkX, chunkY, x, y, foreGround, chunkMap);
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
	
	
	private void recalculateOrientationForAdjascentTileAcrossChunks(int chunkX, int chunkY, int tileX, int tileY, boolean foreGround, ChunkMap map) {
		Tile tile;
		if (map.doesChunkExist(chunkX, chunkY)) {
			Chunk chunk = map.get(chunkX).get(chunkY);
			tile = chunk.getTile(tileX, tileY, foreGround);
			tile.calculateOrientationAndEdge(chunkX, chunkY, tileX, tileY, foreGround, map);

			chunk.populateQuadVertexAttributes(
				tileX,
				tileY,
				tile.getTexCoordX(foreGround),
				tile.getTexCoordY(),
				foreGround ? chunk.fData : chunk.bData,
				foreGround ? chunk.fVertexAttributes : chunk.bVertexAttributes,
				tile.edgeRotation
			);

			chunk.refreshMesh();
		}
	}
	
	
	private void recalculateOrientationForTileWithinSameChunk(int tileX, int tileY, boolean foreGround, ChunkMap map) {
		Tile tile;
		tile = map.get(fData.xChunkCoord).get(fData.yChunkCoord).getTile(tileX, tileY, foreGround);
		tile.calculateOrientationAndEdge(fData.xChunkCoord, fData.yChunkCoord, tileX, tileY, foreGround, map);

		map.get(fData.xChunkCoord).get(fData.yChunkCoord).populateQuadVertexAttributes(
			tileX,
			tileY,
			tile.getTexCoordX(foreGround),
			tile.getTexCoordY(),
			foreGround ? fData : bData,
			foreGround ? fVertexAttributes : bVertexAttributes,
			tile.edgeRotation
		);
	}


	/**
	 * Calculates the orientation of ONE tile within *this* chunk
	 */
	private final void recalculateOrientationsForTile(int tileX, int tileY, boolean foreGround) {

		ChunkMap map = Domain.getWorld(fData.worldId).getTopography().getChunkMap();
		
		// Bottom left
		if (tileX != 0 && tileY != 0) {
			recalculateOrientationForTileWithinSameChunk(tileX - 1, tileY - 1, foreGround, map);
		} else {
			if (tileX == 0 && tileY == 0) {
				recalculateOrientationForAdjascentTileAcrossChunks(fData.xChunkCoord - 1, fData.yChunkCoord - 1, CHUNK_SIZE - 1, CHUNK_SIZE - 1, foreGround, map);
			} else if (tileX == 0) {
				recalculateOrientationForAdjascentTileAcrossChunks(fData.xChunkCoord - 1, fData.yChunkCoord, CHUNK_SIZE - 1, tileY - 1, foreGround, map);
			} else {
				recalculateOrientationForAdjascentTileAcrossChunks(fData.xChunkCoord, fData.yChunkCoord - 1, tileX - 1, CHUNK_SIZE - 1, foreGround, map);
			}
		}
		
		// Top left
		if (tileX != 0 && tileY != CHUNK_SIZE - 1) {
			recalculateOrientationForTileWithinSameChunk(tileX - 1, tileY + 1, foreGround, map);
		} else {
			if (tileX == 0 && tileY == CHUNK_SIZE - 1) {
				recalculateOrientationForAdjascentTileAcrossChunks(fData.xChunkCoord - 1, fData.yChunkCoord + 1, CHUNK_SIZE - 1, 0, foreGround, map);
			} else if (tileX == 0) {
				recalculateOrientationForAdjascentTileAcrossChunks(fData.xChunkCoord - 1, fData.yChunkCoord, CHUNK_SIZE - 1, tileY + 1, foreGround, map);
			} else {
				recalculateOrientationForAdjascentTileAcrossChunks(fData.xChunkCoord, fData.yChunkCoord + 1, tileX - 1, 0, foreGround, map);
			}
		}
		
		// Top right
		if (tileX != CHUNK_SIZE - 1 && tileY != CHUNK_SIZE - 1) {
			recalculateOrientationForTileWithinSameChunk(tileX + 1, tileY + 1, foreGround, map);
		} else {
			if (tileX == CHUNK_SIZE - 1 && tileY == CHUNK_SIZE - 1) {
				recalculateOrientationForAdjascentTileAcrossChunks(fData.xChunkCoord + 1, fData.yChunkCoord + 1, 0, 0, foreGround, map);
			} else if (tileX == CHUNK_SIZE - 1) {
				recalculateOrientationForAdjascentTileAcrossChunks(fData.xChunkCoord + 1, fData.yChunkCoord, 0, tileY + 1, foreGround, map);
			} else {
				recalculateOrientationForAdjascentTileAcrossChunks(fData.xChunkCoord, tileX + 1, 0, 0, foreGround, map);
			}
		}
		
		// Bottom right
		if (tileX != CHUNK_SIZE - 1 && tileY != 0) {
			recalculateOrientationForTileWithinSameChunk(tileX + 1, tileY - 1, foreGround, map);
		} else {
			if (tileX == CHUNK_SIZE - 1 && tileY == 0) {
				recalculateOrientationForAdjascentTileAcrossChunks(fData.xChunkCoord + 1, fData.yChunkCoord - 1, 0, CHUNK_SIZE - 1, foreGround, map);
			} else if (tileX == CHUNK_SIZE - 1) {
				recalculateOrientationForAdjascentTileAcrossChunks(fData.xChunkCoord + 1, fData.yChunkCoord, 0, tileY - 1, foreGround, map);
			} else {
				recalculateOrientationForAdjascentTileAcrossChunks(fData.xChunkCoord, fData.yChunkCoord - 1, tileX + 1, CHUNK_SIZE - 1, foreGround, map);
			}
		}

		// Get the tile to the left
		if (tileX == 0) {
			recalculateOrientationForAdjascentTileAcrossChunks(fData.xChunkCoord - 1, fData.yChunkCoord, CHUNK_SIZE - 1, tileY, foreGround, map);
		} else {
			recalculateOrientationForTileWithinSameChunk(tileX - 1, tileY, foreGround, map);
		}

		// Get the tile to the right
		if (tileX == Topography.CHUNK_SIZE - 1) {
			recalculateOrientationForAdjascentTileAcrossChunks(fData.xChunkCoord + 1, fData.yChunkCoord, 0, tileY, foreGround, map);
		} else {
			recalculateOrientationForTileWithinSameChunk(tileX + 1, tileY, foreGround, map);
		}

		// Get the tile above
		if (tileY == Topography.CHUNK_SIZE - 1) {
			recalculateOrientationForAdjascentTileAcrossChunks(fData.xChunkCoord, fData.yChunkCoord + 1, tileX, 0, foreGround, map);
		} else {
			recalculateOrientationForTileWithinSameChunk(tileX, tileY + 1, foreGround, map);
		}

		// Get the tile below
		if (tileY == 0) {
			recalculateOrientationForAdjascentTileAcrossChunks(fData.xChunkCoord, fData.yChunkCoord - 1, tileX, CHUNK_SIZE - 1, foreGround, map);
		} else {
			recalculateOrientationForTileWithinSameChunk(tileX, tileY - 1, foreGround, map);
		}

		fData.tiles[tileX][tileY].calculateOrientationAndEdge(fData.xChunkCoord,fData.yChunkCoord, tileX, tileY, foreGround, map);
		
		refreshMesh();
	}
}