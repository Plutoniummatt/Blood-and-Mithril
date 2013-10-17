package spritestar.world.generation;

import java.util.ArrayList;

import spritestar.util.datastructure.Boundaries;
import spritestar.util.datastructure.TwoInts;
import spritestar.world.topography.Topography;
import spritestar.world.topography.tile.Tile;

/**
 * A {@link SubStructure} is a {@link Structure} that is generated on top of {@link SuperStructure}s
 *
 * @author Matt
 */
public abstract class SubStructure extends Structure {
	private static final long serialVersionUID = -7186088747059184017L;

	/** A list of the chunk coordinates this {@link SubStructure} occupies */
	private final ArrayList<TwoInts> occupiedChunkSpace = new ArrayList<TwoInts>();

	/** The {@link Boundaries} of the {@link SuperStructure} this {@link SubStructure} exists within */
	protected Boundaries superStructureBoundaries;

	@Override
	public void generate(int startingChunkX, int startingChunkY, boolean generatingToRight) {
		generateStructure(generatingToRight);
	}


	@Override
	protected boolean checkComponent(Component component) {
		ArrayList<TwoInts> componentSpace = new ArrayList<TwoInts>();
		for (int x = Topography.convertToChunkCoord(component.boundaries.left); x <= Topography.convertToChunkCoord(component.boundaries.right); x++) {
			for (int y = Topography.convertToChunkCoord(component.boundaries.bottom); y <= Topography.convertToChunkCoord(component.boundaries.top); y++) {
				TwoInts chunkCoord = new TwoInts(x, y);
				if(!occupiedChunkSpace.contains(chunkCoord)) {
					if (!StructureMap.doesSubStructureExist(x, y)) {
					componentSpace.add(chunkCoord);
					} else {
						return false;
					}
				}
			}
		}
		occupiedChunkSpace.addAll(componentSpace);
		return true;
	}


	@Override
	public Tile getForegroundTile(int worldTileX, int worldTileY) {
		return ComponentService.getTileFromComponentPile(components, worldTileX, worldTileY, true);
	}


	@Override
	public Tile getBackgroundTile(int worldTileX, int worldTileY) {
		return ComponentService.getTileFromComponentPile(components, worldTileX, worldTileY, false);
	}


	/** Put the Substructure in the appropriate places in {@link StructureMap} */
	@Override
  protected int addToStructureMap() {
		return StructureMap.addSubStructure(occupiedChunkSpace, this);
	}


	@Override
	protected void calculateChunksToGenerate() {
		chunksLeftToBeGenerated = occupiedChunkSpace.size();
	}
}