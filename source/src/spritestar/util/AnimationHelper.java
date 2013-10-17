package spritestar.util;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Helper class to load animations
 *
 * @author Matt
 */
public class AnimationHelper {

	
	public static Animation makeAnimation(Texture tex, int startX, int startY, int width, int height, int frames, float duration) {
		TextureRegion[] regions = new TextureRegion[frames];
		
		for (int i = 0; i < frames; i++) {
			regions[i] = new TextureRegion(tex, startX + i * width, startY, width, height);
		}
		return new Animation(duration, regions);
	}
}
