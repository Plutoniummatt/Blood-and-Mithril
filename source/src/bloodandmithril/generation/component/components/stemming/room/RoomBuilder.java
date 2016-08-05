package bloodandmithril.generation.component.components.stemming.room;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.component.components.stemming.ComponentBuilder;

/**
 * Handles the construction of {@link Room}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class RoomBuilder implements ComponentBuilder<Room> {

	private Integer width;
	private Integer height;

	/**
	 * @param width of the room to build
	 */
	public RoomBuilder withWidth(final int width) {
		this.width = width;
		return this;
	}


	/**
	 * @param height of the room to build
	 */
	public RoomBuilder withHeight(final int height) {
		this.height = height;
		return this;
	}


	@Override
	public Room build() {
		if (width == null || height == null) {
			throw new IllegalStateException();
		}

		return new Room(
			null,
			0
		);
	}
}