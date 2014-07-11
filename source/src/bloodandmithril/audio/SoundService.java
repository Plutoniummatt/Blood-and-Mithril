package bloodandmithril.audio;


import static bloodandmithril.csi.ClientServerInterface.isClient;
import static bloodandmithril.csi.ClientServerInterface.isServer;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.csi.ClientServerInterface;

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

	public static Music mainMenu;

	public static Sound pickAxe;
	public static Sound swordSlash;
	public static Sound femaleHit;
	public static Sound stab;
	public static Sound broadSwordBlock;

	private static Music current, next;

	private static float decreasing, increasing, rate;

	private static float volume = 1f;

	private static boolean fadeOut;

	static {
		if (ClientServerInterface.isClient()) {
			mainMenu = Gdx.audio.newMusic(Gdx.files.internal("data/music/mainMenu.mp3"));

			pickAxe = Gdx.audio.newSound(Gdx.files.internal("data/music/pickAxe.wav"));
			swordSlash = Gdx.audio.newSound(Gdx.files.internal("data/music/swordSlash.wav"));
			femaleHit = Gdx.audio.newSound(Gdx.files.internal("data/music/femaleHit.wav"));
			stab = Gdx.audio.newSound(Gdx.files.internal("data/music/stab.wav"));
			broadSwordBlock = Gdx.audio.newSound(Gdx.files.internal("data/music/broadSwordBlock.wav"));
		}
	}

	/** Returns the pan value in relation to camera location */
	public static float getPan(Vector2 location) {
		if (!ClientServerInterface.isClient()) {
			return 0f;
		}

		float panValue = (location.x - BloodAndMithrilClient.cam.position.x) / (BloodAndMithrilClient.WIDTH / 2);

		if (panValue > 0f) {
			return Math.min(panValue, 0.99f);
		} else {
			return Math.max(panValue, -0.99f);
		}
	}


	public static void play(Sound sound, float volume, float pitch, float pan, boolean requiresServerAuthority) {
		if (isServer()) {
			if (isClient()) {
				sound.play(volume, pitch, pan);
			} else if (requiresServerAuthority) {
				// Notify clients
			}
		} else if (!requiresServerAuthority) {
			sound.play(volume, pitch, pan);
		}
	}


	/** Update the music transition timer */
	public static void update(float delta) {
		decreasing = decreasing - delta / rate < 0f ? 0f : decreasing - delta / rate;
		increasing = increasing + delta / rate > volume ? volume : increasing + delta / rate;

		if (next != null) {
			next.setVolume(increasing);
			if (!next.isPlaying()) {
				next.setLooping(true);
				next.play();
			}
		}

		if (current != null) {
			if (next == null) {
				current.setVolume(volume);
			} else {
				current.setVolume(decreasing);
			}

			if (fadeOut) {
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


	/** Returns the volume in relation to camera location */
	public static float getVolume(Vector2 location) {
		if (!ClientServerInterface.isClient()) {
			return 0f;
		}

		Vector2 camPos = new Vector2(BloodAndMithrilClient.cam.position.x, BloodAndMithrilClient.cam.position.y);

		float distance = Math.abs(location.cpy().sub(camPos).len());
		float volume = Math.max(1f - distance / BloodAndMithrilClient.WIDTH, 0f);

		return volume;
	}


	public static void setVolumne(float volume) {
		SoundService.volume = volume;
	}


	public static void changeMusic(float transitionTime, Music toChangeTo) {
		fadeOut = false;

		if (toChangeTo == current || decreasing != 0f) {
			return;
		}

		decreasing = 1f;
		increasing = 0f;

		rate = transitionTime;
		next = toChangeTo;
	}


	public static void fadeOut(float transitionTime) {
		if (decreasing != 0f) {
			return;
		}

		fadeOut = true;

		decreasing = 1f;
		increasing = 0f;

		rate = transitionTime;
		next = null;
	}
}
