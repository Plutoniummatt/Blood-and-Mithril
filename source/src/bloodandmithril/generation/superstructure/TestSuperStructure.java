package bloodandmithril.generation.superstructure;

import static bloodandmithril.world.topography.Topography.convertToChunkCoord;
import static java.lang.Math.max;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.generation.ChunkGenerator;
import bloodandmithril.generation.Structures;
import bloodandmithril.generation.component.Component;
import bloodandmithril.generation.component.components.prefab.DesertPyramid;
import bloodandmithril.generation.component.components.prefab.UndergroundDesertTempleEntrance;
import bloodandmithril.generation.component.components.stemming.interfaces.StemmingDirection;
import bloodandmithril.generation.component.components.stemming.room.Room;
import bloodandmithril.generation.component.components.stemming.room.RoomBuilder;
import bloodandmithril.generation.tools.PerlinNoiseGenerator1D;
import bloodandmithril.generation.tools.RectangularSpaceCalculator;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.prop.plant.DeadDesertBush;
import bloodandmithril.prop.plant.grass.GrassWithLongThinYellowFlowers;
import bloodandmithril.prop.plant.grass.GrassWithYellowFlower;
import bloodandmithril.prop.plant.grass.GrassyWhiteFlowers;
import bloodandmithril.prop.plant.grass.GreenGrass;
import bloodandmithril.prop.plant.grass.TallGrass;
import bloodandmithril.prop.plant.tree.alder.AlderTree;
import bloodandmithril.util.Operator;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.util.Util;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.util.datastructure.TwoInts;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.tiles.brick.GreyBrickTile;
import bloodandmithril.world.topography.tile.tiles.brick.YellowBrickTile;
import bloodandmithril.world.topography.tile.tiles.soil.OrdinarySoilTile;

/**
 * The structure of a desert surface to be stored and used to generate when needed
 *
 * @author Sam, Matt
 */
@Copyright("Matthew Peck 2014")
public class TestSuperStructure extends SuperStructure {
	private static final long serialVersionUID = 4034191268168150728L;

	/** Generates the wavey surface of the desert */
	private final PerlinNoiseGenerator1D perlinSurfaceGenerator = new PerlinNoiseGenerator1D(30, Wiring.injector().getInstance(ParameterPersistenceService.class).getParameters().getSeed(), 1, 0f);

	/** Dimensions of this {@link TestSuperStructure} */
	private int cWidth, cHeight, tDuneVariationHeight;


	/**
	 * @param worldId - The ID of the world.
	 * @param cWidth - The Width of the desert in chunks.
	 * @param cHeight - The Height of the desert in chunks.
	 * @param tDuneVariationHeight - How much the surface height can vary by.
	 */
	public TestSuperStructure(final int worldId, final int cWidth, final int cHeight, final int tDuneVariationHeight) {
		super(worldId);
		this.cWidth = cWidth;
		this.cHeight = cHeight;
		this.tDuneVariationHeight = tDuneVariationHeight;
	}


	@Override
	protected Boundaries findSpace(final int startingChunkX, final int startingChunkY) {
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
	protected void internalGenerate(final boolean generatingToRight) {
		final int rightMostTile = Topography.convertToWorldTileCoord(getBoundaries().right, 19);
		final int leftMostTile = Topography.convertToWorldTileCoord(getBoundaries().left, 0);

		generateSurface(generatingToRight, rightMostTile, leftMostTile);
		generateDungeon();
	}


	/** Generates the desert dungeon */
	private void generateDungeon() {
		// Add pyraid somewhere
		addPyramid();

		Operator<Component> roomFunction = component -> {
			component
			.stemFrom()
			.fromAnyInterface()
			.withDirection(Util.randomOneOf(StemmingDirection.RIGHT, StemmingDirection.LEFT))
			.specifyOffset(0)
			.using(RoomBuilder.class)
			.withHeight(Util.getRandom().nextInt(5) + 10)
			.withWidth(Util.getRandom().nextInt(5) + 10)
			.withWallThickness(2)
			.withWallTile(GreyBrickTile.class, fTile -> {fTile.changeToSmoothCeiling();}, bTile -> {})
			.withStructureKey(getStructureKey())
			.build();
		};
		
		Operator<Component> horizontalCorridorFunction = component -> {
			component
			.stemFrom()
			.fromAnyInterface()
			.withDirection(Util.randomOneOf(StemmingDirection.LEFT, StemmingDirection.RIGHT))
			.specifyOffset(0)
			.using(RoomBuilder.class)
			.withHeight(7)
			.withWidth(Util.getRandom().nextInt(25) + 10)
			.withWallThickness(1)
			.withWallTile(GreyBrickTile.class, fTile -> {fTile.changeToSmoothCeiling();}, bTile -> {})
			.withStructureKey(getStructureKey())
			.withStemmingFunction(roomFunction)
			.build();
		};
		
		Operator<Component> verticalCorridorFunction = component -> {
			component
			.stemFrom()
			.fromAnyInterface()
			.withDirection(StemmingDirection.DOWN)
			.specifyOffset(Util.getRandom().nextInt(100))
			.using(RoomBuilder.class)
			.withHeight(Util.getRandom().nextInt(20) + 50)
			.withWidth(7)
			.withWallThickness(1)
			.withWallTile(GreyBrickTile.class, fTile -> {fTile.changeToSmoothCeiling();}, bTile -> {})
			.withStructureKey(getStructureKey())
			.withStemmingFunction(horizontalCorridorFunction)
			.build();
		};
		
		addTemple()
		.stemFrom()
		.fromAnyInterface()
		.withDirection(StemmingDirection.DOWN)
		.specifyOffset(-Util.getRandom().nextInt(150))
		.using(RoomBuilder.class)
		.withHeight(7)
		.withWidth(Util.getRandom().nextInt(150) + 100)
		.withWallThickness(1)
		.withWallTile(GreyBrickTile.class, fTile -> {fTile.changeToSmoothCeiling();}, bTile -> {})
		.withStructureKey(getStructureKey())
		.withStemmingFunction(verticalCorridorFunction)
		.build();
	}
	
	
	private Component addTemple() {
		final int entranceX = (getBoundaries().left + getBoundaries().right) / 2 * Topography.CHUNK_SIZE;
		final int entranceY = max(
			getSurfaceHeight().apply(366 + entranceX) + 80,
			getSurfaceHeight().apply(24 + entranceX) + 80
		);

		UndergroundDesertTempleEntrance undergroundDesertTempleEntrance = new UndergroundDesertTempleEntrance(
			entranceX,
			entranceY,
			getStructureKey(),
			false,
			GreyBrickTile.class,
			GreyBrickTile.class
		);
		undergroundDesertTempleEntrance.generateInterfaces();
		getComponents().add(undergroundDesertTempleEntrance);
		
		Room room = new RoomBuilder()
		.withHeight(10)
		.withWidth(20)
		.withWallThickness(2)
		.withWallTile(GreyBrickTile.class, fTile -> {}, bTile -> {})
		.withBottomLeftCorner(entranceY - 80, entranceX + 100)
		.withStructureKey(getStructureKey())
		.build()
		.get();
		getComponents().add(room);
		
		startingLocations.add(new TwoInts(entranceX + 40, entranceY - 40));
		
		return undergroundDesertTempleEntrance;
	}


	private void addPyramid() {
		final int pyramidX = (getBoundaries().left + 3) * Topography.CHUNK_SIZE;
		final int pyramidY = max(
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
	}


	/** Generates the surface layer */
	protected void generateSurface(final boolean generatingToRight, final int rightMostTile, final int leftMostTile) {
		final Structures structures = Domain.getWorld(worldId).getTopography().getStructures();
		int startingHeight;
		final int dafaultSurfaceHeight = 100;

		// set starting height
		if (generatingToRight) {
			final SuperStructure superStructure = (SuperStructure) structures.getStructure(convertToChunkCoord(leftMostTile - 1), 0, true);
			if (superStructure != null && superStructure.getSurfaceHeight().apply(leftMostTile - 1) != null) {
				startingHeight = superStructure.getSurfaceHeight().apply(leftMostTile - 1);
			} else {
				startingHeight = dafaultSurfaceHeight;
			}
		} else {
			final SuperStructure superStructure = (SuperStructure) structures.getStructure(convertToChunkCoord(rightMostTile + 1), 0, true);
			if (superStructure != null && superStructure.getSurfaceHeight().apply(rightMostTile + 1) != null) {
				startingHeight = superStructure.getSurfaceHeight().apply(rightMostTile + 1);
			} else {
				startingHeight = dafaultSurfaceHeight;
			}
		}

		setSurfaceHeight(new DesertSurfaceFunction(startingHeight, generatingToRight, rightMostTile, leftMostTile));
		placeProps(rightMostTile, leftMostTile);
	}


	private void placeProps(final int rightMostTile, final int leftMostTile) {
		for (int x = leftMostTile; x <= rightMostTile; x++) {
			if (Util.roll(0.04f)) {
				Structures.get(getStructureKey()).addProp(
					new DeadDesertBush(
						Topography.convertToWorldCoord(x, false),
						Topography.convertToWorldCoord(getSurfaceHeight().apply(x), false) + 16
					)
				);
			}
			if (Util.roll(0.1f)) {
				Structures.get(getStructureKey()).addProp(
					new TallGrass(
						Topography.convertToWorldCoord(x, false),
						Topography.convertToWorldCoord(getSurfaceHeight().apply(x), false) + 16,
						Util.getRandom().nextBoolean()
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
					new AlderTree(
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
	protected Tile internalGetForegroundTile(final int worldTileX, final int worldTileY) {
		if (worldTileY > getSurfaceHeight().apply(worldTileX)) {
			return new Tile.EmptyTile();
		} else {
			final OrdinarySoilTile ordinarySoilTile = new OrdinarySoilTile();
			ordinarySoilTile.changeToSmoothCeiling();
			return ordinarySoilTile;
		}
	}


	@Override
	protected Tile internalGetBackgroundTile(final int worldTileX, final int worldTileY) {
		if (worldTileY > getSurfaceHeight().apply(worldTileX)-1) {
			return new Tile.EmptyTile();
		} else {
			final OrdinarySoilTile ordinarySoilTile = new OrdinarySoilTile();
			ordinarySoilTile.changeToSmoothCeiling();
			return ordinarySoilTile;
		}
	}


	public class DesertSurfaceFunction extends SerializableMappingFunction<Integer, Integer> {
		private static final long serialVersionUID = 2757677577748105968L;

		private int startingHeight;
		private boolean generatingToRight;
		private int rightMostTile;
		private int leftMostTile;

		private DesertSurfaceFunction(final int startingHeight, final boolean generatingToRight, final int rightMostTile, final int leftMostTile) {
			this.startingHeight = startingHeight;
			this.generatingToRight = generatingToRight;
			this.rightMostTile = rightMostTile;
			this.leftMostTile = leftMostTile;
		}

		@Override
		public Integer apply(final Integer x) {
			return (int)(startingHeight + tDuneVariationHeight * perlinSurfaceGenerator.generate(x, 1) - tDuneVariationHeight * perlinSurfaceGenerator.generate(generatingToRight ? leftMostTile : rightMostTile, 1));
		}
	}
}