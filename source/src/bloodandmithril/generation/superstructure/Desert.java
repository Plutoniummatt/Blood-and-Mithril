package bloodandmithril.generation.superstructure;

import static bloodandmithril.world.topography.Topography.convertToChunkCoord;
import static java.lang.Math.max;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.generation.ChunkGenerator;
import bloodandmithril.generation.Structures;
import bloodandmithril.generation.component.components.Corridor;
import bloodandmithril.generation.component.components.Corridor.CorridorCreationCustomization;
import bloodandmithril.generation.component.components.Room;
import bloodandmithril.generation.component.components.Room.RoomCreationCustomization;
import bloodandmithril.generation.component.components.Stairs;
import bloodandmithril.generation.component.components.Stairs.StairsCreationCustomization;
import bloodandmithril.generation.component.components.prefab.DesertPyramid;
import bloodandmithril.generation.component.components.prefab.UndergroundDesertTempleEntrance;
import bloodandmithril.generation.tools.PerlinNoiseGenerator1D;
import bloodandmithril.generation.tools.RectangularSpaceCalculator;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.prop.plant.DeadDesertBush;
import bloodandmithril.prop.plant.grass.GrassWithLongThinYellowFlowers;
import bloodandmithril.prop.plant.grass.GrassWithYellowFlower;
import bloodandmithril.prop.plant.grass.GrassyWhiteFlowers;
import bloodandmithril.prop.plant.grass.GreenGrass;
import bloodandmithril.prop.plant.tree.TestTree;
import bloodandmithril.util.Function;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.util.Util;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.util.datastructure.TwoInts;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.tiles.brick.GreyBrickTile;
import bloodandmithril.world.topography.tile.tiles.brick.YellowBrickTile;
import bloodandmithril.world.topography.tile.tiles.soil.StandardSoilTile;

/**
 * The structure of a desert surface to be stored and used to generate when needed
 *
 * @author Sam, Matt
 */
@Copyright("Matthew Peck 2014")
public class Desert extends SuperStructure {
	private static final long serialVersionUID = 4034191268168150728L;

	/** Generates the wavey surface of the desert */
	private final PerlinNoiseGenerator1D perlinSurfaceGenerator = new PerlinNoiseGenerator1D(30, Wiring.injector().getInstance(ParameterPersistenceService.class).getParameters().getSeed(), 1, 0f);

	/** Dimensions of this {@link Desert} */
	private int cWidth, cHeight, tDuneVariationHeight;


	/**
	 * @param worldId - The ID of the world.
	 * @param cWidth - The Width of the desert in chunks.
	 * @param cHeight - The Height of the desert in chunks.
	 * @param tDuneVariationHeight - How much the surface height can vary by.
	 */
	public Desert(int worldId, int cWidth, int cHeight, int tDuneVariationHeight) {
		super(worldId);
		this.cWidth = cWidth;
		this.cHeight = cHeight;
		this.tDuneVariationHeight = tDuneVariationHeight;
	}


	@Override
	protected Boundaries findSpace(int startingChunkX, int startingChunkY) {
		//calculates where the structure can go
		return RectangularSpaceCalculator.calculateBoundariesConfineWithinTwoHeights(
			true,
			startingChunkX,
			startingChunkY,
			cWidth,
			cHeight,
			ChunkGenerator.maxSurfaceHeightInChunks,
			ChunkGenerator.maxSurfaceHeightInChunks - cHeight,
			Domain.getWorld(worldId).getTopography()
		);
	}


	@Override
	protected void internalGenerate(boolean generatingToRight) {
		int rightMostTile = Topography.convertToWorldTileCoord(getBoundaries().right, 19);
		int leftMostTile = Topography.convertToWorldTileCoord(getBoundaries().left, 0);

		generateSurface(generatingToRight, rightMostTile, leftMostTile);
		generateDungeon();
	}


	/** Generates the desert dungeon */
	private void generateDungeon() {

		// Add pyraid somewhere
		int pyramidX = (getBoundaries().left + 3) * Topography.CHUNK_SIZE;
		int pyramidY = max(
			getSurfaceHeight().apply(184 + pyramidX) + 70,
			getSurfaceHeight().apply(24 + pyramidX) + 70
		);

		getComponents().add(new DesertPyramid(
			pyramidX,
			pyramidY,
			getStructureKey(),
			YellowBrickTile.class,
			YellowBrickTile.class
		));

		// Add the entrance in the middle of this desert
		int entranceX = (getBoundaries().left + getBoundaries().right) / 2 * Topography.CHUNK_SIZE;
		int entranceY = max(
			getSurfaceHeight().apply(366 + entranceX) + 80,
			getSurfaceHeight().apply(24 + entranceX) + 80
		);

		getComponents().add(new UndergroundDesertTempleEntrance(
			entranceX,
			entranceY,
			getStructureKey(),
			false,
			GreyBrickTile.class,
			GreyBrickTile.class
		));

		startingLocations.add(new TwoInts(entranceX + 40, entranceY - 40));

		getComponents().get(0).stem(
			this,
			Stairs.class,
			new Function<StairsCreationCustomization>() {
				@Override
				public StairsCreationCustomization call() {
					return new StairsCreationCustomization(
						false,
						false,
						Util.getRandom().nextBoolean(),
						Util.getRandom().nextInt(30) + 20,
						1,
						17,
						3,
						YellowBrickTile.class,
						YellowBrickTile.class
					);
				}
			}
		).stem(
			this,
			Stairs.class,
			new Function<StairsCreationCustomization>() {
				@Override
				public StairsCreationCustomization call() {
					return new StairsCreationCustomization(
						Util.getRandom().nextBoolean(),
						Util.getRandom().nextBoolean(),
						Util.getRandom().nextBoolean(),
						Util.getRandom().nextInt(30) + 20,
						1,
						17,
						3,
						YellowBrickTile.class,
						YellowBrickTile.class
					);
				}
			}
		).stem(
			this,
			Stairs.class,
			new Function<StairsCreationCustomization>() {
				@Override
				public StairsCreationCustomization call() {
					return new StairsCreationCustomization(
						Util.getRandom().nextBoolean(),
						Util.getRandom().nextBoolean(),
						true,
						Util.getRandom().nextInt(30) + 20,
						1,
						17,
						3,
						YellowBrickTile.class,
						YellowBrickTile.class
					);
				}
			}
		).stem(
			this,
			Corridor.class,
			new Function<CorridorCreationCustomization>() {
				@Override
				public CorridorCreationCustomization call() {
					return new CorridorCreationCustomization(
						Util.getRandom().nextBoolean(),
						3,
						3,
						20 + Util.getRandom().nextInt(10),
						17,
						YellowBrickTile.class
					);
				}
			}
		).stem(
			this,
			Room.class,
			new Function<RoomCreationCustomization>() {
				@Override
				public RoomCreationCustomization call() {
					return new RoomCreationCustomization(
						Util.getRandom().nextBoolean(),
						20 + Util.getRandom().nextInt(10),
						20 + Util.getRandom().nextInt(10),
						3,
						YellowBrickTile.class
					);
				}
			}
		).stem(
			this,
			Stairs.class,
			new Function<StairsCreationCustomization>() {
				@Override
				public StairsCreationCustomization call() {
					return new StairsCreationCustomization(
						Util.getRandom().nextBoolean(),
						Util.getRandom().nextBoolean(),
						Util.getRandom().nextBoolean(),
						Util.getRandom().nextInt(20) + 15,
						1,
						17,
						3,
						YellowBrickTile.class,
						YellowBrickTile.class
					);
				}
			},
			10
		);
	}


	/** Generates the surface layer */
	protected void generateSurface(boolean generatingToRight, int rightMostTile, int leftMostTile) {
		Structures structures = Domain.getWorld(worldId).getTopography().getStructures();
		int startingHeight;
		int dafaultSurfaceHeight = 100;

		// set starting height
		if (generatingToRight) {
			SuperStructure superStructure = (SuperStructure) structures.getStructure(convertToChunkCoord(leftMostTile - 1), 0, true);
			if (superStructure != null && superStructure.getSurfaceHeight().apply(leftMostTile - 1) != null) {
				startingHeight = superStructure.getSurfaceHeight().apply(leftMostTile - 1);
			} else {
				startingHeight = dafaultSurfaceHeight;
			}
		} else {
			SuperStructure superStructure = (SuperStructure) structures.getStructure(convertToChunkCoord(rightMostTile + 1), 0, true);
			if (superStructure != null && superStructure.getSurfaceHeight().apply(rightMostTile + 1) != null) {
				startingHeight = superStructure.getSurfaceHeight().apply(rightMostTile + 1);
			} else {
				startingHeight = dafaultSurfaceHeight;
			}
		}

		setSurfaceHeight(new DesertSurfaceFunction(startingHeight, generatingToRight, rightMostTile, leftMostTile));

		//place props
		for (int x = leftMostTile; x <= rightMostTile; x++) {
			if (Util.roll(0.04f)) {
				Structures.get(getStructureKey()).addProp(
					new DeadDesertBush(
						Topography.convertToWorldCoord(x, false),
						Topography.convertToWorldCoord(getSurfaceHeight().apply(x), false) + 16
					)
				);
			}
			if (Util.roll(0.04f)) {
				Structures.get(getStructureKey()).addProp(
					new GrassyWhiteFlowers(
						Topography.convertToWorldCoord(x, false),
						Topography.convertToWorldCoord(getSurfaceHeight().apply(x), false) + 16,
						Util.getRandom().nextBoolean()
					)
				);
			}
			if (Util.roll(0.04f)) {
				Structures.get(getStructureKey()).addProp(
					new GrassWithLongThinYellowFlowers(
						Topography.convertToWorldCoord(x, false),
						Topography.convertToWorldCoord(getSurfaceHeight().apply(x), false) + 16,
						Util.getRandom().nextBoolean()
					)
				);
			}
			if (Util.roll(0.02f)) {
				Structures.get(getStructureKey()).addProp(
						new GrassWithYellowFlower(
						Topography.convertToWorldCoord(x, false),
						Topography.convertToWorldCoord(getSurfaceHeight().apply(x), false) + 16,
						Util.getRandom().nextBoolean()
					)
				);
			}
			if (Util.roll(0.08f)) {
				Structures.get(getStructureKey()).addProp(
					new TestTree(
						Topography.convertToWorldCoord(x, false),
						Topography.convertToWorldCoord(getSurfaceHeight().apply(x), false) + 16,
						0.95f
					)
				);
			}
			if (Util.roll(1f)) {
				Structures.get(getStructureKey()).addProp(
					new GreenGrass(
						Topography.convertToWorldCoord(x, false),
						Topography.convertToWorldCoord(getSurfaceHeight().apply(x), false) + 16,
						Util.getRandom().nextBoolean()
					)
				);
			}
		}
	}


	@Override
	protected Tile internalGetForegroundTile(int worldTileX, int worldTileY) {
		if (worldTileY > getSurfaceHeight().apply(worldTileX)) {
			return new Tile.EmptyTile();
		} else {
			return new StandardSoilTile();
		}
	}


	@Override
	protected Tile internalGetBackgroundTile(int worldTileX, int worldTileY) {
		if (worldTileY > getSurfaceHeight().apply(worldTileX)-1) {
			return new Tile.EmptyTile();
		} else {
			return new StandardSoilTile();
		}
	}


	public class DesertSurfaceFunction extends SerializableMappingFunction<Integer, Integer> {
		private static final long serialVersionUID = 2757677577748105968L;

		private int startingHeight;
		private boolean generatingToRight;
		private int rightMostTile;
		private int leftMostTile;

		private DesertSurfaceFunction(int startingHeight, boolean generatingToRight, int rightMostTile, int leftMostTile) {
			this.startingHeight = startingHeight;
			this.generatingToRight = generatingToRight;
			this.rightMostTile = rightMostTile;
			this.leftMostTile = leftMostTile;
		}

		@Override
		public Integer apply(Integer x) {
			return (int)(startingHeight + tDuneVariationHeight * perlinSurfaceGenerator.generate(x, 1) - tDuneVariationHeight * perlinSurfaceGenerator.generate(generatingToRight ? leftMostTile : rightMostTile, 1));
		}
	}
}