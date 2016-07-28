package bloodandmithril.world.weather;

import java.io.Serializable;

import bloodandmithril.core.Copyright;

/**
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class WeatherState implements Serializable {
	private static final long serialVersionUID = 1718151357740098361L;
	
	/** -1f means strongest right->left wind, +1f means strongest left->right wind */
	private float wind;
	
	/**
	 * Constructor
	 */
	public WeatherState() {
	}

	
	public float getWind() {
		return wind;
	}

	
	public void setWind(float wind) {
		this.wind = wind;
	}
}