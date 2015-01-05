package bloodandmithril.prop.furniture;

import java.util.Map;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.item.items.container.LiquidContainer;
import bloodandmithril.item.liquid.Liquid;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.TransferLiquidsWindow;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A {@link Prop} that represents a container of liquids
 *
 * @author Matt
 */
public abstract class LiquidContainerProp extends Furniture {
	private static final long serialVersionUID = 5555138707601557563L;
	private final LiquidContainer container;

	/**
	 * Constructor
	 */
	protected LiquidContainerProp(float x, float y, int width, int height, boolean grounded, boolean snapToGrid, float maxAmount, Map<Class<? extends Liquid>, Float> containedLiquids) {
		super(x, y, width, height, grounded);
		this.container = new PropLiquidContainer(maxAmount, containedLiquids);
	}


	@Override
	public ContextMenu getContextMenu() {
		ContextMenu menu = new ContextMenu(0, 0, true);
		final LiquidContainerProp prop = this;

		MenuItem openContainer = new MenuItem(
			"Transfer liquids",
			() -> {
				if (Domain.getSelectedIndividuals().size() != 1) {
					return;
				} else {
					Individual selected = Domain.getSelectedIndividuals().iterator().next();
					UserInterface.addLayeredComponentUnique(
						new TransferLiquidsWindow(selected, prop)
					);
				}
			},
			Domain.getSelectedIndividuals().size() == 1 ? Color.WHITE : Colors.UI_DARK_GRAY,
			Domain.getSelectedIndividuals().size() == 1 ? Color.GREEN : Colors.UI_DARK_GRAY,
			Domain.getSelectedIndividuals().size() == 1 ? Color.GRAY : Colors.UI_DARK_GRAY,
			new ContextMenu(0, 0, true, new MenuItem("You must select a single individual", () -> {}, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, null)),
			() -> {
				return Domain.getSelectedIndividuals().size() != 1;
			}
		);

		menu.addMenuItem(openContainer);
		return menu;
	}


	public LiquidContainer getContainer() {
		return container;
	}


	/**
	 * This should only be used for {@link LiquidContainerProp}s
	 *
	 * @author Matt
	 */
	public static class PropLiquidContainer extends LiquidContainer {
		private static final long serialVersionUID = 5157248940455259595L;

		public PropLiquidContainer(float maxAmount, Map<Class<? extends Liquid>, Float> containedLiquids) {
			super(0f, 0, maxAmount, containedLiquids, 0);
		}


		@Override
		protected LiquidContainer copyContainer() {
			throw new RuntimeException("This should not be called");
		}


		@Override
		protected String getCotainerTitle() {
			throw new RuntimeException("This should not be called");
		}

		@Override
		public LiquidContainer clone() {
			throw new RuntimeException("This should not be called");
		}


		@Override
		protected String internalGetSingular(boolean firstCap) {
			throw new RuntimeException("This should not be called");
		}


		@Override
		protected String internalGetPlural(boolean firstCap) {
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