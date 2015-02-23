package bloodandmithril.graphics.background;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;

import java.util.TreeMap;

import bloodandmithril.core.Copyright;
import bloodandmithril.util.Shaders;
import bloodandmithril.world.weather.Weather;

import com.badlogic.gdx.graphics.Color;
import com.google.common.collect.Maps;

/**
 * Layer that is filtered according to daylight color
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class DayLightColorLayer extends Layer {
	private static final long serialVersionUID = 1539143531802061321L;

	/**
	 * Constructor
	 */
	public DayLightColorLayer() {
		super(getMap());
	}
	
	
	private static TreeMap<Integer, Integer> getMap() {
		TreeMap<Integer, Integer> map = Maps.newTreeMap();
		
		map.put(0, 2);
		for (int i = 500; i < 5000; i += 200) {
			map.put(i, 1);
		}
		for (int i = -200; i > -5000; i -= 200) {
			map.put(i, 1);
		}
		
		return map;
	}


	@Override
	public void preRender() {
		spriteBatch.setShader(Shaders.filter);
		Color daylightColor = Weather.getDaylightColor();
		Shaders.filter.setUniformf("color", Weather.getSunColor().mul(new Color(daylightColor.r, daylightColor.r, daylightColor.r, 1f)));
	}


	@Override
	public float getDistanceX() {
		return 0.9f;
	}


	@Override
	public float getDistanceY() {
		return 0.75f;
	}


	@Override
	public float getOffsetY() {
		return 100;
	}
}