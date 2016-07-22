package bloodandmithril.character.ai.task;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.google.inject.Inject;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.TaskGenerator;
import bloodandmithril.character.ai.routine.daily.DailyRoutine;
import bloodandmithril.character.ai.routine.entityvisible.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.individualcondition.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.stimulusdriven.StimulusDrivenRoutine;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.core.Wiring;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.window.TextInputWindow;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;

/**
 * Instructs {@link Individual} to speak
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
@Name(name = "Speak")
public class Speak extends AITask implements RoutineTask {
	private static final long serialVersionUID = -5210580892146755047L;

	@Inject private transient UserInterface userInterface;

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
	public Speak(final Individual host, final String text, final long duration) {
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
	protected void internalExecute(final float delta) {
		if (!spoken) {
			getHost().speak(text, duration);
			spoken = true;
		}
	}


	public static class SpeakTaskGenerator extends TaskGenerator {
		private static final long serialVersionUID = -8074299444146477391L;
		private String hostName;
		private String[] text;
		private long duration;
		private int hostId;

		public SpeakTaskGenerator(final Individual indi, final long duration, final String... text) {
			this.text = text;
			this.duration = duration;
			this.hostName = indi.getId().getSimpleName();
			this.hostId = indi.getId().getId();
		}

		@Override
		public AITask apply(final Object input) {
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

		@Override
		public boolean valid() {
			return true;
		}

		@Override
		public void render() {
			final UserInterface userInterface = Wiring.injector().getInstance(UserInterface.class);
			userInterface.getShapeRenderer().begin(ShapeType.Line);
			userInterface.getShapeRenderer().setColor(Color.GREEN);
			Gdx.gl20.glLineWidth(2f);
			final Individual attacker = Domain.getIndividual(hostId);
			userInterface.getShapeRenderer().rect(
				worldToScreenX(attacker.getState().position.x) - attacker.getWidth()/2,
				worldToScreenY(attacker.getState().position.y),
				attacker.getWidth(),
				attacker.getHeight()
			);
			userInterface.getShapeRenderer().end();
		}

	}


	private ContextMenu getContextMenu(final Individual host, final Routine routine) {
		return new ContextMenu(getMouseScreenX(), getMouseScreenY(), true, new ContextMenu.MenuItem(
			"Set text",
			() -> {
				userInterface.addLayeredComponent(
					new TextInputWindow(500, 100, "Input text", 300, 250, args -> {
						final String text = (String) args[0];
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
	public ContextMenu getDailyRoutineContextMenu(final Individual host, final DailyRoutine routine) {
		return getContextMenu(host, routine);
	}


	@Override
	public ContextMenu getEntityVisibleRoutineContextMenu(final Individual host, final EntityVisibleRoutine routine) {
		return getContextMenu(host, routine);
	}


	@Override
	public ContextMenu getIndividualConditionRoutineContextMenu(final Individual host, final IndividualConditionRoutine routine) {
		return getContextMenu(host, routine);
	}


	@Override
	public ContextMenu getStimulusDrivenRoutineContextMenu(final Individual host, final StimulusDrivenRoutine routine) {
		return getContextMenu(host, routine);
	}
}