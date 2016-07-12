package bloodandmithril.core;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.google.inject.Singleton;

import bloodandmithril.audio.SoundService;
import bloodandmithril.objectives.Mission;
import bloodandmithril.ui.UserInterface;

/**
 * Tracks missions
 * TODO - Persist this
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class MissionTracker {

	/** Current camera coordinates for each world */
	private final Collection<Mission> missions = new ConcurrentLinkedDeque<Mission>();


	public void addMission(final Mission m) {
		missions.add(m);

		SoundService.play(SoundService.newMission);
		Wiring.injector().getInstance(Threading.class).clientProcessingThreadPool.submit(() -> {
			for (int i = 0; i < 5; i++) {
				UserInterface.addUIFloatingText(
					"New mission!",
					Color.ORANGE,
					new Vector2(220, 60)
				);
				try {
					Thread.sleep(1000);
				} catch (final Exception e) {}
			}
		});
	}


	public Collection<Mission> getMissions() {
		return missions;
	}
}
