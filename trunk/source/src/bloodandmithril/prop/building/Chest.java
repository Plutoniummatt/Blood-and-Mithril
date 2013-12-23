package bloodandmithril.prop.building;

import bloodandmithril.item.Container;

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
	protected Container container;

	/**
	 * Constructor
	 */
	protected Chest(float x, float y, int width, int height, boolean grounded, float capacity) {
		super(x, y, width, height, grounded);
		setupContainer(capacity);
	}


	/**
	 * Creates and sets up the {@link #container}
	 */
	private void setupContainer(float capacity) {
		this.container = new ChestContainer(capacity);
	}


	/**
	 * The {@link Container} that drives the inventory of this {@link Chest}
	 */
	public class ChestContainer extends Container {
		private static final long serialVersionUID = 3061765937846818271L;


		/**
		 * Constructor
		 */
		protected ChestContainer(float inventoryMassCapacity) {
			super(inventoryMassCapacity, false);
		}


		public Vector2 getPositionOfChest() {
			return position;
		}
	}
}