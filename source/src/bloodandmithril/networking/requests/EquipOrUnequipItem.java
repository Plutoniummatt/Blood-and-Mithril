package bloodandmithril.networking.requests;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.world.Domain;

/**
 * Send a {@link Request} to equip or unequip an item
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class EquipOrUnequipItem implements Request {

	private final int individualId;
	private final Equipable equipable;
	private final boolean equip;

	/**
	 * Constructor
	 */
	public EquipOrUnequipItem(final boolean equip, final Equipable equipable, final int individualId) {
		this.equip = equip;
		this.equipable = equipable;
		this.individualId = individualId;
	}


	@Override
	public Responses respond() {
		final Individual individual = Domain.getIndividual(individualId);

		if (equip) {
			individual.equip(equipable);
		} else {
			individual.unequip(equipable);
		}

		final Responses responses = new Responses(true);
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