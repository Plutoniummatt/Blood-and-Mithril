package bloodandmithril.networking.requests;

import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.world.Domain;

/**
 * {@link Request} for an {@link Individual} to throw an {@link Item}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class RequestThrowItem implements Request {

	@Inject private UserInterface userInterface;

	private int individualId;
	private Item toThrow;
	private Vector2 mouseCoords;

	/**
	 * Constructor
	 */
	public RequestThrowItem(final Individual individual, final Item toThrow, final Vector2 mouseCoords) {
		this.mouseCoords = mouseCoords;
		this.individualId = individual.getId().getId();
		this.toThrow = toThrow;
	}


	@Override
	public Responses respond() {
		final Individual individual = Domain.getIndividual(individualId);

		if (toThrow.isEquippable()) {
			individual.unequip((Equipable) toThrow);
		}

		if (individual.takeItem(toThrow) == 1) {
			userInterface.refreshRefreshableWindows();
			Domain.getWorld(individual.getWorldId()).items().addItem(
				toThrow.copy(),
				individual.getEmissionPosition(),
				mouseCoords.sub(individual.getEmissionPosition()).clamp(0f, 300f).scl(Math.min(3f, 3f / toThrow.getMass()))
			);
		}

		final Responses responses = new Responses(true);
		responses.add(new SynchronizeIndividual.SynchronizeIndividualResponse(individualId, System.currentTimeMillis()));
		responses.add(new RefreshWindowsResponse());
		return responses;
	}


	@Override
	public boolean tcp() {
		return true;
	}


	@Override
	public boolean notifyOthers() {
		return true;
	}
}