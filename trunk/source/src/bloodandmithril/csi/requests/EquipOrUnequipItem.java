package bloodandmithril.csi.requests;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.csi.requests.RefreshWindows.RefreshWindowsResponse;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.world.Domain;

/**
 * Send a {@link Request} to equip or unequip an item
 *
 * @author Matt
 */
public class EquipOrUnequipItem implements Request {

	private final int individualId;
	private final Equipable equipable;
	private final boolean equip;

	/**
	 * Constructor
	 */
	public EquipOrUnequipItem(boolean equip, Equipable equipable, int individualId) {
		this.equip = equip;
		this.equipable = equipable;
		this.individualId = individualId;
	}


	@Override
	public Responses respond() {
		Individual individual = Domain.getIndividuals().get(individualId);

		if (equip) {
			individual.equip(equipable);
		} else {
			individual.unequip(equipable);
		}

		Responses responses = new Responses(true);
		responses.add(new SynchronizeIndividual.SynchronizeIndividualResponse(individual.getId().getId(), System.currentTimeMillis()));
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