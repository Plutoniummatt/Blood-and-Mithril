package bloodandmithril.character.ai.routine;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.util.Fonts.defaultFont;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.audio.SoundService.SuspicionLevel;
import bloodandmithril.audio.SoundService.SuspiciousSound;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.RoutineTasks;
import bloodandmithril.character.ai.perception.SoundStimulus;
import bloodandmithril.character.ai.perception.Stimulus;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.Panel;
import bloodandmithril.ui.components.window.EditAIRoutineWindow;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.util.Util.Colors;

/**
 * A {@link Routine} that is triggered by a {@link Stimulus} such as {@link SoundStimulus}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public final class StimulusDrivenRoutine extends Routine {
	private static final long serialVersionUID = 2347934053852793343L;

	private StimulusTriggerFunction triggerFunction;
	private Stimulus triggeringStimulus;
	private boolean triggered;

	/**
	 * Constructor
	 */
	public StimulusDrivenRoutine(final IndividualIdentifier hostId) {
		super(hostId);
		setDescription("Stimulus driven routine");
	}


	public final void setTriggerFunction(final StimulusTriggerFunction triggerFunction) {
		this.triggerFunction = triggerFunction;
	}


	/**
	 * Attempt to trigger the execution of this {@link Routine}
	 */
	public final void attemptTrigger(final Stimulus stimulus) {
		if (triggerFunction.s.isAssignableFrom(stimulus.getClass())) {
			if (triggerFunction.apply(stimulus)) {
				this.triggeringStimulus = stimulus;
				this.triggered = true;
			}
		}
	}


	public final StimulusTriggerFunction getTriggerFunction() {
		return triggerFunction;
	}


	/**
	 * @return the stimulus that triggered this routine
	 */
	public SerializableFunction<Stimulus> getTriggeringStimulus() {
		return new TriggeringStimulusFuture();
	}


	public class TriggeringStimulusFuture implements SerializableFunction<Stimulus> {
		private static final long serialVersionUID = 8196898951457294073L;
		@Override
		public Stimulus call() {
			return triggeringStimulus;
		}
	}


	@Override
	public final Object getTaskGenerationParameter() {
		return triggeringStimulus;
	}


	@Override
	public final boolean areExecutionConditionsMet() {
		return triggered;
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
			final AITask toNullify = task;
			if (toNullify.uponCompletion()) {
				return true;
			} else {
				this.task = null;
				this.triggered = false;
				return false;
			}
		}

		return false;
	}


	@Override
	protected void internalExecute(final float delta) {
		if (task != null) {
			task.executeTask(delta);
			if (triggered) {
				triggered = false;
			}
		}
	}


	@Override
	public final Deque<Panel> constructEditWizard(final EditAIRoutineWindow parent) {
		final Deque<Panel> wizard = new ArrayDeque<>();

		wizard.add(new StimulusDrivenRoutinePanel(parent));

		return wizard;
	}


	public final class StimulusDrivenRoutinePanel extends RoutinePanel {
		private Button changeStimulusButton;
		protected StimulusDrivenRoutinePanel(final Component parent) {
			super(parent);
			this.changeStimulusButton = new Button(
				"Change stimulus",
				Fonts.defaultFont,
				0,
				0,
				150,
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

				for (final Class<? extends RoutineTask> routineTaskClass : RoutineTasks.getTaskClasses()) {
					menu.addMenuItem(
						new MenuItem(
							routineTaskClass.getAnnotation(Name.class).name(),
							() -> {},
							Color.ORANGE,
							Color.GREEN,
							Color.GRAY,
							() -> { return Wiring.injector().getInstance(routineTaskClass).getStimulusDrivenRoutineContextMenu(getHost(), StimulusDrivenRoutine.this);}
						)
					);
				}

				parent.setActive(false);
				copy.add(menu);
			}

			if (changeStimulusButton.click()) {
				final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

				menu.addMenuItem(
					new MenuItem(
						"Sound heard",
						() -> {
							StimulusDrivenRoutine.this.triggerFunction = new SuspiciousSoundAITriggerFunction(SuspicionLevel.INVESTIGATE);
						},
						Color.ORANGE,
						Color.GREEN,
						Color.GRAY,
						null
					)
				);

				parent.setActive(false);
				copy.add(menu);
			}

			return super.leftClick(copy, windowsCopy) || changeStimulusButton.click();
		}

		@Override
		public final void leftClickReleased() {
		}

		@Override
		public final void render(final Graphics graphics) {
			super.render(graphics);
			defaultFont.setColor(parent.isActive() ? Colors.modulateAlpha(Color.ORANGE, parent.getAlpha()) : Colors.modulateAlpha(Color.ORANGE, 0.6f * parent.getAlpha()));

			defaultFont.drawWrapped(
				graphics.getSpriteBatch(),
				triggerFunction == null ? "Not configured" : triggerFunction.getDetailedDescription(),
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

			if (aiTaskGenerator != null) {
				defaultFont.drawWrapped(
					graphics.getSpriteBatch(),
					aiTaskGenerator.getStimulusDrivenRoutineDetailedDescription(),
					x + 10,
					y - 117,
					width - 5
				);
			}

			changeStimulusButton.render(x + 84, y - height + 70, parent.isActive(), parent.getAlpha(), graphics);
		}
	}


	public static abstract class StimulusTriggerFunction extends SerializableMappingFunction<Stimulus, Boolean> {
		private static final long serialVersionUID = 6829023381484228088L;
		private Class<? extends Stimulus> s;

		protected StimulusTriggerFunction(final Class<? extends Stimulus> s) {
			this.s = s;
		}

		public Class<? extends Stimulus> getTriggeringStimulusClass() {
			return s;
		}

		public abstract String getDetailedDescription();
	}


	public static final class SuspiciousSoundAITriggerFunction extends StimulusTriggerFunction {
		private static final long serialVersionUID = 5189638348993362947L;
		private SuspicionLevel level;

		public SuspiciousSoundAITriggerFunction(final SuspicionLevel level) {
			super(SuspiciousSound.class);
			this.level = level;
		}

		@Override
		public final String getDetailedDescription() {
			return "This event occurs when a suspicious sound is heard";
		}

		@Override
		public final Boolean apply(final Stimulus input) {
			if (input instanceof SuspiciousSound) {
				return ((SuspiciousSound) input).getSuspicionLevel().severity >= level.severity;
			}

			return false;
		}
	}
}