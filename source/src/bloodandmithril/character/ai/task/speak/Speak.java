package bloodandmithril.character.ai.task.speak;

import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.google.inject.Inject;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ExecutedBy;
import bloodandmithril.character.ai.RoutineContextMenusProvidedBy;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.TaskGenerator;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.core.Wiring;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;

/**
 * Instructs {@link Individual} to speak
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
@Name(name = "Speak")
@ExecutedBy(SpeakExecutor.class)
@RoutineContextMenusProvidedBy(SpeakRoutineContextMenuProvider.class)
public class Speak extends AITask implements RoutineTask {
	private static final long serialVersionUID = -5210580892146755047L;

	String text;
	long duration;
	boolean spoken = false;

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
}