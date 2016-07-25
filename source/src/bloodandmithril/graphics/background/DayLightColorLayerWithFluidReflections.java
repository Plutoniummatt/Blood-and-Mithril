package bloodandmithril.graphics.background;

import java.util.TreeMap;

import com.google.common.collect.Maps;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.datastructure.WrapperForTwo;

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
		super(getMap(), true);
	}


	private static TreeMap<Integer, WrapperForTwo<Integer, Integer>> getMap() {
		TreeMap<Integer, WrapperForTwo<Integer, Integer>> map = Maps.newTreeMap();

		map.put(400, WrapperForTwo.wrap(BackgroundRenderingService.FLOATING_ISLAND_CASTLE, 160));
		map.put(572, WrapperForTwo.wrap(BackgroundRenderingService.SHIP, 157));

		for (int i = 630; i < 10630; i += 200) {
			map.put(i, WrapperForTwo.wrap(BackgroundRenderingService.EMPTY, 0));
		}

		for (int i = 200; i > -5000; i -= 200) {
			map.put(i, WrapperForTwo.wrap(BackgroundRenderingService.EMPTY, 0));
		}

		return map;
	}


	@Override
	public void preRender(Graphics graphics) {
		graphics.getSpriteBatch().setShader(Shaders.pass);
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
		return -160;
	}
}