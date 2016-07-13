package bloodandmithril.util.cursorboundtask;

import static bloodandmithril.control.InputUtilities.isKeyPressed;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseWorldCoords;

import com.badlogic.gdx.graphics.Color;
import com.google.inject.Inject;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.control.Controls;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.CursorBoundTask;
import bloodandmithril.world.Domain;


/**
 * Throws an item
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class ThrowItemCursorBoundTask extends CursorBoundTask {

	@Inject
	private Controls controls;

	private Individual individual;
	private Item item;

	/**
	 * Constructor
	 */
	public ThrowItemCursorBoundTask(final Item item, final Individual individual) {
		super(
			args -> {
				if (ClientServerInterface.isServer()) {
					if (item.isEquippable()) {
						individual.unequip((Equipable)item);
					}

					if (individual.takeItem(item) == 1) {
						UserInterface.refreshRefreshableWindows();
						Domain.getWorld(individual.getWorldId()).items().addItem(
							item.copy(),
							individual.getEmissionPosition(),
							getMouseWorldCoords().sub(individual.getEmissionPosition()).clamp(0f, 300f).scl(Math.min(3f, 3f / item.getMass()))
						);
					}
				} else {
					ClientServerInterface.SendRequest.sendThrowItemRequest(individual, item, getMouseWorldCoords());
				}
			},
			true
		);
		this.item = item;
		this.individual = individual;
	}


	@Override
	public void renderUIGuide(final Graphics graphics) {
		UserInterface.renderArrow(individual.getEmissionPosition(), getMouseWorldCoords(), new Color(0f, 1f, 0f, 0.65f), 3f, 0f, 300f);
	}


	@Override
	public boolean executionConditionMet() {
		return true;
	}


	@Override
	public String getShortDescription() {
		return "Throw";
	}


	@Override
	public boolean canCancel() {
		return true;
	}


	@Override
	public CursorBoundTask getImmediateTask() {
		if (isKeyPressed(controls.continuousThrowing.keyCode) && individual.has(item) > 0) {
			return new ThrowItemCursorBoundTask(item, individual);
		}
		return null;
	}


	@Override
	public void keyPressed(final int keyCode) {
	}
}