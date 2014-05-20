package bloodandmithril.character.ai.task;

import bloodandmithril.character.Individual;
import bloodandmithril.character.Individual.IndividualIdentifier;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.Item;
import bloodandmithril.prop.crafting.CraftingStation;
import bloodandmithril.world.Domain;

/**
 * {@link AITask} that represents the crafting of a {@link Craftable}
 *
 * @author Matt
 */
public class Craft extends CompositeAITask {
	private static final long serialVersionUID = 4625886192088540454L;
	private Item item;
	private int quantity;
	private int craftingStationId;
	private boolean bulk;

	/**
	 * Constructor
	 */
	public Craft(Individual host, CraftingStation craftingStation, Item item, int quantity) {
		super(
			host.getId(),
			"Crafting",
			new GoToLocation(
				host,
				new WayPoint(craftingStation.position, 32),
				false,
				32f,
				true
			)
		);

		this.item = item;
		this.quantity = quantity;
		this.bulk = quantity > 1;
		this.craftingStationId = craftingStation.id;
		appendTask(new Crafting(hostId, craftingStation.id));
	}


	public int getCraftingStationId() {
		return craftingStationId;
	}


	public int getQuantity() {
		return quantity;
	}


	public class Crafting extends AITask {
		private static final long serialVersionUID = 7987930854206245256L;
		private int craftingStationId;
		private boolean occupied;

		/**
		 * Constructor
		 */
		public Crafting(IndividualIdentifier hostId, int craftingStationId) {
			super(hostId);
			this.craftingStationId = craftingStationId;
		}


		@Override
		public String getDescription() {
			return "Crafting";
		}


		@Override
		public boolean isComplete() {
			return occupied || quantity == 0;
		}


		@Override
		public void uponCompletion() {
		}


		@Override
		public void execute(float delta) {
			CraftingStation craftingStation = (CraftingStation) Domain.getProps().get(craftingStationId);
			Individual individual = Domain.getIndividuals().get(hostId.getId());

			if (!craftingStation.craft(item, individual, delta)) {
				occupied = true;
			}

			if (craftingStation.isFinished()) {
				if (bulk) {
					craftingStation.takeItem(individual);
				}
				quantity--;
			}
		}
	}
}