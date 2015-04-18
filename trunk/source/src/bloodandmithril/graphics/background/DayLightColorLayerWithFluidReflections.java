package bloodandmithril.graphics.background;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;

import java.util.TreeMap;

import bloodandmithril.core.Copyright;
import bloodandmithril.util.Shaders;

import com.google.common.collect.Maps;

/**
 * Layer that is filtered according to daylight color, and with reflective fluid surfaces
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class DayLightColorLayerWithFluidReflections extends Layer {
	private static final long serialVersionUID = 1539143531802061321L;

	/**
	 * Constructor
	 */
	public DayLightColorLayerWithFluidReflections() {
		super(getMap());
	}


	private static TreeMap<Integer, Integer> getMap() {
		TreeMap<Integer, Integer> map = Maps.newTreeMap();

		map.put(400, 2);
		for (int i = 873; i < 5473; i += 200) {
			map.put(i, 0);
		}
		for (int i = 200; i > -5000; i -= 200) {
			map.put(i, 0);
		}

		return map;
	}


	@Override
	public void preRender() {
		spriteBatch.setShader(Shaders.pass);
	}


	@Override
	public float getDistanceX() {
		return 0.95f;
	}


	@Override
	public float getDistanceY() {
		return 0.95f;
	}


	@Override
	public float getOffsetY() {
		return 390;
	}
}