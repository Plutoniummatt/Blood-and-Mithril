package bloodandmithril.generation.component.components.stemming.room;

import static java.lang.Math.max;
import static java.lang.Math.min;

import com.google.common.base.Optional;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.component.components.stemming.ComponentBuilder;
import bloodandmithril.generation.component.components.stemming.interfaces.HorizontalInterface;
import bloodandmithril.generation.component.components.stemming.interfaces.VerticalInterface;
import bloodandmithril.util.Operator;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.topography.tile.Tile;

/**
 * Handles the construction of {@link Room}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class RoomBuilder extends ComponentBuilder<Room> {

	private Optional<Integer> width = Optional.absent();
	private Optional<Integer> height = Optional.absent();
	private Optional<Integer> wallThickness = Optional.absent();
	
	private Optional<Integer> bottom = Optional.absent();
	private Optional<Integer> left = Optional.absent();
	
	private Optional<Class<? extends Tile>> wallTile = Optional.absent();
	
	private Operator<Tile> fTileManipulator = tile -> {};
	private Operator<Tile> bTileManipulator = tile -> {};

	/**
	 * @param width of the EMPTY PART of the room to build
	 */
	public RoomBuilder withWidth(final int width) {
		this.width = Optional.of(width);
		return this;
	}


	/**
	 * @param height of the of the EMPTY PART of the room to build
	 */
	public RoomBuilder withHeight(final int height) {
		this.height = Optional.of(height);
		return this;
	}
	
	
	/**
	 * @param wallThickness of the room to build
	 */
	public RoomBuilder withWallThickness(final int wallThickness) {
		this.wallThickness = Optional.of(wallThickness);
		return this;
	}
	
	
	/**
	 * @param wallThickness of the room to build
	 */
	public RoomBuilder withWallTile(final Class<? extends Tile> wallTile, Operator<Tile> fTileManipulator, Operator<Tile> bTileManipulator) {
		this.wallTile = Optional.of(wallTile);
		this.fTileManipulator = fTileManipulator;
		this.bTileManipulator = bTileManipulator;
		return this;
	}
	
	
	/**
	 * @param specify the bottom left corner of the EMPTY PART of the room
	 */
	public RoomBuilder withBottomLeftCorner(final int bottom, final int left) {
		this.bottom = Optional.of(bottom);
		this.left = Optional.of(left);
		return this;
	}


	@Override
	protected Room buildComponent() {
		if (!width.isPresent() || !height.isPresent() || !wallThickness.isPresent() || !structureKey.isPresent() || !wallTile.isPresent()) {
			throw new IllegalStateException();
		}
		
		if (interfaceToStemFrom.isPresent()) {
			if (bottom.isPresent() || left.isPresent()) {
				throw new IllegalStateException();
			}
			
			return buildFromInterface(interfaceToStemFrom.get());
		} else {
			if (!bottom.isPresent() || !left.isPresent()) {
				throw new IllegalStateException();
			}
			
			return new Room(
				new Boundaries(
					bottom.get() + height.get() + 2 * wallThickness.get(), 
					bottom.get(), 
					left.get(), 
					left.get() + width.get() + 2 * wallThickness.get()
				),
				structureKey.get(),
				wallThickness.get(),
				wallTile.get(),
				fTileManipulator,
				bTileManipulator
			);
		}
	}


	@Override
	protected Room buildComponentFrom(VerticalInterface verticalInterface) {
		int offset = 0;
		if (height.get() < verticalInterface.height) {
			offset = min(max(0, interfaceOffset), verticalInterface.height - height.get() - 1); 
		} else {
			offset = max(min(0, interfaceOffset), verticalInterface.height - height.get() - 1); 
		}
		
		switch (verticalInterface.getStemmingDirection()) {
			case LEFT:
				return new Room(
					new Boundaries(
						verticalInterface.tileY + height.get() + wallThickness.get() + offset,
						verticalInterface.tileY - wallThickness.get() + offset,
						verticalInterface.tileX - width.get() - wallThickness.get(),
						verticalInterface.tileX + wallThickness.get()
					), 
					structureKey.get(), 
					wallThickness.get(), 
					wallTile.get(),
					fTileManipulator,
					bTileManipulator
				);
			
			case RIGHT:
				return new Room(
					new Boundaries(
						verticalInterface.tileY + height.get() + wallThickness.get() + offset,
						verticalInterface.tileY - wallThickness.get() + offset,
						verticalInterface.tileX - wallThickness.get(),
						verticalInterface.tileX + width.get() + wallThickness.get()
					), 
					structureKey.get(), 
					wallThickness.get(), 
					wallTile.get(),
					fTileManipulator,
					bTileManipulator
				);
				
			default: 
				throw new UnsupportedOperationException();
		}
	}


	@Override
	protected Room buildComponentFrom(HorizontalInterface horizontalInterface) {
		int offset = 0;
		if (width.get() < horizontalInterface.width) {
			offset = min(max(0, interfaceOffset), horizontalInterface.width - width.get() - 1); 
		} else {
			offset = max(min(0, interfaceOffset), horizontalInterface.width - width.get() - 1); 
		}
		
		switch (horizontalInterface.getStemmingDirection()) {
			case UP:
				return new Room(
					new Boundaries(
						horizontalInterface.tileY - height.get() - wallThickness.get(),
						horizontalInterface.tileY + wallThickness.get(),
						horizontalInterface.tileX - wallThickness.get() + offset,
						horizontalInterface.tileX + width.get() + wallThickness.get() + offset
					), 
					structureKey.get(), 
					wallThickness.get(), 
					wallTile.get(),
					fTileManipulator,
					bTileManipulator
				);
			
			case DOWN:
				return new Room(
					new Boundaries(
						horizontalInterface.tileY + wallThickness.get(),
						horizontalInterface.tileY - height.get() - wallThickness.get(),
						horizontalInterface.tileX - wallThickness.get() + offset,
						horizontalInterface.tileX + width.get() + wallThickness.get() + offset
					), 
					structureKey.get(), 
					wallThickness.get(), 
					wallTile.get(),
					fTileManipulator,
					bTileManipulator
				);
				
			default: 
				throw new UnsupportedOperationException();
		}
	}
}