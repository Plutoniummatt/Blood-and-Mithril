package bloodandmithril.ui.components.window;

import java.util.Deque;
import java.util.List;

import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;

/**
 * {@link Window} used to edit {@link Routine}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class EditAIRoutineWindow extends Window {

	private IndividualIdentifier id;
	private Routine routine;

	/**
	 * Constructor
	 */
	public EditAIRoutineWindow(IndividualIdentifier id, Routine routine) {
		super(600, 400, "Editing routine for " + id.getSimpleName(), true, 600, 400, false, true, true);
		this.id = id;
		this.routine = routine;
	}


	@Override
	protected void internalWindowRender() {
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
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