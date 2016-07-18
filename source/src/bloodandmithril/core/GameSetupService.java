package bloodandmithril.core;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.audio.SoundService;
import bloodandmithril.control.InputHandlers;
import bloodandmithril.control.keydown.GameSpeedControlKeyPressedHandler;
import bloodandmithril.control.keydown.IndividualUIKeyPressedHandler;
import bloodandmithril.control.leftclick.CoreInGameLeftClickHandler;
import bloodandmithril.control.rightclick.CancelCursorBoundTaskRightClickHandler;
import bloodandmithril.control.rightclick.InGameContextMenuSpawner;
import bloodandmithril.control.rightclick.IndividualControlRightClickHandler;
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

	@Inject private InputHandlers inputHandlers;
	@Inject private PositionalIndexingService positionalIndexingService;
	@Inject private UserInterface userInterface;

	public void setup() {
		SoundService.changeMusic(2f, SoundService.desertAmbient);
		userInterface.contextMenus.clear();
		positionalIndexingService.reindex();
		userInterface.loadBars();
		userInterface.loadButtons();
		addAdditionalInputHandlers();
	}


	private void addAdditionalInputHandlers() {
		// Add additional input handlers
		// WARNING: Ordering here is very important
		inputHandlers.addKeyPressedHandler(IndividualUIKeyPressedHandler.class);
		inputHandlers.addKeyPressedHandler(GameSpeedControlKeyPressedHandler.class);

		inputHandlers.addLeftClickHandler(CoreInGameLeftClickHandler.class);

		inputHandlers.addRightClickHandler(CancelCursorBoundTaskRightClickHandler.class);
		inputHandlers.addRightClickHandler(InGameContextMenuSpawner.class);
		inputHandlers.addRightClickHandler(IndividualControlRightClickHandler.class);
	}
}
