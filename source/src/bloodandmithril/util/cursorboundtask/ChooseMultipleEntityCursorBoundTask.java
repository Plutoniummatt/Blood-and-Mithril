package bloodandmithril.util.cursorboundtask;

import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseWorldX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseWorldY;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.google.common.collect.Lists;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.MouseOverable;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.util.CursorBoundTask;
import bloodandmithril.world.Domain;

/**
 * A {@link CursorBoundTask} to select multiple entities for whatever
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public abstract class ChooseMultipleEntityCursorBoundTask<F extends MouseOverable, T extends Serializable> extends CursorBoundTask {

	protected List<T> entities = Lists.newLinkedList();

	public ChooseMultipleEntityCursorBoundTask(boolean isWorldCoordinate, Class<F> clazz) {
		super(null, isWorldCoordinate);

		setTask(args -> {
			Collection<F> nearbyEntities = Domain.getActiveWorld().getPositionalIndexMap().getNearbyEntities(clazz, getMouseWorldX(), getMouseWorldY());
			List<T> availableEntities = Lists.newLinkedList();

			for (F entity : Lists.newArrayList(nearbyEntities)) {
				if (canAdd(entity) && entity.isMouseOver()) {
					availableEntities.add(transform(entity));
				} else {
					nearbyEntities.remove(entity);
				}
			}

			if (availableEntities.size() > 1) {
				ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);
				for (F entity : nearbyEntities) {
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
				UserInterface.contextMenus.clear();
				UserInterface.contextMenus.add(menu);
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
	public void keyPressed(int keyCode) {
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