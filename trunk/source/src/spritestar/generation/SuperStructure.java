package spritestar.generation;

import spritestar.world.topography.tile.Tile;

/**
 * Super {@link Structure} - This should always exist as long as its on screen
 *
 * @author Matt
 */
public abstract class SuperStructure extends Structure {


	/**
	 * @see spritestar.generation.Structure#internalGenerate()
	 */
	@Override
	protected void internalGenerate() {
		// TODO Auto-generated method stub
	}


	/**
	 * @see spritestar.generation.Structure#addToStructureMap(boolean)
	 */
	@Override
	public void addToStructureMap() {
		// TODO Auto-generated method stub
	}


	/**
	 * @see spritestar.generation.Structure#calculateChunksToGenerate()
	 */
	@Override
	public void calculateChunksToGenerate() {
		// TODO Auto-generated method stub
	}


	/**
	 * @see spritestar.generation.Structure#getForegroundTile(int, int)
	 */
	@Override
	public Class<? extends Tile> getForegroundTile(int tileX, int tileY) {
		// TODO Auto-generated method stub
		return null;
	}


	/**
	 * @see spritestar.generation.Structure#getBackgroundTile(int, int)
	 */
	@Override
	public Class<? extends Tile> getBackgroundTile(int tileX, int tileY) {
		// TODO Auto-generated method stub
		return null;
	}
}