package bloodandmithril.ui.components.window;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.google.inject.Inject;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.control.BloodAndMithrilClientInputProcessor;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Util.Colors;

/**
 * {@link Window} with controls relevant to the selected entity
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class SelectedIndividualsControlWindow extends Window {

	@Inject
	private GameClientStateTracker gameClientStateTracker;

	HashMap<Integer, Button> buttons = newHashMap();

	/**
	 * Constructor
	 */
	public SelectedIndividualsControlWindow(final int x, final int y, final int length, final int height, final String title, final boolean active) {
		super(x, y, length, height, title, active, length, height, true, false, true);
		setupButtons();
		setAlwaysActive(true);
	}


	/**
	 * Sets up the buttons for this {@link SelectedIndividualsControlWindow}
	 */
	private void setupButtons() {
		buttons.put(0, new Button(
			"Run",
			Fonts.defaultFont,
			0,
			0,
			40,
			16,
			() -> {
				final boolean run = gameClientStateTracker.getSelectedIndividuals().stream().mapToInt(indi -> {
					return indi.isWalking() ? 0 : 1;
				}).sum() > 0;

				for (final Individual individual : gameClientStateTracker.getSelectedIndividuals()) {
					if (ClientServerInterface.isServer()) {
						individual.setWalking(run);
					} else {
						ClientServerInterface.SendRequest.sendRunWalkRequest(individual.getId().getId(), run);
					}
				}
			},
			Color.WHITE,
			Color.GREEN,
			Color.WHITE,
			UIRef.BL
		));

		buttons.put(1, new Button(
			"Shut up",
			Fonts.defaultFont,
			0,
			0,
			40,
			16,
			() -> {
				final boolean someoneSpeaking = gameClientStateTracker.getSelectedIndividuals().stream().mapToInt(individual -> {
					return individual.isShutUp() ? 0 : 1;
				}).sum() > 0;

				for (final Individual individual : gameClientStateTracker.getSelectedIndividuals()) {
					if (ClientServerInterface.isServer()) {
						individual.setShutUp(someoneSpeaking);
					} else {
						ClientServerInterface.SendRequest.sendIndividualSpeakRequest(individual, !someoneSpeaking);
					}
				}
			},
			Color.WHITE,
			Color.GREEN,
			Color.WHITE,
			UIRef.BL
		));

		buttons.put(2, new Button(
			"Disable AI",
			Fonts.defaultFont,
			0,
			0,
			40,
			16,
			() -> {
				final boolean someoneHasAISuppressed = gameClientStateTracker.getSelectedIndividuals().stream().mapToInt(individual -> {
					return individual.isAISuppressed() ? 1 : 0;
				}).sum() > 0;

				for (final Individual individual : gameClientStateTracker.getSelectedIndividuals()) {
					if (ClientServerInterface.isServer()) {
						individual.setAISuppression(!someoneHasAISuppressed);
					} else {
						ClientServerInterface.SendRequest.sendAISuppressionRequest(individual, !someoneHasAISuppressed);
					}
				}
			},
			Color.WHITE,
			Color.GREEN,
			Color.WHITE,
			UIRef.BL
		));
	}


	@Override
	protected void internalWindowRender(final Graphics graphics) {
		final boolean someoneRunning = gameClientStateTracker.getSelectedIndividuals().stream().mapToInt(individual -> {
			return individual.isWalking() ? 0 : 1;
		}).sum() > 0;

		final boolean someoneSpeaking = gameClientStateTracker.getSelectedIndividuals().stream().mapToInt(individual -> {
			return individual.isShutUp() ? 0 : 1;
		}).sum() > 0;

		final boolean someoneHasAISuppressed = gameClientStateTracker.getSelectedIndividuals().stream().mapToInt(individual -> {
			return individual.isAISuppressed() ? 1 : 0;
		}).sum() > 0;

		final boolean buttonsActive = UserInterface.getLayeredComponents().isEmpty() ? false : UserInterface.getLayeredComponents().getLast() == this;
		final boolean selected = gameClientStateTracker.getSelectedIndividuals().size() > 0;

		// Run button
		buttons.get(0).text = someoneRunning ? () -> {return "Walk";} : () -> {return "Run";};
		buttons.get(0).setIdleColor(selected ? someoneRunning ? Color.GREEN : Color.ORANGE : Colors.UI_GRAY);
		buttons.get(0).setOverColor(selected ? buttonsActive ? someoneRunning ? Color.ORANGE : Color.GREEN : someoneRunning ? Color.GREEN : Color.ORANGE : Colors.UI_GRAY);
		buttons.get(0).setDownColor(selected ? buttonsActive ? Color.WHITE : someoneRunning ? Color.GREEN : Color.ORANGE : Colors.UI_GRAY);
		buttons.get(0).render(x + 27, y - 20, isActive(), isActive() ? getAlpha() : getAlpha() * 0.6f, graphics);

		// Shut up button
		buttons.get(1).text = someoneSpeaking ? () -> {return "Shut up";} : () -> {return "Speak";};
		buttons.get(1).setIdleColor(selected ? someoneSpeaking ? Color.ORANGE : Color.GREEN : Colors.UI_GRAY);
		buttons.get(1).setOverColor(selected ? buttonsActive ? someoneSpeaking ? Color.GREEN : Color.ORANGE : someoneSpeaking ? Color.ORANGE : Color.GREEN : Colors.UI_GRAY);
		buttons.get(1).setDownColor(selected ? buttonsActive ? Color.WHITE : someoneSpeaking ? Color.GREEN : Color.ORANGE : Colors.UI_GRAY);
		buttons.get(1).render(x + 27, y - 40, isActive(), isActive() ? getAlpha() : getAlpha() * 0.6f, graphics);

		// AI Suppression
		buttons.get(2).text = someoneHasAISuppressed ? () -> {return "Enable AI";} : () -> {return "Disable AI";};
		buttons.get(2).setIdleColor(selected ? someoneHasAISuppressed ? Color.ORANGE : Color.GREEN : Colors.UI_GRAY);
		buttons.get(2).setOverColor(selected ? buttonsActive ? someoneHasAISuppressed ? Color.GREEN : Color.ORANGE : someoneHasAISuppressed ? Color.ORANGE : Color.GREEN : Colors.UI_GRAY);
		buttons.get(2).setDownColor(selected ? buttonsActive ? Color.WHITE : someoneHasAISuppressed ? Color.GREEN : Color.ORANGE : Colors.UI_GRAY);
		buttons.get(2).render(x + 27, y - 60, isActive(), isActive() ? getAlpha() : getAlpha() * 0.6f, graphics);
	}



	@Override
	protected void internalLeftClick(final List<ContextMenu> copy, final Deque<Component> windowsCopy) {
		for (final Button button : buttons.values()) {
			button.click();
		}
	}


	@Override
	public void setActive(final boolean active) {
		super.setActive(true);
	}


	@Override
	protected void uponClose() {
	}


	@Override
	public boolean keyPressed(final int keyCode) {
		if (keyCode == Keys.ESCAPE) {
			return false;
		}

		if (super.keyPressed(keyCode)) {
			return true;
		}

		if (keyCode == Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).getKeyMappings().toggleWalkRun.keyCode) {
			buttons.get(0).getTask().execute();
		}

		if (keyCode == Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).getKeyMappings().disableEnableAI.keyCode) {
			buttons.get(2).getTask().execute();
		}
		return false;
	}


	@Override
	public void leftClickReleased() {
	}


	@Override
	public Object getUniqueIdentifier() {
		return getClass();
	}
}