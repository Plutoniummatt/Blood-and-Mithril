package bloodandmithril.prop.furniture;

import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.Wiring;
import bloodandmithril.item.items.container.LiquidContainerItem;
import bloodandmithril.item.liquid.Liquid;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.TransferLiquidsWindow;
import bloodandmithril.util.Util.Colors;

/**
 * A {@link Prop} that represents a container of liquids
 *
 * @author Matt
 */
public abstract class LiquidContainerProp extends Furniture {
	private static final long serialVersionUID = 5555138707601557563L;
	private final LiquidContainerItem container;

	/**
	 * Constructor
	 */
	protected LiquidContainerProp(final float x, final float y, final int width, final int height, final boolean grounded, final boolean snapToGrid, final float maxAmount, final Map<Class<? extends Liquid>, Float> containedLiquids) {
		super(x, y, width, height, grounded);
		this.container = new PropLiquidContainerItem(maxAmount, containedLiquids);
	}


	@Override
	public ContextMenu getContextMenu() {
		final ContextMenu menu = new ContextMenu(0, 0, true);
		final LiquidContainerProp prop = this;

		final GameClientStateTracker gameClientStateTracker = Wiring.injector().getInstance(GameClientStateTracker.class);

		final MenuItem openContainer = new MenuItem(
			"Transfer liquids",
			() -> {
				if (gameClientStateTracker.getSelectedIndividuals().size() != 1) {
					return;
				} else {
					final Individual selected = gameClientStateTracker.getSelectedIndividuals().iterator().next();
					Wiring.injector().getInstance(UserInterface.class).addLayeredComponentUnique(
						new TransferLiquidsWindow(selected, prop)
					);
				}
			},
			gameClientStateTracker.getSelectedIndividuals().size() == 1 ? Color.WHITE : Colors.UI_DARK_GRAY,
			gameClientStateTracker.getSelectedIndividuals().size() == 1 ? Color.GREEN : Colors.UI_DARK_GRAY,
			gameClientStateTracker.getSelectedIndividuals().size() == 1 ? Color.GRAY : Colors.UI_DARK_GRAY,
			() -> {
				return new ContextMenu(0, 0, true, new MenuItem("You must select a single individual", () -> {}, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, null));
			},
			() -> {
				return gameClientStateTracker.getSelectedIndividuals().size() != 1;
			}
		);

		menu.addMenuItem(openContainer);
		return menu;
	}


	public LiquidContainerItem getContainer() {
		return container;
	}


	/**
	 * This should only be used for {@link LiquidContainerProp}s
	 *
	 * @author Matt
	 */
	public static class PropLiquidContainerItem extends LiquidContainerItem {
		private static final long serialVersionUID = 5157248940455259595L;

		public PropLiquidContainerItem(final float maxAmount, final Map<Class<? extends Liquid>, Float> containedLiquids) {
			super(0f, 0, maxAmount, containedLiquids, 0);
		}


		@Override
		protected LiquidContainerItem copyContainer() {
			throw new RuntimeException("This should not be called");
		}


		@Override
		protected String getCotainerTitle() {
			throw new RuntimeException("This should not be called");
		}

		@Override
		public LiquidContainerItem clone() {
			throw new RuntimeException("This should not be called");
		}


		@Override
		protected String internalGetSingular(final boolean firstCap) {
			throw new RuntimeException("This should not be called");
		}


		@Override
		protected String internalGetPlural(final boolean firstCap) {
			throw new RuntimeException("This should not be called");
		}


		@Override
		public String getDescription() {
			throw new RuntimeException("This should not be called");
		}


		@Override
		public TextureRegion getTextureRegion() {
			throw new RuntimeException("This should not be called");
		}


		@Override
		public TextureRegion getIconTextureRegion() {
			throw new RuntimeException("This should not be called");
		}
	}
}