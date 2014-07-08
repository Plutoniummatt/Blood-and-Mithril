package bloodandmithril.util;

import bloodandmithril.csi.ClientServerInterface;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Helper class to load animations
 *
 * @author Matt
 */
public class AnimationHelper {


	public static Animation animation(Texture tex, int startX, int startY, int width, int height, int frames, float duration, int playMode) {
		if (ClientServerInterface.isClient()) {
			TextureRegion[] regions = new TextureRegion[frames];

			for (int i = 0; i < frames; i++) {
				regions[i] = new TextureRegion(tex, startX + i * width, startY, width, height);
			}
			Animation animation = new Animation(duration, regions);
			animation.setPlayMode(playMode);

			return animation;
		} else {
			TextureRegion[] regions = new TextureRegion[frames];
			Animation animation = new Animation(duration, regions);
			animation.setPlayMode(playMode);

			return animation;
		}
	}
}
