package bloodandmithril.character.ai.task;

import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.TaskGenerator;
import bloodandmithril.character.ai.routine.DailyRoutine;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.StimulusDrivenRoutine;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.window.TextInputWindow;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
import com.google.inject.Inject;

/**
 * Instructs {@link Individual} to speak
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
@Name(name = "Speak")
public class Speak extends AITask implements RoutineTask {
	private static final long serialVersionUID = -5210580892146755047L;
	private String text;
	private long duration;
	private boolean spoken = false;

	@Inject
	Speak() {
		super(null);
	}

	/**
	 * @param Constructor
	 */
	public Speak(Individual host, String text, long duration) {
		super(host.getId());
		this.text = text;
		this.duration = duration;
	}


	@Override
	public String getShortDescription() {
		return "Speaking";
	}


	@Override
	public boolean isComplete() {
		return spoken;
	}


	@Override
	public boolean uponCompletion() {
		return false;
	}


	@Override
	public void execute(float delta) {
		if (!spoken) {
			getHost().speak(text, duration);
			spoken = true;
		}
	}


	public static class SpeakTaskGenerator<T> extends TaskGenerator<T> {
		private static final long serialVersionUID = -8074299444146477391L;
		private String hostName;
		private String[] text;
		private long duration;
		private int hostId;

		public SpeakTaskGenerator(Individual indi, long duration, String... text) {
			this.text = text;
			this.duration = duration;
			this.hostName = indi.getId().getSimpleName();
			this.hostId = indi.getId().getId();
		}

		@Override
		public AITask apply(T input) {
			if (Domain.getIndividual(hostId) == null || !Domain.getIndividual(hostId).isAlive()) {
				return null;
			}

			return new Speak(Domain.getIndividual(hostId), Util.randomOneOf(text), duration);
		}

		private String desc() {
			if (text.length == 1) {
				return hostName + " says \"" + text[0] + "\"";
			} else {
				return hostName + " speaks";
			}
		}

		@Override
		public String getDailyRoutineDetailedDescription() {
			return desc();
		}

		@Override
		public String getEntityVisibleRoutineDetailedDescription() {
			return desc();
		}

		@Override
		public String getIndividualConditionRoutineDetailedDescription() {
			return desc();
		}

		@Override
		public String getStimulusDrivenRoutineDetailedDescription() {
			return desc();
		}

	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ContextMenu getContextMenu(Individual host, Routine<?> routine) {
		return new ContextMenu(getMouseScreenX(), getMouseScreenY(), true, new ContextMenu.MenuItem(
			"Set text",
			() -> {
				UserInterface.addLayeredComponent(
					new TextInputWindow(500, 100, "Input text", 300, 250, args -> {
						String text = (String) args[0];
						routine.setAiTaskGenerator(new SpeakTaskGenerator(host, Math.max(7500, text.length() * 10), text));
					}, "Set", true, "")
				);
			},
			Color.ORANGE,
			Color.GREEN,
			Color.GRAY,
			null
		));
	}


	@Override
	public ContextMenu getDailyRoutineContextMenu(Individual host, DailyRoutine routine) {
		return getContextMenu(host, routine);
	}


	@Override
	public ContextMenu getEntityVisibleRoutineContextMenu(Individual host, EntityVisibleRoutine routine) {
		return getContextMenu(host, routine);
	}


	@Override
	public ContextMenu getIndividualConditionRoutineContextMenu(Individual host, IndividualConditionRoutine routine) {
		return getContextMenu(host, routine);
	}


	@Override
	public ContextMenu getStimulusDrivenRoutineContextMenu(Individual host, StimulusDrivenRoutine routine) {
		return getContextMenu(host, routine);
	}
}