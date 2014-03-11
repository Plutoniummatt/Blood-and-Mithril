package bloodandmithril.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

/**
 * A light, holding data of the occlusion map and 1d shadow map
 *
 * @author Matt
 */
public class Light {

	/** World coords and size of this {@link Light} */
	public float x, y, spanBegin, spanEnd;
	public int size;
	public Color color;
	public float intensity;
	public boolean renderSwitch;

	/** Various {@link FrameBuffer}s */
	public FrameBuffer fOcclusion, mOcclusion, fShadowMap, mShadowMap;

	/**
	 * Constructor
	 * 
	 * SpanBegin - Counter-Clockwise, begining from the -ve x-axis, the begining angle of light span
	 * spanEnd - Counter-Clockwise, from spanEnd, the span, 1f meaning 360 degrees.
	 */
	public Light(int size, float x, float y, Color color, float intensity, float spanBegin, float spanEnd) {
		this.size = size;
		this.x = x;
		this.y = y;
		this.color = color;
		this.intensity = intensity;
		this.spanBegin = spanBegin;
		this.spanEnd = spanEnd;
	}
	
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof Light) {
			Light otherLight = (Light) other;
			return this.x == otherLight.x && this.y == otherLight.y && this.color.equals(otherLight.color) && this.intensity == otherLight.intensity;
		}
		
		return false;
	}
}