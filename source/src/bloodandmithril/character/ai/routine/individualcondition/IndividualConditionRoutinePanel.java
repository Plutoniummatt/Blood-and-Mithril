package bloodandmithril.character.ai.routine.individualcondition;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Deque;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.google.inject.Inject;

import bloodandmithril.character.ai.Routine.RoutinePanel;
import bloodandmithril.character.ai.RoutineContextMenusProvidedBy;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.RoutineTaskContextMenuProvider;
import bloodandmithril.character.ai.RoutineTasks;
import bloodandmithril.character.ai.routine.individualcondition.IndividualConditionRoutine.IndividualAffectedByConditionTriggerFunction;
import bloodandmithril.character.ai.routine.individualcondition.IndividualConditionRoutine.IndividualHealthTriggerFunction;
import bloodandmithril.character.conditions.Condition;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.TextInputWindow;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Util.Colors;

/**
 * Used to set the time the {@link IndividualConditionRoutine} takes place
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class IndividualConditionRoutinePanel extends RoutinePanel {

	@Inject private UserInterface userInterface;

	private Button changeConditionButton;

	IndividualConditionRoutinePanel(final Component parent, final IndividualConditionRoutine routine) {
		super(parent, routine);
		this.changeConditionButton = new Button(
			"Change condition",
			Fonts.defaultFont,
			0,
			0,
			160,
			16,
			() -> {
			},
			Color.GREEN,
			Color.WHITE,
			Color.GRAY,
			UIRef.M
		);
	}

	@Override
	public final boolean leftClick(final List<ContextMenu> copy, final Deque<Component> windowsCopy) {
		if (changeTaskButton.click()) {
			final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), false);

			for (final Class<? extends RoutineTask> routineClass : RoutineTasks.getTaskClasses()) {
				menu.addMenuItem(
					new MenuItem(
						routineClass.getAnnotation(Name.class).name(),
						() -> {},
						Color.ORANGE,
						Color.GREEN,
						Color.GRAY,
						() -> {
							final Class<? extends RoutineTaskContextMenuProvider> providerClass = routineClass.getAnnotation(RoutineContextMenusProvidedBy.class).value();
							return Wiring.injector().getInstance(providerClass).getIndividualConditionRoutineContextMenu(routine.getHost(), (IndividualConditionRoutine) routine);
						}
					)
				);
			}

			parent.setActive(false);
			copy.add(menu);
		}

		if (changeConditionButton.click()) {
			final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), false);

			menu.addMenuItem(
				new ContextMenu.MenuItem(
					"Health",
					() -> {},
					Color.ORANGE,
					Color.GREEN,
					Color.GRAY,
					() -> { return new ContextMenu(0, 0, true,
						new MenuItem("Less than %", () -> {
							userInterface.addLayeredComponentUnique(
								new TextInputWindow("lessthan%", 300, 100, "Input %", 200, 100, args -> {
									try {
										final float parseFloat = Float.parseFloat((String)args[0]);
										if (parseFloat > 100 || parseFloat < 0) {
											throw new RuntimeException();
										}
										((IndividualConditionRoutine) routine).setTriggerFunction(new IndividualHealthTriggerFunction(false, parseFloat));
									} catch (final Exception e) {
										userInterface.addClientMessage("Error", "Enter valid value (between 0 and 100)");
									}
								}, "Confirm", true, "")
							);
						}, Color.ORANGE, Color.GREEN, Color.GRAY, null),
						new MenuItem("Greater than %", () -> {
							userInterface.addLayeredComponentUnique(
								new TextInputWindow("greaterthan%", 300, 100, "Input %", 200, 100, args -> {
									try {
										final float parseFloat = Float.parseFloat((String)args[0]);
										if (parseFloat > 100 || parseFloat < 0) {
											throw new RuntimeException();
										}
										((IndividualConditionRoutine) routine).setTriggerFunction(new IndividualHealthTriggerFunction(true, parseFloat));
									} catch (final Exception e) {
										userInterface.addClientMessage("Error", "Enter valid value (between 0 and 100)");
									}
								}, "Confirm", true, "")
							);
						}, Color.ORANGE, Color.GREEN, Color.GRAY, null)
					);}
				)
			);

			menu.addMenuItem(new MenuItem(
				"Affected by",
				() -> {
				},
				Color.ORANGE,
				Color.GREEN,
				Color.GRAY,
				() -> { return getConditionsSubMenu();}
			));

			parent.setActive(false);
			copy.add(menu);
		}

		return super.leftClick(copy, windowsCopy) || changeConditionButton.click();
	}

	private ContextMenu getConditionsSubMenu() {
		final ContextMenu contextMenu = new ContextMenu(
			0,
			0,
			true
		);

		for (final Class<? extends Condition> c : Condition.getAllConditions()) {
			contextMenu.addMenuItem(new MenuItem(
				c.getAnnotation(Name.class).name(),
				() -> {
					((IndividualConditionRoutine) routine).setTriggerFunction(new IndividualAffectedByConditionTriggerFunction(c));
				},
				Color.ORANGE,
				Color.GREEN,
				Color.GRAY,
				null
			));
		}
		return contextMenu;
	}

	@Override
	public final void render(final Graphics graphics) {
		super.render(graphics);

		final IndividualConditionRoutine individualConditionRoutine = (IndividualConditionRoutine) routine;

		defaultFont.setColor(parent.isActive() ? Colors.modulateAlpha(Color.ORANGE, parent.getAlpha()) : Colors.modulateAlpha(Color.ORANGE, 0.6f * parent.getAlpha()));

		defaultFont.drawWrapped(
			graphics.getSpriteBatch(),
			individualConditionRoutine.executionCondition == null ? "Not configured" : individualConditionRoutine.executionCondition.getDetailedDescription(individualConditionRoutine.getHost()),
			x + 10,
			y - 27,
			width - 5
		);

		defaultFont.drawWrapped(
			graphics.getSpriteBatch(),
			"Task:",
			x + 10,
			y - 97,
			width - 5
		);

		defaultFont.setColor(parent.isActive() ? Colors.modulateAlpha(Color.WHITE, parent.getAlpha()) : Colors.modulateAlpha(Color.WHITE, 0.6f * parent.getAlpha()));

		if (individualConditionRoutine.getTaskGenerator() != null) {
			defaultFont.drawWrapped(
				graphics.getSpriteBatch(),
				individualConditionRoutine.getTaskGenerator().getIndividualConditionRoutineDetailedDescription(),
				x + 10,
				y - 117,
				width - 5
			);
		}

		changeConditionButton.render(x + 89, y - height + 70, parent.isActive(), parent.getAlpha(), graphics);
	}
}