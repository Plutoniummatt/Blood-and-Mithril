package bloodandmithril.character.ai.routine;

import static bloodandmithril.core.BloodAndMithrilClient.getGraphics;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;
import static bloodandmithril.util.Fonts.defaultFont;
import static bloodandmithril.world.Epoch.getTimeString;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.RoutineTasks;
import bloodandmithril.character.ai.TaskGenerator;
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
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;
import bloodandmithril.world.Epoch;

import com.badlogic.gdx.graphics.Color;

/**
 * {@link Routine} that executes at, or later than a specified time every day
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class DailyRoutine extends Routine<Individual> {
	private static final long serialVersionUID = -255141692263126217L;

	private TaskGenerator<Individual> aiTaskGenerator;
	private Epoch lastExecutedEpoch = null;
	private float routineTime;
	private float toleranceTime;
	private AITask task;

	/**
	 * Constructor
	 */
	public DailyRoutine(IndividualIdentifier hostId, float routineTime, float toleranceTime) {
		super(hostId);
		this.routineTime = routineTime;
		this.toleranceTime = toleranceTime;
		setDescription("Daily routine");
	}


	@Override
	public void setAiTaskGenerator(TaskGenerator<Individual> aiTaskGenerator) {
		this.aiTaskGenerator = aiTaskGenerator;
	}


	@Override
	public boolean areExecutionConditionsMet() {
		Epoch currentEpoch = Domain.getWorld(getHost().getWorldId()).getEpoch();
		return currentEpoch.getTime() >= routineTime && currentEpoch.getTime() <= routineTime + toleranceTime && (lastExecutedEpoch == null || currentEpoch.dayOfMonth != lastExecutedEpoch.dayOfMonth);
	}


	@Override
	public void prepare() {
		if (aiTaskGenerator != null) {
			this.task = aiTaskGenerator.apply(getHost());
		}
	}


	@Override
	public boolean isComplete() {
		if (task != null) {
			return task.isComplete() || !areExecutionConditionsMet();
		}

		return false;
	}


	@Override
	public boolean uponCompletion() {
		if (task != null) {
			AITask toNullify = task;
			this.task = null;
			this.lastExecutedEpoch = Domain.getWorld(getHost().getWorldId()).getEpoch().copy();
			return toNullify.uponCompletion();
		}

		return false;
	}


	@Override
	public void execute(float delta) {
		if (task != null) {
			task.execute(delta);
		}
	}


	@Override
	public Deque<Panel> constructEditWizard(EditAIRoutineWindow parent) {
		Deque<Panel> wizard = new ArrayDeque<>();

		wizard.add(new DailyRoutineInfoPanel(parent));

		return wizard;
	}


	/**
	 * Used to set the time the {@link DailyRoutine} takes place
	 *
	 * @author Matt
	 */
	@Copyright("Matthew Peck 2015")
	public class DailyRoutineInfoPanel extends RoutinePanel {
		private Button changeTimeButton, changeToleranceButton;
		protected DailyRoutineInfoPanel(Component parent) {
			super(parent);
			this.changeTimeButton = new Button(
				"Change time",
				Fonts.defaultFont,
				0,
				0,
				110,
				16,
				() -> {
					UserInterface.addLayeredComponent(
						new TextInputWindow(
							250,
							100,
							"Change time",
							250,
							100,
							args -> {
								try {
									String[] split = ((String) args[0]).split(":");
									routineTime = Float.parseFloat(split[0]) + Float.parseFloat(split[1])/60f;
								} catch (Exception e) {
									UserInterface.addClientMessage("Error", "Enter time in HH:mm format");
								}
							},
							"Confirm",
							true,
							Epoch.getTimeString(routineTime)
						)
					);
				},
				Color.GREEN,
				Color.WHITE,
				Color.GRAY,
				UIRef.M
			);
			this.changeToleranceButton = new Button(
				"Change duration",
				Fonts.defaultFont,
				0,
				0,
				150,
				16,
				() -> {
					UserInterface.addLayeredComponent(
						new TextInputWindow(
							250,
							100,
							"Change duration",
							250,
							100,
							args -> {
								try {
									String[] split = ((String) args[0]).split(":");
									toleranceTime = Float.parseFloat(split[0]) + Float.parseFloat(split[1])/60f;
								} catch (Exception e) {
									UserInterface.addClientMessage("Error", "Enter time in HH:mm format");
								}
							},
							"Confirm",
							true,
							Epoch.getTimeString(toleranceTime)
						)
					);
				},
				Color.GREEN,
				Color.WHITE,
				Color.GRAY,
				UIRef.M
			);
		}

		@Override
		public boolean leftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
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
							Wiring.injector().getInstance(routineClass).getDailyRoutineContextMenu(getHost(), DailyRoutine.this)
						)
					);
				}

				parent.setActive(false);
				copy.add(menu);
			}

			return super.leftClick(copy, windowsCopy) || changeTaskButton.click() || changeToleranceButton.click();
		}

		@Override
		public void render() {
			super.render();
			defaultFont.setColor(parent.isActive() ? Colors.modulateAlpha(Color.ORANGE, parent.getAlpha()) : Colors.modulateAlpha(Color.ORANGE, 0.6f * parent.getAlpha()));

			defaultFont.drawWrapped(
				getGraphics().getSpriteBatch(),
				"Routine occurs daily between " + getTimeString(routineTime) + " and " + getTimeString(routineTime + toleranceTime),
				x + 10,
				y - 27,
				width - 5
			);

			if (lastExecutedEpoch == null) {
				Epoch epoch = Domain.getWorld(getHost().getWorldId()).getEpoch();
				if (epoch.getTime() >= routineTime) {
					epoch = epoch.copy();
					epoch.incrementDay();
				}
				defaultFont.drawWrapped(
					getGraphics().getSpriteBatch(),
					"Next scheduled occurrence: " + getTimeString(routineTime) + " on " + epoch.getDateString(),
					x + 10,
					y - 47,
					width - 5
				);
			} else {
				Epoch copy = lastExecutedEpoch.copy();
				copy.incrementDay();
				defaultFont.drawWrapped(
					getGraphics().getSpriteBatch(),
					"Next scheduled occurrence: " + getTimeString(routineTime) + " on " + copy.getDateString(),
					x + 10,
					y - 47,
					width - 5
				);
			}

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
					aiTaskGenerator.getDailyRoutineDetailedDescription(),
					x + 10,
					y - 117,
					width - 5
				);
			}

			changeToleranceButton.render(x + 84, y - height + 70, parent.isActive(), parent.getAlpha());
			changeTimeButton.render(x + 64, y - height + 90, parent.isActive(), parent.getAlpha());
		}
	}
}