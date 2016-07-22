package bloodandmithril.character.ai.task.craft;

import static bloodandmithril.character.ai.task.GoToLocation.goTo;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ExecutedBy;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.task.compositeaitask.CompositeAITask;
import bloodandmithril.character.ai.task.compositeaitask.CompositeAITaskExecutor;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.items.Item;
import bloodandmithril.prop.construction.craftingstation.CraftingStation;
import bloodandmithril.util.datastructure.SerializableDoubleWrapper;

/**
 * {@link AITask} that represents the crafting of a {@link Craftable}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@ExecutedBy(CompositeAITaskExecutor.class)
public class Craft extends CompositeAITask {
	private static final long serialVersionUID = 4625886192088540454L;
	private int craftingStationId;
	int quantity;
	SerializableDoubleWrapper<Item, Integer> item;
	boolean bulk;

	/**
	 * Constructor
	 */
	public Craft(final Individual host, final CraftingStation craftingStation, final SerializableDoubleWrapper<Item, Integer> item, final int quantity) {
		super(
			host.getId(),
			"Crafting",
			goTo(
				host,
				host.getState().position.cpy(),
				new WayPoint(craftingStation.position, 5),
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


	@ExecutedBy(CraftingExecutor.class)
	public class Crafting extends AITask {
		private static final long serialVersionUID = 7987930854206245256L;
		int craftingStationId;
		boolean occupied, stop;

		/**
		 * Constructor
		 */
		public Crafting(final IndividualIdentifier hostId, final int craftingStationId) {
			super(hostId);
			this.craftingStationId = craftingStationId;
		}


		public Craft getParent() {
			return Craft.this;
		}


		@Override
		public String getShortDescription() {
			return "Crafting";
		}
	}
}