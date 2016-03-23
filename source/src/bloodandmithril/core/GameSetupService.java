package bloodandmithril.core;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.audio.SoundService;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.performance.PositionalIndexingService;
import bloodandmithril.ui.UserInterface;

/**
 * Service used for various setup
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class GameSetupService {

	@Inject Graphics graphics;

	public void setup() {
		SoundService.changeMusic(2f, SoundService.desertAmbient);
		UserInterface.contextMenus.clear();
		PositionalIndexingService.reindex();
		UserInterface.loadBars();
		graphics.getUi().loadButtons();
	}
}
