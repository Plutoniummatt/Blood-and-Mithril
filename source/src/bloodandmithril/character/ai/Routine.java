package bloodandmithril.character.ai;

import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Deque;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.google.inject.Inject;

import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.Panel;
import bloodandmithril.ui.components.window.AIRoutinesWindow;
import bloodandmithril.ui.components.window.EditAIRoutineWindow;
import bloodandmithril.ui.components.window.TextInputWindow;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Epoch;

/**
 * A player customisable {@link AITask}, designed for automation and eliminating the need of micro-managing laborious tasks..
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
@ExecutedBy()
public abstract class Routine extends AITask {
	private static final long serialVersionUID = -8502601311459390398L;
	private int priority = 1;
	private String description = "";
	private float timeBetweenOcurrences;
	private Epoch lastOcurrence;
	private boolean enabled;

	protected TaskGenerator aiTaskGenerator;
	private AITask task;

	/**
	 * Protected constructor
	 */
	public Routine(final IndividualIdentifier hostId) {
		super(hostId);
		this.enabled = true;
	}


	public int getPriority() {
		return priority;
	}


	public void setPriority(final int priority) {
		this.priority = priority;
	}


	@Override
	public String getShortDescription() {
		return description;
	}


	/**
	 * Sets the description
	 */
	public void setDescription(final String description) {
		this.description = description;
	}


	public abstract Deque<Panel> constructEditWizard(final EditAIRoutineWindow parent);


	/**
	 * @return whether or not this {@link Routine} meets execution conditions
	 */
	public abstract boolean areExecutionConditionsMet();


	/**
	 * Gets the parameter for the {@link TaskGenerator} to generate the task
	 */
	public abstract Object getTaskGenerationParameter();


	/**
	 * Renders the UI aide for this {@link Routine}
	 */
	public void render() {
		if (aiTaskGenerator != null) {
			aiTaskGenerator.render();
		}
	}


	/**
	 * Generates a task for this routine
	 */
	protected void generateTask() {
		if (aiTaskGenerator != null) {
			this.task = aiTaskGenerator.apply(getTaskGenerationParameter());
		}
	}


	/**
	 * @return whether or not this {@link Routine} is still valid
	 */
	protected boolean isValid() {
		return aiTaskGenerator.valid();
	}


	/**
	 * @param aiTaskGenerator the task generator to set
	 */
	public void setAiTaskGenerator(final TaskGenerator aiTaskGenerator) {
		this.aiTaskGenerator = aiTaskGenerator;
		setEnabled(true);
	}


	public TaskGenerator getTaskGenerator() {
		return aiTaskGenerator;
	}


	/**
	 * @return the minimum game-time that must elapse before this {@link Routine} can occur again
	 */
	public float getTimeBetweenOcurrences() {
		return timeBetweenOcurrences;
	}


	/**
	 * @param see {@link #getTimeBetweenOcurrences()}
	 */
	public void setTimeBetweenOcurrences(final float timeBetweenOcurrences) {
		this.timeBetweenOcurrences = timeBetweenOcurrences;
	}


	public Epoch getLastOcurrence() {
		return lastOcurrence;
	}


	public void setLastOcurrence(final Epoch lastOcurrence) {
		this.lastOcurrence = lastOcurrence;
	}


	public boolean isEnabled() {
		return enabled;
	}


	public AITask getTask() {
		return task;
	}


	public void setTask(final AITask aiTask) {
		this.task = aiTask;
	}


	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;

		if (ClientServerInterface.isClient()) {
			Wiring.injector().getInstance(UserInterface.class).refreshRefreshableWindows(AIRoutinesWindow.class);
		} else {
			ClientServerInterface.SendNotification.notifyRefreshWindows();
		}
	}


	public static abstract class RoutinePanel extends Panel {
		@Inject	private UserInterface userInterface;

		protected Button changeTaskButton, changeTimeBetweenOcurrences;
		protected Routine routine;

		protected RoutinePanel(final Component parent, final Routine routine) {
			super(parent);
			this.routine = routine;

			this.changeTaskButton = new Button(
				"Change task",
				Fonts.defaultFont,
				0,
				0,
				110,
				16,
				() -> {
				},
				Color.GREEN,
				Color.WHITE,
				Color.GRAY,
				UIRef.M
			);
			this.changeTimeBetweenOcurrences = new Button(
				"Change minimum time between ocurrences",
				Fonts.defaultFont,
				0,
				0,
				380,
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
		public boolean leftClick(final List<ContextMenu> copy, final Deque<Component> windowsCopy) {
			if (changeTimeBetweenOcurrences.click()) {
				userInterface.addLayeredComponentUnique(
					new TextInputWindow("changeTimeBetweenOcurrences", 300, 100, "Change minimum time between ocurrences", 300, 100, args -> {
						float time = 0f;
						try {
							final String[] split = ((String) args[0]).split(":");
							time = Float.parseFloat(split[0]) + Float.parseFloat(split[1])/60f;
						} catch (final Exception e) {
							userInterface.addClientMessage("Error", "Enter time in HH:mm format");
						}

						routine.setTimeBetweenOcurrences(time);
					}, "Confirm", true,Epoch.getTimeString(routine.timeBetweenOcurrences))
				);
			}

			return changeTaskButton.click() || changeTimeBetweenOcurrences.click();
		}

		@Override
		public void leftClickReleased() {
		}

		@Override
		public void render(final Graphics graphics) {
			defaultFont.setColor(parent.isActive() ? Colors.modulateAlpha(Color.ORANGE, parent.getAlpha()) : Colors.modulateAlpha(Color.ORANGE, 0.6f * parent.getAlpha()));

			defaultFont.drawWrapped(
				graphics.getSpriteBatch(),
				"This routine can not occur more than once in " + Epoch.getTimeString(routine.timeBetweenOcurrences),
				x + 10,
				y - height + 120,
				width - 5
			);

			changeTimeBetweenOcurrences.render(x + 199, y - height + 50, parent.isActive(), parent.getAlpha(), graphics);
			changeTaskButton.render(x + 64, y - height + 30, parent.isActive(), parent.getAlpha(), graphics);
		}

		@Override
		public boolean keyPressed(final int keyCode) {
			return false;
		}
	}
}