package bloodandmithril.character.ai.routine;

import static bloodandmithril.core.BloodAndMithrilClient.getGraphics;
import static bloodandmithril.util.Fonts.defaultFont;
import static bloodandmithril.world.Epoch.getTimeString;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.Panel;
import bloodandmithril.ui.components.window.EditAIRoutineWindow;
import bloodandmithril.util.SerializableMappingFunction;
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

	private SerializableMappingFunction<Individual, AITask> aiTaskGenerator;
	private int lastExecutedDayOfMonth = 99;
	private float routineTime;
	private AITask task;

	/**
	 * Constructor
	 */
	public DailyRoutine(IndividualIdentifier hostId, float routineTime) {
		super(hostId);
		this.routineTime = routineTime;
		setDescription("Daily routine");
	}


	@Override
	public void setAiTaskGenerator(SerializableMappingFunction<Individual, AITask> aiTaskGenerator) {
		this.aiTaskGenerator = aiTaskGenerator;
	}


	@Override
	public boolean areExecutionConditionsMet() {
		Epoch currentEpoch = Domain.getWorld(getHost().getWorldId()).getEpoch();
		return currentEpoch.getTime() >= routineTime && currentEpoch.dayOfMonth != lastExecutedDayOfMonth;
	}


	@Override
	public void prepare() {
		this.task = aiTaskGenerator.apply(getHost());
	}


	@Override
	public boolean isComplete() {
		if (task != null) {
			return task.isComplete();
		}

		return false;
	}


	@Override
	public boolean uponCompletion() {
		if (task != null) {
			AITask toNullify = task;
			this.task = null;
			this.lastExecutedDayOfMonth = Domain.getWorld(getHost().getWorldId()).getEpoch().dayOfMonth;
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

		wizard.add(new SetTimePanel(parent));

		return wizard;
	}


	/**
	 * Used to set the time the {@link DailyRoutine} takes place
	 *
	 * @author Matt
	 */
	@Copyright("Matthew Peck 2015")
	public class SetTimePanel extends Panel {
		protected SetTimePanel(Component parent) {
			super(parent);
		}

		@Override
		public boolean leftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
			return false;
		}

		@Override
		public void leftClickReleased() {
		}

		@Override
		public void render() {
			defaultFont.setColor(parent.isActive() ? Colors.modulateAlpha(Color.ORANGE, parent.getAlpha()) : Colors.modulateAlpha(Color.ORANGE, 0.6f * parent.getAlpha()));

			defaultFont.drawWrapped(
				getGraphics().getSpriteBatch(),
				"Routine occurs daily at " + getTimeString(routineTime),
				x + 10,
				y - 27,
				width - 5
			);

			if (aiTaskGenerator != null) {
				defaultFont.drawWrapped(
					getGraphics().getSpriteBatch(),
					"Task:",
					x + 10,
					y - 107,
					width - 5
				);
			}

			if (aiTaskGenerator != null) {
				defaultFont.drawWrapped(
					getGraphics().getSpriteBatch(),
					aiTaskGenerator.apply(getHost()).getShortDescription(),
					x + 10,
					y - 127,
					width - 5
				);
			}
		}

		@Override
		public boolean keyPressed(int keyCode) {
			return false;
		}
	}
}