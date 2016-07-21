package bloodandmithril.item.items.food.plant;

import static bloodandmithril.character.ai.perception.Visible.getVisible;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.UpdatedBy;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.food.Food;
import bloodandmithril.prop.Growable;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.plant.CarrotProp;
import bloodandmithril.prop.plant.CarrotProp.SoilTilesOnly;
import bloodandmithril.prop.updateservice.SeedPropUpdateService;
import bloodandmithril.ui.FloatingTextService;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;

/**
 * A Carrot
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class CarrotItem extends Food {
	private static final long serialVersionUID = 3714624810622084079L;
	public static final String description = "The carrot is a root vegetable, usually orange in color. It has a crisp texture when fresh.";

	public static TextureRegion CARROT;

	/**
	 * Constructor
	 */
	public CarrotItem() {
		super(0.1f, 1, false, ItemValues.CARROT);
	}


	@Override
	protected String internalGetSingular(final boolean firstCap) {
		return firstCap ? "Carrot" : "carrot";
	}


	@Override
	protected String internalGetPlural(final boolean firstCap) {
		return firstCap ? "Carrots" : "carrots";
	}


	@Override
	public boolean consume(final Individual consumer) {
		SoundService.play(SoundService.crunch, consumer.getState().position, true, getVisible(consumer));
		Wiring.injector().getInstance(FloatingTextService.class).addFloatingTextToIndividual(consumer, "+5 Hunger", Color.ORANGE);
		consumer.increaseHunger(0.05f);
		return true;
	}


	@Override
	public String getDescription() {
		return description;
	}


	@Override
	protected boolean internalSameAs(final Item other) {
		if (other instanceof CarrotItem) {
			return true;
		}
		return false;
	}


	@Override
	public TextureRegion getTextureRegion() {
		return CARROT;
	}


	@Override
	protected Item internalCopy() {
		return new CarrotItem();
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
	public static class CarrotSeedItem extends SeedItem {
		private static final long serialVersionUID = 818272685820694513L;

		/**
		 * Constructor
		 */
		public CarrotSeedItem() {
			super(0.01f, ItemValues.CARROT_SEED);
		}


		@Override
		protected String internalGetSingular(final boolean firstCap) {
			return firstCap ? "Carrot seed" : "carrot seed";
		}


		@Override
		protected String internalGetPlural(final boolean firstCap) {
			return firstCap ? "Carrot seeds" : "carrot seeds";
		}


		@Override
		public String getDescription() {
			return "Carrot seeds, plant these in soil.";
		}


		@Override
		public TextureRegion getTextureRegion() {
			return null;
		}


		@Override
		public TextureRegion getIconTextureRegion() {
			return null;
		}


		@Override
		protected Item internalCopy() {
			return new CarrotSeedItem();
		}


		@Override
		public bloodandmithril.prop.plant.seed.SeedProp getPropSeed() {
			return new CarrotSeedProp(0, 0, this);
		}
	}


	/**
	 * Seed of a carrot as a prop
	 *
	 * @author Matt
	 */
	@UpdatedBy(SeedPropUpdateService.class)
	public static class CarrotSeedProp extends bloodandmithril.prop.plant.seed.SeedProp {
		private static final long serialVersionUID = 1761994206485966594L;

		/** {@link TextureRegion} of this seed */
		public static TextureRegion CARROT_SEED;

		/**
		 * Constructor
		 */
		public CarrotSeedProp(final float x, final float y, final SeedItem seed) {
			super(x, y, seed, new SoilTilesOnly());
		}


		@Override
		public void render(final Graphics graphics) {
			graphics.getSpriteBatch().draw(CARROT_SEED, position.x - width / 2, position.y);
		}


		@Override
		public void synchronizeProp(final Prop other) {
		}


		@Override
		public ContextMenu getContextMenu() {
			final ContextMenu menu = new ContextMenu(0, 0, true);

			menu.addMenuItem(
				new ContextMenu.MenuItem(
					"Show info",
					() -> {
						Wiring.injector().getInstance(UserInterface.class).addGlobalMessage("Carrot seed", "The seed of a carrot, plant this to grow carrots.");
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
		public String getContextMenuItemLabel() {
			return "Carrot seed";
		}


		@Override
		public boolean destroyUponHarvest() {
			return true;
		}


		@Override
		public Growable germinate() {
			return new CarrotProp(position.x, position.y);
		}


		@Override
		public void growth(final float delta) {
			setGerminationProgress(getGerminationProgress() + delta / 100f);
		}


		@Override
		public void preRender() {
		}


		@Override
		public boolean canBeUsedAsFireSource() {
			return false;
		}
	}
}