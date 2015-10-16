package bloodandmithril.character.ai.routine;

import static bloodandmithril.core.BloodAndMithrilClient.getGraphics;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;
import static bloodandmithril.util.Fonts.defaultFont;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.RoutineTasks;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.core.Wiring;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.Panel;
import bloodandmithril.ui.components.window.EditAIRoutineWindow;
import bloodandmithril.ui.components.window.TextInputWindow;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.util.Util.Colors;

/**
 * A Routine based on the condition of the host
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public final class IndividualConditionRoutine extends Routine {
	private static final long serialVersionUID = 6831994593107089893L;

	private IndividualConditionTriggerFunction executionCondition;

	/**
	 * Constructor
	 */
	public IndividualConditionRoutine(IndividualIdentifier hostId) {
		super(hostId);
		setDescription("Condition routine");
	}


	public final void setTriggerFunction(IndividualConditionTriggerFunction executionCondition) {
		this.executionCondition = executionCondition;
	}


	@Override
	public final Object getTaskGenerationParameter() {
		return getHost();
	}


	@Override
	public final boolean areExecutionConditionsMet() {
		return executionCondition.apply(getHost());
	}


	@Override
	public final boolean isComplete() {
		if (task != null) {
			return task.isComplete();
		}

		return false;
	}


	@Override
	public final boolean uponCompletion() {
		if (task != null) {
			AITask toNullify = task;
			this.task = null;
			return toNullify.uponCompletion();
		}

		return false;
	}


	@Override
	public final void execute(float delta) {
		if (task != null) {
			task.execute(delta);
		}
	}


	@Override
	public final Deque<Panel> constructEditWizard(EditAIRoutineWindow parent) {
		Deque<Panel> wizard = new ArrayDeque<>();

		wizard.add(new IndividualConditionRoutinePanel(parent));

		return wizard;
	}


	public static abstract class IndividualConditionTriggerFunction extends SerializableMappingFunction<Individual, Boolean> {
		private static final long serialVersionUID = -7651195239417056155L;
		public abstract String getDetailedDescription(Individual host);
	}


	public static final class IndividualHealthTriggerFunction extends IndividualConditionTriggerFunction {
		private static final long serialVersionUID = -676643881949925314L;
		private boolean greaterThan;
		private float percentage;

		public IndividualHealthTriggerFunction(boolean greaterThan, float percentage) {
			this.greaterThan = greaterThan;
			if (percentage < 0 || percentage > 100) {
				throw new RuntimeException();
			}
			this.percentage = percentage / 100f;
		}

		@Override
		public final Boolean apply(Individual input) {
			if (greaterThan) {
				return input.getState().health/input.getState().maxHealth > percentage;
			} else {
				return input.getState().health/input.getState().maxHealth < percentage;
			}
		}
		@Override
		public final String getDetailedDescription(Individual host) {
			if (greaterThan) {
				return "This routine occurs when health is above " + String.format("%.2f", percentage*100) + "%";
			} else {
				return "This routine occurs when health is below " + String.format("%.2f", percentage*100) + "%";
			}
		}
	}


	/**
	 * Used to set the time the {@link IndividualConditionRoutine} takes place
	 *
	 * @author Matt
	 */
	@Copyright("Matthew Peck 2015")
	public final class IndividualConditionRoutinePanel extends RoutinePanel {
		private Button changeConditionButton;
		protected IndividualConditionRoutinePanel(Component parent) {
			super(parent);
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
		public final boolean leftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
			if (changeTaskButton.click()) {
				ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), false);

				for (Class<? extends RoutineTask> routineClass : RoutineTasks.getTaskClasses()) {
					menu.addMenuItem(
						new MenuItem(
							routineClass.getAnnotation(Name.class).name(),
							() -> {},
							Color.ORANGE,
							Color.GREEN,
							Color.GRAY,
							Wiring.injector().getInstance(routineClass).getIndividualConditionRoutineContextMenu(getHost(), IndividualConditionRoutine.this)
						)
					);
				}

				parent.setActive(false);
				copy.add(menu);
			}

			if (changeConditionButton.click()) {
				ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), false);

				menu.addMenuItem(
					new ContextMenu.MenuItem(
						"Health",
						() -> {},
						Color.ORANGE,
						Color.GREEN,
						Color.GRAY,
						new ContextMenu(0, 0, true,
							new MenuItem("Less than %", () -> {
								UserInterface.addLayeredComponent(
									new TextInputWindow(300, 100, "Input %", 200, 100, args -> {
										try {
											float parseFloat = Float.parseFloat((String)args[0]);
											if (parseFloat > 100 || parseFloat < 0) {
												throw new RuntimeException();
											}
											IndividualConditionRoutine.this.setTriggerFunction(new IndividualHealthTriggerFunction(false, parseFloat));
										} catch (Exception e) {
											UserInterface.addClientMessage("Error", "Enter valid value (between 0 and 100)");
										}
									}, "Confirm", true, "")
								);
							}, Color.ORANGE, Color.GREEN, Color.GRAY, null),
							new MenuItem("Greater than %", () -> {
								UserInterface.addLayeredComponent(
									new TextInputWindow(300, 100, "Input %", 200, 100, args -> {
										try {
											float parseFloat = Float.parseFloat((String)args[0]);
											if (parseFloat > 100 || parseFloat < 0) {
												throw new RuntimeException();
											}
											IndividualConditionRoutine.this.setTriggerFunction(new IndividualHealthTriggerFunction(true, parseFloat));
										} catch (Exception e) {
											UserInterface.addClientMessage("Error", "Enter valid value (between 0 and 100)");
										}
									}, "Confirm", true, "")
								);
							}, Color.ORANGE, Color.GREEN, Color.GRAY, null)
						)
					)
				);

				parent.setActive(false);
				copy.add(menu);
			}

			return super.leftClick(copy, windowsCopy) || changeConditionButton.click();
		}

		@Override
		public final void render() {
			super.render();
			defaultFont.setColor(parent.isActive() ? Colors.modulateAlpha(Color.ORANGE, parent.getAlpha()) : Colors.modulateAlpha(Color.ORANGE, 0.6f * parent.getAlpha()));

			defaultFont.drawWrapped(
				getGraphics().getSpriteBatch(),
				executionCondition.getDetailedDescription(getHost()),
				x + 10,
				y - 27,
				width - 5
			);

			defaultFont.drawWrapped(
				getGraphics().getSpriteBatch(),
				"Task:",
				x + 10,
				y - 97,
				width - 5
			);

			defaultFont.setColor(parent.isActive() ? Colors.modulateAlpha(Color.WHITE, parent.getAlpha()) : Colors.modulateAlpha(Color.WHITE, 0.6f * parent.getAlpha()));

			if (aiTaskGenerator != null) {
				defaultFont.drawWrapped(
					getGraphics().getSpriteBatch(),
					aiTaskGenerator.getIndividualConditionRoutineDetailedDescription(),
					x + 10,
					y - 117,
					width - 5
				);
			}

			changeConditionButton.render(x + 89, y - height + 70, parent.isActive(), parent.getAlpha());
		}
	}
}