package bloodandmithril.character.ai.task;

import static bloodandmithril.character.ai.task.GoToLocation.goTo;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.items.Item;
import bloodandmithril.prop.construction.craftingstation.CraftingStation;
import bloodandmithril.util.datastructure.SerializableDoubleWrapper;
import bloodandmithril.world.Domain;

/**
 * {@link AITask} that represents the crafting of a {@link Craftable}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Craft extends CompositeAITask {
	private static final long serialVersionUID = 4625886192088540454L;
	private SerializableDoubleWrapper<Item, Integer> item;
	private int quantity;
	private int craftingStationId;
	private boolean bulk;

	/**
	 * Constructor
	 */
	public Craft(Individual host, CraftingStation craftingStation, SerializableDoubleWrapper<Item, Integer> item, int quantity) {
		super(
			host.getId(),
			"Crafting",
			goTo(
				host,
				host.getState().position.cpy(),
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
		private boolean occupied, stop;

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
			return occupied || quantity == 0 || stop;
		}


		@Override
		public boolean uponCompletion() {
			return false;
		}


		@Override
		public void execute(float delta) {
			CraftingStation craftingStation = (CraftingStation) Domain.getProp(craftingStationId);
			Individual individual = Domain.getIndividual(hostId.getId());

			if (individual == null || craftingStation == null) {
				stop = true;
				return;
			}

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