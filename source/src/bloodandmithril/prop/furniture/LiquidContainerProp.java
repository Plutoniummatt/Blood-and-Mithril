package bloodandmithril.prop.furniture;

import java.util.Map;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.item.liquid.Liquid;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;

/**
 * A {@link Prop} that represents a container of liquids
 *
 * @author Matt
 */
public abstract class LiquidContainerProp extends Furniture {
	private static final long serialVersionUID = 5555138707601557563L;

	protected Map<Class<? extends Liquid>, Float> containedLiquids;
	protected final float maxAmount;

	/**
	 * Constructor
	 */
	protected LiquidContainerProp(float x, float y, int width, int height, boolean grounded, boolean snapToGrid, float maxAmount) {
		super(x, y, width, height, grounded, false);
		this.maxAmount = maxAmount;
	}


	@Override
	public ContextMenu getContextMenu() {
		ContextMenu menu = new ContextMenu(0, 0, true);

		MenuItem openContainer = new MenuItem(
			"Transfer liquids",
			() -> {
				if (Domain.getSelectedIndividuals().size() != 1) {
					return;
				} else {
					Individual selected = Domain.getSelectedIndividuals().iterator().next();
					if (ClientServerInterface.isServer()) {

					} else {

					}
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
}