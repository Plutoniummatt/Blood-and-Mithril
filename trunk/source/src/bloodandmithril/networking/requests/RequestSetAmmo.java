package bloodandmithril.networking.requests;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.RangedWeapon;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.networking.requests.RefreshWindows.RefreshWindowsResponse;
import bloodandmithril.networking.requests.SynchronizeIndividual.SynchronizeIndividualResponse;
import bloodandmithril.world.Domain;

/**
 * A {@link Request} to change the ammo on a {@link RangedWeapon}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class RequestSetAmmo implements Request {
	
	private final int individualId;
	private final RangedWeapon weapon;
	private final Item ammo;

	/**
	 * Constructor
	 */
	public RequestSetAmmo(int individualId, RangedWeapon weapon, Item ammo) {
		this.individualId = individualId;
		this.weapon = weapon;
		this.ammo = ammo;
	}
	

	@Override
	public Responses respond() {
		Individual individual = Domain.getIndividual(individualId);
		if (individual != null) {
			for (Item equipped : individual.getEquipped().keySet()) {
				if (equipped.sameAs((Item) weapon) && individual.has(ammo) > 0) {
					((RangedWeapon) equipped).setAmmo(ammo);
				}
			}
		}
		
		
		SynchronizeIndividualResponse syncIndiResponse = new SynchronizeIndividualResponse(individualId, System.currentTimeMillis());
		RefreshWindowsResponse refreshWindows = new RefreshWindowsResponse();
		
		Responses responses = new Responses(true);
		responses.add(syncIndiResponse);
		responses.add(refreshWindows);
		
		return responses;
	}

	
	@Override
	public boolean tcp() {
		return true;
	}

	
	@Override
	public boolean notifyOthers() {
		return false;
	}
}