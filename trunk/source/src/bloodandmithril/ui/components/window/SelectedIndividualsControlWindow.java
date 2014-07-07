package bloodandmithril.ui.components.window;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.ui.KeyMappings;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;

/**
 * {@link Window} with controls relevant to the selected entity
 *
 * @author Matt
 */
public class SelectedIndividualsControlWindow extends Window {

	HashMap<Integer, Button> buttons = newHashMap();

	/**
	 * Constructor
	 */
	public SelectedIndividualsControlWindow(int x, int y, int length, int height, String title, boolean active) {
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
				boolean run = Domain.getSelectedIndividuals().stream().mapToInt(indi -> {
					return indi.isWalking() ? 0 : 1;
				}).sum() > 0;

				for (Individual individual : Domain.getSelectedIndividuals()) {
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
	}


	@Override
	protected void internalWindowRender() {
		boolean someoneRunning = Domain.getSelectedIndividuals().stream().mapToInt(individual -> {
			return individual.isWalking() ? 0 : 1;
		}).sum() > 0;

		boolean buttonsActive = UserInterface.layeredComponents.isEmpty() ? false : UserInterface.layeredComponents.getLast() == this;
		boolean selected = Domain.getSelectedIndividuals().size() > 0;

		// Run button
		buttons.get(0).text = someoneRunning ? "Walk" : "Run";
		buttons.get(0).setIdleColor(selected ? someoneRunning ? Color.GREEN : Color.ORANGE : Colors.UI_GRAY);
		buttons.get(0).setOverColor(selected ? buttonsActive ? someoneRunning ? Color.ORANGE : Color.GREEN : someoneRunning ? Color.GREEN : Color.ORANGE : Colors.UI_GRAY);
		buttons.get(0).setDownColor(selected ? buttonsActive ? Color.WHITE : someoneRunning ? Color.GREEN : Color.ORANGE : Colors.UI_GRAY);
		buttons.get(0).render(x + 27, y - 20, isActive(), isActive() ? getAlpha() : getAlpha() * 0.6f);
	}



	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		for (Button button : buttons.values()) {
			button.click();
		}
	}


	@Override
	public void setActive(boolean active) {
		super.setActive(true);
	}


	@Override
	protected void uponClose() {
	}


	@Override
	public boolean keyPressed(int keyCode) {
		if (keyCode == Keys.ESCAPE) {
			return false;
		}

		if (super.keyPressed(keyCode)) {
			return true;
		}

		if (keyCode == KeyMappings.toggleWalkRun) {
			buttons.get(0).getTask().execute();
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