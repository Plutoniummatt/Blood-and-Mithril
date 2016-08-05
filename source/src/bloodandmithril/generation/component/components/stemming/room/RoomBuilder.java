package bloodandmithril.generation.component.components.stemming.room;

import com.google.common.base.Optional;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.component.components.stemming.ComponentBuilder;
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
	public RoomBuilder withWallTile(final Class<? extends Tile> wallTile) {
		this.wallTile = Optional.of(wallTile);
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
	public Room build() {
		if (!width.isPresent() || !height.isPresent() || !wallThickness.isPresent() || !structureKey.isPresent() || !wallTile.isPresent()) {
			throw new IllegalStateException();
		}
		
		if (toStemFrom.isPresent()) {
			if (bottom.isPresent() || left.isPresent()) {
				throw new IllegalStateException();
			}
			
			return new Room(
				new Boundaries(
					bottom.get() + height.get() + wallThickness.get(), 
					bottom.get(), 
					left.get(), 
					left.get() + width.get() + wallThickness.get()
				),
				structureKey.get(),
				wallThickness.get(),
				wallTile.get()
			);
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
				wallTile.get()
			);
		}
	}
}