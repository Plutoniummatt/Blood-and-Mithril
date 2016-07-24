package bloodandmithril.world.weather;

import java.io.Serializable;

import bloodandmithril.core.Copyright;

/**
 * Cloud
 * 
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class Cloud implements Serializable {
	private static final long serialVersionUID = -8421278935167280306L;
	
	public final int cloudTextureId;
	public final int height;
	
	private float xPosition;
	
	/**
	 * Constructor
	 */
	public Cloud(int cloudTextureId, int height, float xPosition) {
		this.cloudTextureId = cloudTextureId;
		this.height = height;
		this.xPosition = xPosition;
	}

	
	public float getxPosition() {
		return xPosition;
	}
	

	public void setxPosition(float xPosition) {
		this.xPosition = xPosition;
	}
}