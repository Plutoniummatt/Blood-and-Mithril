package bloodandmithril.item.items.food.plant;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.food.Food;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A Carrot
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Carrot extends Food {
	private static final long serialVersionUID = 3714624810622084079L;
	public static final String description = "The carrot is a root vegetable, usually orange in color. It has a crisp texture when fresh.";

	public static TextureRegion CARROT;

	/**
	 * Constructor
	 */
	public Carrot() {
		super(0.1f, false, ItemValues.CARROT);
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return firstCap ? "Carrot" : "carrot";
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return firstCap ? "Carrots" : "carrots";
	}


	@Override
	public boolean consume(Individual consumer) {
		SoundService.play(6, consumer.getState().position, true);
		consumer.addFloatingText("+5 Hunger", Color.ORANGE);
		consumer.increaseHunger(0.05f);
		return true;
	}


	@Override
	public String getDescription() {
		return description;
	}


	@Override
	protected boolean internalSameAs(Item other) {
		if (other instanceof Carrot) {
			return true;
		}
		return false;
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return CARROT;
	}


	@Override
	protected Item internalCopy() {
		return new Carrot();
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		return null;
	}


	/**
	 * Seed of a carrot
	 *
	 * @author Matt
	 */
	public static class CarrotSeed extends Seed {
		private static final long serialVersionUID = 818272685820694513L;

		/**
		 * Constructor
		 */
		public CarrotSeed() {
			super(0.01f, ItemValues.CARROT_SEED);
		}


		@Override
		protected String internalGetSingular(boolean firstCap) {
			return firstCap ? "Carrot seed" : "carrot seed";
		}


		@Override
		protected String internalGetPlural(boolean firstCap) {
			return firstCap ? "Carrot seeds" : "carrot seeds";
		}


		@Override
		public String getDescription() {
			return "Carrot seeds, plant these in soil.";
		}


		@Override
		protected TextureRegion getTextureRegion() {
			return null;
		}


		@Override
		public TextureRegion getIconTextureRegion() {
			return null;
		}


		@Override
		protected Item internalCopy() {
			return new CarrotSeed();
		}


		@Override
		public bloodandmithril.prop.plant.seed.Seed getPropSeed() {
			return new CarrotSeedProp(0, 0, this);
		}
	}


	/**
	 * Seed of a carrot as a prop
	 *
	 * @author Matt
	 */
	public static class CarrotSeedProp extends bloodandmithril.prop.plant.seed.Seed {
		private static final long serialVersionUID = 1761994206485966594L;

		/**
		 * Constructor
		 */
		public CarrotSeedProp(float x, float y, Seed seed) {
			super(x, y, seed);
		}


		@Override
		public void render() {
		}


		@Override
		public void synchronizeProp(Prop other) {
		}


		@Override
		public ContextMenu getContextMenu() {
			ContextMenu menu = new ContextMenu(0, 0, true);
			
			menu.addMenuItem(
				new ContextMenu.MenuItem(
					"Show info", 
					() -> {
						UserInterface.addMessage("Carrot seed", "The seed of a carrot, plant this to grow carrots.");
					}, 
					Color.WHITE, 
					Color.GREEN, 
					Color.WHITE, 
					null
				)
			);
			
			return menu;
		}


		@Override
		public void update(float delta) {
		}


		@Override
		public String getContextMenuItemLabel() {
			return "Carrot seed";
		}


		@Override
		public boolean destroyUponHarvest() {
			return true;
		}
	}
}