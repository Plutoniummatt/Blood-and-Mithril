package bloodandmithril.generation.component.components.stemming.room;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.component.components.stemming.ComponentStemmer;

/**
 * {@link ComponentStemmer} that creates {@link Room}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class RoomFactory implements ComponentStemmer<RoomBuilder, Room> {

	private final RoomBuilder roomBuilder = new RoomBuilder();

	@Override
	public RoomBuilder create() {
		return roomBuilder;
	}


	@Override
	public Room build() {
		return roomBuilder.build();
	}
}