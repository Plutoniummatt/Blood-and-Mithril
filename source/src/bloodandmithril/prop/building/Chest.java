package bloodandmithril.prop.building;

import bloodandmithril.item.Container;
import bloodandmithril.world.GameWorld;

import com.badlogic.gdx.math.Vector2;

/**
 * A chest, stores things.
 *
 * This is an extension of {@link Construction}
 * This also contains a extension of {@link Container}, which is the actual inventory of the chest
 *
 * @author Matt
 */
public abstract class Chest extends Construction {

	/** The inventory {@link Container} that backs this {@link Chest} */
	public Container container;

	/**
	 * Constructor
	 */
	protected Chest(float x, float y, int width, int height, boolean grounded, float capacity) {
		super(x, y, width, height, grounded);
		this.container = new ChestContainer(capacity, id);
	}


	/**
	 * The {@link Container} that drives the inventory of this {@link Chest}
	 */
	public static class ChestContainer extends Container {
		private static final long serialVersionUID = 3061765937846818271L;

		/** Id of the prop this chest belongs to */
		public int propId;

		/**
		 * Constructor
		 */
		protected ChestContainer(float inventoryMassCapacity, int propId) {
			super(inventoryMassCapacity, false);
			this.propId = propId;
		}


		public Vector2 getPositionOfChest() {
			return GameWorld.props.get(propId).position;
		}
	}
}