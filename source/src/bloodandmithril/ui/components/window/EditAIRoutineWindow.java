package bloodandmithril.ui.components.window;

import java.util.Deque;
import java.util.List;

import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.Panel;

/**
 * {@link Window} used to edit {@link Routine}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class EditAIRoutineWindow extends Window {

	private IndividualIdentifier id;
	private final Routine routine;
	private final Deque<Panel> wizard;

	/**
	 * Constructor
	 */
	public EditAIRoutineWindow(IndividualIdentifier id, Routine routine) {
		super(600, 400, "Editing routine for " + id.getSimpleName(), true, 600, 400, false, true, true);
		this.id = id;
		this.routine = routine;
		this.wizard = routine.constructEditWizard(this);
	}


	@Override
	protected void internalWindowRender() {
		if (!wizard.isEmpty()) {
			Panel panel = wizard.peek();

			panel.x = x;
			panel.y = y;
			panel.width = width;
			panel.height = height;

			panel.render();
		}
	}


	/** Render world-level UI guides */
	@Override
	public void renderWorldUIGuide() {
		if (routine != null) {
			routine.render();
		}
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		if (!wizard.isEmpty()) {
			wizard.peek().leftClick(copy, windowsCopy);
		}
	}


	@Override
	protected void uponClose() {
	}


	@Override
	public Object getUniqueIdentifier() {
		return id.getId() + getClass().getSimpleName() + routine.getPriority() + routine.getClass().getSimpleName();
	}


	@Override
	public void leftClickReleased() {
	}
}