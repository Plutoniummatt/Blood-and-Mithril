package bloodandmithril.audio;


import static bloodandmithril.networking.ClientServerInterface.isClient;
import static bloodandmithril.networking.ClientServerInterface.isServer;

import java.util.Map;

import bloodandmithril.character.ai.perception.Listener;
import bloodandmithril.character.ai.perception.SoundStimulus;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Maps;

/**
 * Master class for audio-related things
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class SoundService {

	public static Music mainMenu, desertNight;

	public static final int pickAxe 			= 1;
	public static final int swordSlash 			= 2;
	public static final int femaleHit 			= 3;
	public static final int stab 				= 4;
	public static final int broadSwordBlock 	= 5;
	public static final int crunch 				= 6;
	public static final int swallow				= 7;
	public static final int flint				= 8;
	public static final int campfireCooking		= 9;
	public static final int anvil				= 10;

	private static Map<Integer, com.badlogic.gdx.audio.Sound> sounds = Maps.newHashMap();

	private static Music current, next;

	private static float decreasing, increasing, rate;

	private static float volume = 1f;

	private static boolean fadeOut;

	static {
		if (ClientServerInterface.isClient()) {
			mainMenu = Gdx.audio.newMusic(Gdx.files.internal("data/music/mainMenu.mp3"));
			desertNight = Gdx.audio.newMusic(Gdx.files.internal("data/music/desertNight.mp3"));

			sounds.put(pickAxe, 			Gdx.audio.newSound(Gdx.files.internal("data/music/pickAxe.wav")));
			sounds.put(swordSlash, 			Gdx.audio.newSound(Gdx.files.internal("data/music/swordSlash.wav")));
			sounds.put(femaleHit, 			Gdx.audio.newSound(Gdx.files.internal("data/music/femaleHit.wav")));
			sounds.put(stab, 				Gdx.audio.newSound(Gdx.files.internal("data/music/stab.wav")));
			sounds.put(broadSwordBlock, 	Gdx.audio.newSound(Gdx.files.internal("data/music/broadSwordBlock.wav")));
			sounds.put(crunch, 				Gdx.audio.newSound(Gdx.files.internal("data/music/crunch.wav")));
			sounds.put(swallow,				Gdx.audio.newSound(Gdx.files.internal("data/music/swallow.wav")));
			sounds.put(flint,				Gdx.audio.newSound(Gdx.files.internal("data/music/flint.wav")));
			sounds.put(campfireCooking,		Gdx.audio.newSound(Gdx.files.internal("data/music/campfireCooking.wav")));
			sounds.put(anvil,				Gdx.audio.newSound(Gdx.files.internal("data/music/anvil.wav")));
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


	public static void play(int sound, Vector2 location, float triggerRadius, boolean requiresServerAuthority) {
		if (sound == -1) {
			return;
		}

		if (isServer()) {
			triggerListeners(location, triggerRadius);
			if (isClient()) {
				sounds.get(sound).play(getVolume(location), 1f, getPan(location));
			} else if (requiresServerAuthority) {
				ClientServerInterface.SendNotification.notifyPlaySound(-1, sound, location);
			}
		} else if (!requiresServerAuthority) {
			sounds.get(sound).play(getVolume(location), 1f, getPan(location));
		}
	}


	private static void triggerListeners(Vector2 location, float triggerRadius) {
		Domain.getActiveWorld().getPositionalIndexMap().getEntitiesWithinBounds(
			Individual.class,
			location.x - triggerRadius,
			location.x + triggerRadius,
			location.y + triggerRadius,
			location.y - triggerRadius
		).forEach(individualId -> {
			Individual listener = Domain.getIndividual(individualId);
			if (listener == null || !(listener instanceof Listener) || listener.getState().position.cpy().dst(location) > triggerRadius) {
				return;
			}


		});
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
			}
			current = next;
			next = null;
		}
	}


	/** Returns the volume in relation to camera location */
	private static float getVolume(Vector2 location) {
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

		if (toChangeTo == current) {
			return;
		}

		if (current == null) {
			current = toChangeTo;
			decreasing = 0f;
			increasing = 0f;
		} else {
			decreasing = 1f;
			increasing = 0f;
		}


		rate = transitionTime;
		next = toChangeTo;
	}


	public static void fadeOut(float transitionTime) {
		if (next != null) {
			current = next;
		}

		decreasing = 1f;
		increasing = 0f;

		fadeOut = true;
		rate = transitionTime;
		next = null;
	}


	/**
	 * Sound class that contains sound ID and methods related to perception of sounds by {@link Listener}s
	 *
	 * @author Matt
	 */
	public abstract static class Sound {
		public final int soundId;

		/**
		 * Constructor
		 */
		public Sound(int soundId) {
			this.soundId = soundId;
		}


		/**
		 * @return the {@link SoundStimulus}
		 */
		public abstract SoundStimulus getStimulus();
	}
}
