package bloodandmithril.util.cursorboundtask;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.control.InputUtilities.getMouseWorldX;
import static bloodandmithril.control.InputUtilities.getMouseWorldY;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.MouseOverable;
import bloodandmithril.core.Wiring;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.util.CursorBoundTask;

/**
 * A {@link CursorBoundTask} to select multiple entities for whatever
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public abstract class ChooseMultipleEntityCursorBoundTask<F extends MouseOverable, T extends Serializable> extends CursorBoundTask {
	
	@Inject
	private UserInterface userInterface;

	protected List<T> entities = Lists.newLinkedList();

	public ChooseMultipleEntityCursorBoundTask(final boolean isWorldCoordinate, final Class<F> clazz) {
		super(null, isWorldCoordinate);

		setTask(args -> {
			final Collection<F> nearbyEntities = Wiring.injector().getInstance(GameClientStateTracker.class).getActiveWorld().getPositionalIndexMap().getNearbyEntities(clazz, getMouseWorldX(), getMouseWorldY());
			final List<T> availableEntities = Lists.newLinkedList();

			for (final F entity : Lists.newArrayList(nearbyEntities)) {
				if (canAdd(entity) && entity.isMouseOver()) {
					availableEntities.add(transform(entity));
				} else {
					nearbyEntities.remove(entity);
				}
			}

			if (availableEntities.size() > 1) {
				final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);
				for (final F entity : nearbyEntities) {
					menu.addMenuItem(
						new MenuItem(
							"Select " + entity.getMenuTitle(),
							() -> {
								entities.add(transform(entity));
							},
							Color.ORANGE,
							Color.GREEN,
							Color.GRAY,
							null
						)
					);
				}
				userInterface.contextMenus.clear();
				userInterface.contextMenus.add(menu);
			} else if (availableEntities.size() == 1) {
				entities.addAll(availableEntities);
			}
		});
	}


	@Override
	public CursorBoundTask getImmediateTask() {
		return this;
	}


	@Override
	public boolean canCancel() {
		return true;
	}


	@Override
	public void keyPressed(final int keyCode) {
	}


	/**
	 * @return whether we should add an F
	 */
	public abstract boolean canAdd(F f);


	/**
	 * Transforms an F into a T
	 */
	public abstract T transform(F f);
}