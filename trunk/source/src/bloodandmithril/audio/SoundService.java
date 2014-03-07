package bloodandmithril.audio;


import bloodandmithril.BloodAndMithrilClient;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;

/**
 * Master class for audio-related things
 *
 * @author Matt
 */
public class SoundService {

	public static Music music1 = Gdx.audio.newMusic(Gdx.files.internal("data/music/music1.mp3"));
	
	public static Sound pickAxe = Gdx.audio.newSound(Gdx.files.internal("data/music/pickAxe.wav"));
	
	private static Music current, next;
	
	private static float decreasing, increasing, rate;
	
	/** Returns the pan value in relation to camera location */
	public static float getPan(Vector2 location) {
		float panValue = (location.x - BloodAndMithrilClient.cam.position.x) / (BloodAndMithrilClient.WIDTH / 2);
		
		if (panValue > 0f) {
			return Math.min(panValue, 1f);
		} else {
			return Math.max(panValue, -1f);
		}
	}
	
	
	/** Update the music transition timer */
	public static void update(float delta) {
		decreasing = decreasing - (delta / rate) < 0f ? 0f : decreasing - (delta / rate);
		increasing = increasing + (delta / rate) > 1f ? 1f : increasing + (delta / rate);
		
		if (next != null) {
			next.setVolume(increasing);
			if (!next.isPlaying()) {
				next.setLooping(true);
				next.play();
			}
		}
		
		if (current != null) {
			if (next == null) {
				current.setVolume(1f);
			} else {
				current.setVolume(decreasing);
			}
			
			if (!current.isPlaying()) {
				current.setLooping(true);
				current.play();
			}
		}
		
		if (increasing == 1f && next != null) {
			if (current != null) {
				current.stop();
				current.dispose();
			}
			current = next;
			next = null;
		}
	}
	
	
	/** Reutns the volume in relation to camera location */
	public static float getVolumne(Vector2 location) {
		Vector2 camPos = new Vector2(BloodAndMithrilClient.cam.position.x, BloodAndMithrilClient.cam.position.y);
		
		float distance = Math.abs(location.cpy().sub(camPos).len());
		float volume = Math.max(1f - distance / BloodAndMithrilClient.WIDTH, 0f);
		
		return volume;
	}
	
	
	public static void changeMusic(float transitionTime, Music toChangeTo) {
		if (toChangeTo == current || decreasing != 0f) {
			return;
		}
		
		decreasing = 1f;
		increasing = 0f;
		
		rate = transitionTime;
		next = toChangeTo;
	}
}
