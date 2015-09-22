package bloodandmithril.character.ai.routine;

import static bloodandmithril.core.BloodAndMithrilClient.getGraphics;
import static bloodandmithril.util.Fonts.defaultFont;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.perception.Observer;
import bloodandmithril.character.ai.perception.Visible;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.Panel;
import bloodandmithril.ui.components.window.EditAIRoutineWindow;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;

/**
 * A {@link Routine} that depends on the outcome of a {@link Condition} to trigger the following tasks
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class EntityVisibleRoutine<T extends Visible> extends Routine<T> {
	private static final long serialVersionUID = -5762591639048417273L;

	private SerializableMappingFunction<T, Boolean> identificationFunction;
	private SerializableMappingFunction<T, AITask> aiTaskGenerator;
	private AITask task;
	private Class<T> tClass;

	/**
	 * Constructor
	 */
	public EntityVisibleRoutine(IndividualIdentifier hostId, Class<T> tClass, SerializableMappingFunction<T, Boolean> identificationFunction) {
		super(hostId);
		this.tClass = tClass;
		this.identificationFunction = identificationFunction;
		setDescription("Entity visible routine");
	}


	@Override
	@SuppressWarnings("unchecked")
	public boolean areExecutionConditionsMet() {
		Individual individual = Domain.getIndividual(hostId.getId());
		List<Visible> observed = ((Observer) individual).observe(individual.getWorldId(), individual.getId().getId());
		for (Visible v : observed) {
			if (tClass.isAssignableFrom(v.getClass()) && identificationFunction.apply((T) v)) {
				return true;
			}
		}

		return false;
	}


	/**
	 * @return the visible entity, or null if nothing is visible
	 */
	@SuppressWarnings("unchecked")
	public T getVisibleEntity() {
		Individual individual = Domain.getIndividual(hostId.getId());
		List<Visible> observed = ((Observer) individual).observe(individual.getWorldId(), individual.getId().getId());
		for (Visible v : observed) {
			if (tClass.isAssignableFrom(v.getClass()) && identificationFunction.apply((T) v)) {
				return (T) v;
			}
		}

		return null;
	}


	@Override
	public void setAiTaskGenerator(SerializableMappingFunction<T, AITask> aiTaskGenerator) {
		this.aiTaskGenerator = aiTaskGenerator;
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
	public void prepare() {
		this.task = aiTaskGenerator.apply(getVisibleEntity());
	}


	@Override
	public Deque<Panel> constructEditWizard(EditAIRoutineWindow parent) {
		Deque<Panel> wizard = new ArrayDeque<>();

		wizard.add(new EntityVisibleInfoPanel(parent));

		return wizard;
	}


	/**
	 * Used to set the time the {@link EntityVisibleRoutine} takes place
	 *
	 * @author Matt
	 */
	@Copyright("Matthew Peck 2015")
	public class EntityVisibleInfoPanel extends Panel {
		private Button changeTaskButton;

		protected EntityVisibleInfoPanel(Component parent) {
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
				"Routine occurs upon " + tClass.getSimpleName() + " being visible to " + getHost().getId().getSimpleName(),
				x + 10,
				y - 27,
				width - 5
			);

			if (aiTaskGenerator != null) {
				defaultFont.drawWrapped(
					getGraphics().getSpriteBatch(),
					"Task:",
					x + 10,
					y - 127,
					width - 5
				);
			}

			defaultFont.setColor(parent.isActive() ? Colors.modulateAlpha(Color.WHITE, parent.getAlpha()) : Colors.modulateAlpha(Color.WHITE, 0.6f * parent.getAlpha()));
			if (aiTaskGenerator != null) {
				AITask apply = aiTaskGenerator.apply(getVisibleEntity());
				if (apply instanceof RoutineTask) {
					defaultFont.drawWrapped(
						getGraphics().getSpriteBatch(),
						((RoutineTask) apply).getDetailedDescription(),
						x + 10,
						y - 177,
						width - 5
					);
				}
			}

			changeTaskButton.render(x + 64, y - 140, parent.isActive(), parent.getAlpha());
		}

		@Override
		public boolean keyPressed(int keyCode) {
			return false;
		}
	}
}