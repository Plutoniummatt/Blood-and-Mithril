package bloodandmithril.util;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.base.Function;

/**
 * Helper class to load animations
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class AnimationHelper {


	public static Animation animation(Texture tex, int startX, int startY, int width, int height, int frames, float duration, PlayMode playMode) {
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
	
	
	/**
	 * Utility class to determine the correct animation to use, depending on function
	 *
	 * @author Matt
	 */
	@Copyright("Matthew Peck 2015")
	public static class AnimationSwitcher {
		public LinkedHashMap<Function<Individual, Boolean>, Animation> animations = new LinkedHashMap<Function<Individual,Boolean>, Animation>();
		
		public Animation getAnimation(Individual individual) {
			Animation def = null;
			
			for (Entry<Function<Individual, Boolean>, Animation> entry : animations.entrySet()) {
				def = entry.getValue();
				if (entry.getKey().apply(individual)) {
					return def;
				}
			}
			
			return def;
		}
	}
}
