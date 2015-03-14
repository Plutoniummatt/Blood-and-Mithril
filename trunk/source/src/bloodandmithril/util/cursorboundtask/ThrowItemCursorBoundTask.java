package bloodandmithril.util.cursorboundtask;

import static bloodandmithril.core.BloodAndMithrilClient.getMouseWorldCoords;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.CursorBoundTask;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;


/**
 * Throws an item
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class ThrowItemCursorBoundTask extends CursorBoundTask {
	private Individual individual;
	private Item item;

	/**
	 * Constructor
	 */
	public ThrowItemCursorBoundTask(final Item item, final Individual individual) {
		super(
			args -> {
				if (ClientServerInterface.isServer()) {
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
	public void renderUIGuide() {
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
		if (Gdx.input.isKeyPressed(BloodAndMithrilClient.getKeyMappings().continuousThrowing.keyCode) && individual.has(item) > 0) {
			return new ThrowItemCursorBoundTask(item, individual);
		}
		return null;
	}
}