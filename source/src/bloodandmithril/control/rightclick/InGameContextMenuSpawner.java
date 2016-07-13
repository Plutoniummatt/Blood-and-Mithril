package bloodandmithril.control.rightclick;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.control.InputUtilities.getMouseWorldX;
import static bloodandmithril.control.InputUtilities.getMouseWorldY;
import static bloodandmithril.control.InputUtilities.isKeyPressed;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.google.inject.Inject;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualContextMenuService;
import bloodandmithril.control.RightClickHandler;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.item.items.Item;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.world.Domain;

public class InGameContextMenuSpawner implements RightClickHandler {

	@Inject
	private GameClientStateTracker gameClientStateTracker;
	@Inject
	private IndividualContextMenuService individualContextMenuService;

	@Override
	public boolean rightClick(final boolean doubleClick) {
		if (isKeyPressed(Keys.ANY_KEY)) {
			return false;
		}

		UserInterface.contextMenus.clear();
		final ContextMenu newMenu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		for (final int indiKey : gameClientStateTracker.getActiveWorld().getPositionalIndexMap().getNearbyEntityIds(Individual.class, getMouseWorldX(), getMouseWorldY())) {
			final Individual indi = Domain.getIndividual(indiKey);
			if (indi.isMouseOver()) {
				final ContextMenu secondaryMenu = individualContextMenuService.getContextMenu(indi);
				newMenu.getMenuItems().add(
					new MenuItem(
						indi.getId().getSimpleName() + " (" + indi.getClass().getSimpleName() + ")",
						() -> {
							secondaryMenu.x = getMouseScreenX();
							secondaryMenu.y = getMouseScreenY();
						},
						Color.WHITE,
						indi.getToolTipTextColor(),
						indi.getToolTipTextColor(),
						() -> { return secondaryMenu; }
					)
				);
			}
		}

		for (final int propKey : gameClientStateTracker.getActiveWorld().getPositionalIndexMap().getNearbyEntityIds(Prop.class, getMouseWorldX(), getMouseWorldY())) {
			final Prop prop = gameClientStateTracker.getActiveWorld().props().getProp(propKey);
			if (prop.isMouseOver()) {
				final ContextMenu secondaryMenu = prop.getContextMenu();
				newMenu.getMenuItems().add(
					new MenuItem(
						prop.getContextMenuItemLabel(),
						() -> {
							secondaryMenu.x = getMouseScreenX();
							secondaryMenu.y = getMouseScreenY();
						},
						prop.getContextMenuColor(),
						Color.GREEN,
						Color.GRAY,
						() -> { return secondaryMenu; }
					)
				);
			}
		}

		for (final Integer itemId : gameClientStateTracker.getActiveWorld().getPositionalIndexMap().getNearbyEntityIds(Item.class, getMouseWorldX(), getMouseWorldY())) {
			final Item item = gameClientStateTracker.getActiveWorld().items().getItem(itemId);
			if (item.isMouseOver()) {
				final ContextMenu secondaryMenu = item.getContextMenu();
				newMenu.getMenuItems().add(
					new MenuItem(
						item.getSingular(true),
						() -> {
							secondaryMenu.x = getMouseScreenX();
							secondaryMenu.y = getMouseScreenY();
						},
						Color.ORANGE,
						Color.GREEN,
						Color.GRAY,
						() -> { return secondaryMenu; }
					)
				);
			}
		}

		if (!newMenu.getMenuItems().isEmpty()) {
			UserInterface.contextMenus.add(newMenu);
			return true;
		}

		return false;
	}
}