package bloodandmithril.character.ai;

import static bloodandmithril.core.BloodAndMithrilClient.getGraphics;
import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Deque;
import java.util.List;

import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
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

import com.badlogic.gdx.graphics.Color;

/**
 * A player customisable {@link AITask}, designed for automation and eliminating the need of micro-managing laborious tasks..
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public abstract class Routine extends AITask {
	private static final long serialVersionUID = -8502601311459390398L;
	private int priority = 1;
	private String description = "";
	private float timeBetweenOcurrences;
	private Epoch lastOcurrence;
	private boolean enabled;

	protected TaskGenerator aiTaskGenerator;
	protected AITask task;

	/**
	 * Protected constructor
	 */
	public Routine(IndividualIdentifier hostId) {
		super(hostId);
		this.enabled = true;
	}


	public int getPriority() {
		return priority;
	}


	public void setPriority(int priority) {
		this.priority = priority;
	}


	@Override
	public String getShortDescription() {
		return description;
	}


	/**
	 * Sets the description
	 */
	public void setDescription(String description) {
		this.description = description;
	}


	/**
	 * @return whether or not this {@link Routine} meets execution conditions
	 */
	public abstract boolean areExecutionConditionsMet();


	/**
	 * Gets the parameter for the {@link TaskGenerator} to generate the task
	 */
	public abstract Object getTaskGenerationParameter();


	/**
	 * Generates a task for this routine
	 */
	protected void generatedTask() {
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
	public abstract void setAiTaskGenerator(TaskGenerator aiTaskGenerator);


	/**
	 * @return the UI wizard for editing this {@link Routine}
	 */
	public abstract Deque<Panel> constructEditWizard(EditAIRoutineWindow parent);


	/**
	 * @return the minimum game-time that must elapse before this {@link Routine} can occur again
	 */
	public float getTimeBetweenOcurrences() {
		return timeBetweenOcurrences;
	}


	/**
	 * @param see {@link #getTimeBetweenOcurrences()}
	 */
	public void setTimeBetweenOcurrences(float timeBetweenOcurrences) {
		this.timeBetweenOcurrences = timeBetweenOcurrences;
	}


	public Epoch getLastOcurrence() {
		return lastOcurrence;
	}


	public void setLastOcurrence(Epoch lastOcurrence) {
		this.lastOcurrence = lastOcurrence;
	}


	public boolean isEnabled() {
		return enabled;
	}


	public void setEnabled(boolean enabled) {
		this.enabled = enabled;

		if (ClientServerInterface.isClient()) {
			UserInterface.refreshRefreshableWindows(AIRoutinesWindow.class);
		} else {
			ClientServerInterface.SendNotification.notifyRefreshWindows();
		}
	}


	public abstract class RoutinePanel extends Panel {
		protected Button changeTaskButton, changeTimeBetweenOcurrences;
		protected RoutinePanel(Component parent) {
			super(parent);

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
		public boolean leftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
			if (changeTimeBetweenOcurrences.click()) {
				UserInterface.addLayeredComponent(
					new TextInputWindow(300, 100, "Change minimum time between ocurrences", 300, 100, args -> {
						float time = 0f;
						try {
							String[] split = ((String) args[0]).split(":");
							time = Float.parseFloat(split[0]) + Float.parseFloat(split[1])/60f;
						} catch (Exception e) {
							UserInterface.addClientMessage("Error", "Enter time in HH:mm format");
						}

						setTimeBetweenOcurrences(time);
					}, "Confirm", true,Epoch.getTimeString(timeBetweenOcurrences))
				);
			}

			return changeTaskButton.click() || changeTimeBetweenOcurrences.click();
		}

		@Override
		public void leftClickReleased() {
		}

		@Override
		public void render() {
			defaultFont.setColor(parent.isActive() ? Colors.modulateAlpha(Color.ORANGE, parent.getAlpha()) : Colors.modulateAlpha(Color.ORANGE, 0.6f * parent.getAlpha()));

			defaultFont.drawWrapped(
				getGraphics().getSpriteBatch(),
				"This routine can not occur more than once in " + Epoch.getTimeString(timeBetweenOcurrences),
				x + 10,
				y - height + 120,
				width - 5
			);

			changeTimeBetweenOcurrences.render(x + 199, y - height + 50, parent.isActive(), parent.getAlpha());
			changeTaskButton.render(x + 64, y - height + 30, parent.isActive(), parent.getAlpha());
		}

		@Override
		public boolean keyPressed(int keyCode) {
			return false;
		}
	}
}