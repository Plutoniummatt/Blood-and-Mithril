package bloodandmithril.networking.requests;

import bloodandmithril.character.ai.task.craft.Craft;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.prop.construction.craftingstation.CraftingStation;
import bloodandmithril.util.datastructure.SerializableDoubleWrapper;
import bloodandmithril.world.Domain;

@Copyright("Matthew Peck 2014")
public class RequestStartCrafting implements Request {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5312066031108712263L;
	private final int individualId;
	private final int craftingStationId;
	private final SerializableDoubleWrapper<Item, Integer> item;
	private final int quantity;

	/**
	 * Constructor
	 */
	public RequestStartCrafting(Individual host, CraftingStation craftingStation, SerializableDoubleWrapper<Item, Integer> item, int quantity) {
		this.quantity = quantity;
		this.individualId = host.getId().getId();
		this.craftingStationId = craftingStation.id;
		this.item = item;
	}


	@Override
	public Responses respond() {
		Individual individual = Domain.getIndividual(individualId);
		CraftingStation craftingStation = (CraftingStation) Domain.getWorld(Domain.getIndividual(individualId).getWorldId()).props().getProp(craftingStationId);
		individual.getAI().setCurrentTask(new Craft(individual, craftingStation, item, quantity));

		return new Responses(false);
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