package bloodandmithril.control;

import java.util.Map;

import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;

/**
 * Tracks camera position
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class CameraTracker {

	private final Map<Integer, Vector2> worldCamCoordinates = Maps.newHashMap();

	public Map<Integer, Vector2> getWorldcamcoordinates() {
		return worldCamCoordinates;
	}
}