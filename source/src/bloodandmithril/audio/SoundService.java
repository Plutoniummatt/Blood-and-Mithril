package bloodandmithril.audio;


import static bloodandmithril.networking.ClientServerInterface.isClient;
import static bloodandmithril.networking.ClientServerInterface.isServer;
import static bloodandmithril.util.datastructure.WrapperForThree.wrap;

import java.util.Map;

import bloodandmithril.character.ai.perception.Listener;
import bloodandmithril.character.ai.perception.Observer;
import bloodandmithril.character.ai.perception.SoundStimulus;
import bloodandmithril.character.ai.perception.Visible;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.Function;
import bloodandmithril.util.datastructure.WrapperForThree;
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
	public static final int anvil1				= 10;
	public static final int anvil2				= 11;

	static {

	}

	private static Map<Integer, WrapperForThree<Function<SoundStimulus>, com.badlogic.gdx.audio.Sound, Float>> sounds = Maps.newHashMap();

	private static Music current, next;

	private static float decreasing, increasing, rate;

	private static float volume = 1f;

	private static boolean fadeOut;

	static {
		if (isClient()) {
			mainMenu = Gdx.audio.newMusic(Gdx.files.internal("data/music/mainMenu.mp3"));
			desertNight = Gdx.audio.newMusic(Gdx.files.internal("data/music/desertNight.mp3"));
		}

		sounds.put(pickAxe, 			wrap(() -> {return new SuspiciousSound(null, SuspicionLevel.INVESTIGATE);}, 			!isClient() ? null : Gdx.audio.newSound(Gdx.files.internal("data/music/pickAxe.wav")), 			1300f));
		sounds.put(swordSlash, 			wrap(() -> {return new SuspiciousSound(null, SuspicionLevel.INVESTIGATE_CAUTION);}, 	!isClient() ? null : Gdx.audio.newSound(Gdx.files.internal("data/music/swordSlash.wav")), 		300f));
		sounds.put(femaleHit, 			wrap(() -> {return new SuspiciousSound(null, SuspicionLevel.INVESTIGATE);}, 			!isClient() ? null : Gdx.audio.newSound(Gdx.files.internal("data/music/femaleHit.wav")), 		500f));
		sounds.put(stab, 				wrap(() -> {return new SuspiciousSound(null, SuspicionLevel.INVESTIGATE);}, 			!isClient() ? null : Gdx.audio.newSound(Gdx.files.internal("data/music/stab.wav")), 			100f));
		sounds.put(broadSwordBlock, 	wrap(() -> {return new SuspiciousSound(null, SuspicionLevel.INVESTIGATE_CAUTION);}, 	!isClient() ? null : Gdx.audio.newSound(Gdx.files.internal("data/music/broadSwordBlock.wav")), 	1000f));
		sounds.put(crunch, 				wrap(() -> {return new SuspiciousSound(null, SuspicionLevel.INVESTIGATE);}, 			!isClient() ? null : Gdx.audio.newSound(Gdx.files.internal("data/music/crunch.wav")), 			100f));
		sounds.put(swallow,				wrap(() -> {return new SuspiciousSound(null, SuspicionLevel.PAUSE);}, 					!isClient() ? null : Gdx.audio.newSound(Gdx.files.internal("data/music/swallow.wav")), 			20f));
		sounds.put(flint,				wrap(() -> {return new SuspiciousSound(null, SuspicionLevel.PAUSE);}, 					!isClient() ? null : Gdx.audio.newSound(Gdx.files.internal("data/music/flint.wav")), 			200f));
		sounds.put(campfireCooking,		wrap(() -> {return new SuspiciousSound(null, SuspicionLevel.INVESTIGATE);}, 			!isClient() ? null : Gdx.audio.newSound(Gdx.files.internal("data/music/campfireCooking.wav")), 	300f));
		sounds.put(anvil1,				wrap(() -> {return new SuspiciousSound(null, SuspicionLevel.INVESTIGATE);}, 			!isClient() ? null : Gdx.audio.newSound(Gdx.files.internal("data/music/anvil1.wav")), 			1300f));
		sounds.put(anvil2,				wrap(() -> {return new SuspiciousSound(null, SuspicionLevel.INVESTIGATE);}, 			!isClient() ? null : Gdx.audio.newSound(Gdx.files.internal("data/music/anvil2.wav")), 			1300f));
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


	public static void play(int sound, Vector2 location, boolean requiresServerAuthority, Visible source) {
		if (sound == -1) {
			return;
		}

		if (isServer()) {
			triggerListeners(location, sounds.get(sound).c, sound, source);
			if (isClient()) {
				sounds.get(sound).b.play(getVolume(location), 1f, getPan(location));
			} else if (requiresServerAuthority) {
				ClientServerInterface.SendNotification.notifyPlaySound(-1, sound, location);
			}
		} else if (!requiresServerAuthority) {
			sounds.get(sound).b.play(getVolume(location), 1f, getPan(location));
		}
	}


	public static Function<SoundStimulus> getSoundStimulusFunction(int id) {
		return sounds.get(id).a;
	}


	private static void triggerListeners(Vector2 location, float triggerRadius, int sound, Visible source) {
		Domain.getActiveWorld().getPositionalIndexMap().getEntitiesWithinBounds(
			Individual.class,
			location.x - triggerRadius,
			location.x + triggerRadius,
			location.y + triggerRadius,
			location.y - triggerRadius
		).forEach(individualId -> {
			Individual listener = Domain.getIndividual(individualId);
			if (listener == null || listener.isAISuppressed() || !(listener instanceof Listener) || listener.getState().position.cpy().dst(location) > triggerRadius || listener.isSelected()) {
				return;
			}

			SoundStimulus stimulus = sounds.get(sound).a.call();
			stimulus.setEmissionPosition(location);

			if (source != null && listener instanceof Observer) {
				if (!((Listener) listener).reactIfVisible(stimulus) && source.isVisibleTo((Observer) listener, Domain.getWorld(listener.getWorldId()))) {
					return;
				}
			}

			listener.getAI().addStimulus(stimulus);
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
	 * A suspicious sound.
	 *
	 * @author Matt
	 */
	public static class SuspiciousSound implements SoundStimulus {
		private static final long serialVersionUID = 6466600820010007897L;
		private final SuspicionLevel suspicionLevel;
		private Vector2 position;

		/**
		 * Constructor
		 */
		public SuspiciousSound(Vector2 position, SuspicionLevel suspicionLevel) {
			this.position = position;
			this.suspicionLevel = suspicionLevel;
		}

		@Override
		public Vector2 getEmissionPosition() {
			return position;
		}

		public SuspicionLevel getSuspicionLevel() {
			return suspicionLevel;
		}

		@Override
		public void setEmissionPosition(Vector2 position) {
			this.position = position;
		}
	}


	public static enum SuspicionLevel {
		NONE(0), PAUSE(1), INVESTIGATE(2), INVESTIGATE_CAUTION(3), BACKUP(4), FLEE(5);
		public final int severity;

		private SuspicionLevel(int severity) {
			this.severity = severity;
		}
	}
}
