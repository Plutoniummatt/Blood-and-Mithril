package bloodandmithril.prop.building;

import bloodandmithril.item.Container;
import bloodandmithril.item.Item;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.math.Vector2;

/**
 * A chest, stores things.
 *
 * This is an extension of {@link Construction}
 * This also contains a extension of {@link Container}, which is the actual inventory of the chest
 *
 * @author Matt
 */
public abstract class ConstructionWithContainer extends Construction {

	/** The inventory {@link Container} that backs this {@link ConstructionWithContainer} */
	public Container container;

	/**
	 * Constructor
	 */
	protected ConstructionWithContainer(float x, float y, int width, int height, boolean grounded, float capacity) {
		super(x, y, width, height, grounded);
		this.container = new ConstructionContainer(capacity, id);
	}
	
	
	/**
	 * Called after super.giveItem on the container.
	 */
	protected abstract void giveItemDecorator(Item item);


	/**
	 * The {@link Container} that drives the inventory of this {@link ConstructionWithContainer}
	 */
	public class ConstructionContainer extends Container {
		private static final long serialVersionUID = 3061765937846818271L;

		/** Id of the prop this chest belongs to */
		public int propId;

		/**
		 * Constructor
		 */
		protected ConstructionContainer(float inventoryMassCapacity, int propId) {
			super(inventoryMassCapacity, false);
			this.propId = propId;
		}


		public Vector2 getPositionOfConstruction() {
			return Domain.getProps().get(propId).position;
		}
		
		
		@Override
		public synchronized void giveItem(Item item) {
			super.giveItem(item);
			giveItemDecorator(item);
		}
	}
}