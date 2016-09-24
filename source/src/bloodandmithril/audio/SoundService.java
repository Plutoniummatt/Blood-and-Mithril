package bloodandmithril.audio;


import static bloodandmithril.networking.ClientServerInterface.isClient;
import static bloodandmithril.networking.ClientServerInterface.isServer;
import static bloodandmithril.util.datastructure.WrapperForThree.wrap;

import java.io.Serializable;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Maps;

import bloodandmithril.character.ai.perception.Listener;
import bloodandmithril.character.ai.perception.Observer;
import bloodandmithril.character.ai.perception.SoundStimulus;
import bloodandmithril.character.ai.perception.Visible;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.Function;
import bloodandmithril.util.datastructure.WrapperForThree;
import bloodandmithril.world.Domain;

/**
 * Master class for audio-related things
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class SoundService {

	public static Music mainMenu, desertAmbient;

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
	public static final int femaleGoing			= 12;
	public static final int femaleOk			= 13;
	public static final int newMission			= 14;
	public static final int achievement			= 15;

	private static final Map<Integer, WrapperForThree<Function<SoundStimulus>, com.badlogic.gdx.audio.Sound, Float>> sounds = Maps.newHashMap();

	private static Music current, next;

	private static float decreasing, increasing, rate;

	private static float volume = 1f;

	private static boolean fadeOut;

	static {
		if (isClient()) {
			mainMenu = Gdx.audio.newMusic(Gdx.files.internal("data/music/mainMenu.mp3"));
			desertAmbient = Gdx.audio.newMusic(Gdx.files.internal("data/music/desertNight.mp3"));
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
		sounds.put(femaleGoing,			wrap(() -> {return new SuspiciousSound(null, SuspicionLevel.INVESTIGATE);}, 			!isClient() ? null : Gdx.audio.newSound(Gdx.files.internal("data/music/going.wav")), 			50f));
		sounds.put(femaleOk,			wrap(() -> {return new SuspiciousSound(null, SuspicionLevel.INVESTIGATE);}, 			!isClient() ? null : Gdx.audio.newSound(Gdx.files.internal("data/music/ok.wav")), 				50f));
		sounds.put(newMission,			wrap(() -> {return new SuspiciousSound(null, SuspicionLevel.NONE);}, 					!isClient() ? null : Gdx.audio.newSound(Gdx.files.internal("data/music/newMission.wav")),		0f));
		sounds.put(achievement,			wrap(() -> {return new SuspiciousSound(null, SuspicionLevel.NONE);}, 					!isClient() ? null : Gdx.audio.newSound(Gdx.files.internal("data/music/achievement.wav")),		0f));
	}

	/** Returns the pan value in relation to camera location */
	public static final float getPan(final Vector2 location, final Graphics graphics) {
		if (!ClientServerInterface.isClient()) {
			return 0f;
		}

		final float panValue = (location.x - graphics.getCam().position.x) / (graphics.getWidth() / 2);

		if (panValue > 0f) {
			return Math.min(panValue, 0.99f);
		} else {
			return Math.max(panValue, -0.99f);
		}
	}


	public static final void play(final int sound) {
		sounds.get(sound).b.play(1f, 1f, 0f);
	}


	public static final void play(final int sound, final Vector2 location, final boolean requiresServerAuthority, final Visible source) {
		if (sound == -1) {
			return;
		}

		if (isServer()) {
			triggerListeners(location, sounds.get(sound).c, sound, source);
			if (isClient()) {
				final Graphics graphics = Wiring.injector().getInstance(Graphics.class);
				sounds.get(sound).b.play(getVolume(location, graphics), 1f, getPan(location, graphics));
			} else if (requiresServerAuthority) {
				ClientServerInterface.SendNotification.notifyPlaySound(-1, sound, location);
			}
		} else if (!requiresServerAuthority) {
			final Graphics graphics = Wiring.injector().getInstance(Graphics.class);
			sounds.get(sound).b.play(getVolume(location, graphics), 1f, getPan(location, graphics));
		}
	}


	public static final Function<SoundStimulus> getSoundStimulusFunction(final int id) {
		return sounds.get(id).a;
	}


	private static final void triggerListeners(final Vector2 location, final float triggerRadius, final int sound, final Visible source) {
		final GameClientStateTracker gameClientStateTracker = Wiring.injector().getInstance(GameClientStateTracker.class);
		Wiring.injector().getInstance(GameClientStateTracker.class).getActiveWorld().getPositionalIndexChunkMap().getEntitiesWithinBounds(
			Individual.class,
			location.x - triggerRadius,
			location.x + triggerRadius,
			location.y + triggerRadius,
			location.y - triggerRadius
		).forEach(individualId -> {
			final Individual listener = Domain.getIndividual(individualId);
			if (listener == null || listener.isAISuppressed() || !(listener instanceof Listener) || listener.getState().position.cpy().dst(location) > triggerRadius || gameClientStateTracker.isIndividualSelected(listener)) {
				return;
			}

			final SoundStimulus stimulus = sounds.get(sound).a.call();
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
	public static final void update(final float delta) {
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
	private static final float getVolume(final Vector2 location, final Graphics graphics) {
		if (!ClientServerInterface.isClient()) {
			return 0f;
		}

		final Vector2 camPos = new Vector2(graphics.getCam().position.x, graphics.getCam().position.y);

		final float distance = Math.abs(location.cpy().sub(camPos).len());
		final float volume = Math.max(1f - distance / graphics.getWidth(), 0f);

		return volume;
	}


	public static final void setVolumne(final float volume) {
		SoundService.volume = volume;
	}


	public static final void changeMusic(final float transitionTime, final Music toChangeTo) {
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


	public static final void fadeOut(final float transitionTime) {
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
	public static final class SuspiciousSound implements SoundStimulus {
		private static final long serialVersionUID = 6466600820010007897L;
		private final SuspicionLevel suspicionLevel;
		private Vector2 position;

		/**
		 * Constructor
		 */
		public SuspiciousSound(final Vector2 position, final SuspicionLevel suspicionLevel) {
			this.position = position;
			this.suspicionLevel = suspicionLevel;
		}

		@Override
		public final Vector2 getEmissionPosition() {
			return position;
		}

		public final SuspicionLevel getSuspicionLevel() {
			return suspicionLevel;
		}

		@Override
		public final void setEmissionPosition(final Vector2 position) {
			this.position = position;
		}

		@Override
		public final void stimulate(final Individual individual) {
			// TODO Auto-generated method stub
		}
	}


	public static enum SuspicionLevel implements Serializable {
		NONE(0), PAUSE(1), INVESTIGATE(2), INVESTIGATE_CAUTION(3), BACKUP(4), FLEE(5);
		public final int severity;

		private SuspicionLevel(final int severity) {
			this.severity = severity;
		}
	}
}
