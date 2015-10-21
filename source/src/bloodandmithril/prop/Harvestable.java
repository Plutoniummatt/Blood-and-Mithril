package bloodandmithril.prop;

import java.util.Collection;

import bloodandmithril.character.ai.perception.Visible;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine.EntityVisible;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.item.items.Item;
import bloodandmithril.util.datastructure.WrapperForTwo;

/**
 * Interface for harvesting
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@Name(name = "Harvestable")
public interface Harvestable {

	/** Returns the item that harvesting this {@link Harvestable} provides */
	public abstract Collection<Item> harvest(boolean canReceive);

	/** True if the prop is destroyed upon being harvested */
	public abstract boolean destroyUponHarvest();

	public static class VisibleHarvestable extends EntityVisible {
		private static final long serialVersionUID = -211448711527852658L;
		private WrapperForTwo<Class<? extends Harvestable>, Harvestable> wrapper = WrapperForTwo.wrap(Harvestable.class, null);

		@Override
		public Boolean apply(Visible input) {
			if (input instanceof Harvestable) {
				return true;
			}

			return false;
		}

		@Override
		public String getDetailedDescription(Individual host) {
			return "This routine occurs when a harvestable prop is visible to " + host.getId().getSimpleName();
		}

		@Override
		@SuppressWarnings("unchecked")
		public WrapperForTwo<Class<? extends Harvestable>, Harvestable> getEntity() {
			return wrapper;
		}
	}
}