package bloodandmithril.character.ai.routine;

import static bloodandmithril.core.BloodAndMithrilClient.getGraphics;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;
import static bloodandmithril.util.Fonts.defaultFont;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map.Entry;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.RoutineTasks;
import bloodandmithril.character.ai.TaskGenerator;
import bloodandmithril.character.ai.perception.Observer;
import bloodandmithril.character.ai.perception.Visible;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.Individual.Behaviour;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.core.Wiring;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.Panel;
import bloodandmithril.ui.components.window.EditAIRoutineWindow;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.util.datastructure.Wrapper;
import bloodandmithril.util.datastructure.WrapperForTwo;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;

/**
 * A {@link Routine} that depends on the outcome of a {@link Condition} to trigger the following tasks
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class EntityVisibleRoutine extends Routine {
	private static final long serialVersionUID = -5762591639048417273L;

	private EntityVisible identificationFunction;
	private TaskGenerator aiTaskGenerator;
	private AITask task;

	/**
	 * Constructor
	 */
	public EntityVisibleRoutine(IndividualIdentifier hostId, EntityVisible identificationFunction) {
		super(hostId);
		this.identificationFunction = identificationFunction;
		setDescription("Entity visible routine");
	}


	@Override
	public boolean areExecutionConditionsMet() {
		Individual individual = Domain.getIndividual(hostId.getId());
		List<Visible> observed = ((Observer) individual).observe(individual.getWorldId(), individual.getId().getId());
		for (Visible v : observed) {
			if (identificationFunction.apply(v)) {
				return true;
			}
		}

		return false;
	}


	/**
	 * @return the visible entity, or null if nothing is visible
	 */
	public Visible getVisibleEntity() {
		Individual individual = Domain.getIndividual(hostId.getId());
		List<Visible> observed = ((Observer) individual).observe(individual.getWorldId(), individual.getId().getId());
		for (Visible v : observed) {
			if (identificationFunction.apply(v)) {
				return v;
			}
		}

		return null;
	}


	@Override
	public void setAiTaskGenerator(TaskGenerator aiTaskGenerator) {
		this.aiTaskGenerator = aiTaskGenerator;
	}


	@Override
	public void prepare() {
		if (aiTaskGenerator != null) {
			this.task = aiTaskGenerator.apply(getVisibleEntity());
		}
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
	public Deque<Panel> constructEditWizard(EditAIRoutineWindow parent) {
		Deque<Panel> wizard = new ArrayDeque<>();

		wizard.add(new EntityVisibleInfoPanel(parent));

		return wizard;
	}


	public EntityVisible getIdentificationFunction() {
		return identificationFunction;
	}


	/**
	 * Trigger function for a specific {@link Visible}
	 *
	 * @author Matt
	 */
	@Copyright("Matthew Peck 2015")
	public static class SpecificEntityVisible<T extends Visible> extends EntityVisible {
		private static final long serialVersionUID = -5442698966769008090L;
		private T t;
		private WrapperForTwo<Class<? extends Visible>, T> wrapper;

		public SpecificEntityVisible(T t) {
			this.t = t;
			this.wrapper = WrapperForTwo.wrap(t.getClass(), t);
		}

		@Override
		public Boolean apply(Visible input) {
			return t.sameAs(input);
		}

		@Override
		public String getDetailedDescription(Individual host) {
			return null;
		}

		@Override
		@SuppressWarnings("unchecked")
		public WrapperForTwo<Class<? extends Visible>, T> getEntity() {
			return wrapper;
		}
	}


	/**
	 * Trigger function for a type of {@link Visible}
	 *
	 * @author Matt
	 */
	@Copyright("Matthew Peck 2015")
	public static abstract class EntityVisible extends SerializableMappingFunction<Visible, Boolean> {
		private static final long serialVersionUID = -5442698966769008090L;

		public abstract String getDetailedDescription(Individual host);

		public abstract <T extends Visible> WrapperForTwo<Class<? extends T>, T> getEntity();
	}


	/**
	 * Trigger function for a type of {@link Visible}
	 *
	 * @author Matt
	 */
	@Copyright("Matthew Peck 2015")
	public static abstract class TypeEntityVisible extends EntityVisible {
		private static final long serialVersionUID = -5442698966769008090L;
		protected Class<? extends Visible> t;

		TypeEntityVisible(Class<? extends Visible> t) {
			this.t = t;
		}

		@Override
		public Boolean apply(Visible input) {
			return t.equals(input.getClass());
		}
	}


	public static class IndividualEntityVisible extends TypeEntityVisible {
		private static final long serialVersionUID = 1633442019980027732L;
		private Behaviour behaviour;
		private int hostId;
		private WrapperForTwo<Class<? extends Individual>, Individual> wrapper;
		private boolean alive;

		public IndividualEntityVisible(int hostId, Class<? extends Individual> t, Behaviour b, boolean alive) {
			super(t);
			this.hostId = hostId;
			this.behaviour = b;
			this.alive = alive;
			this.wrapper = WrapperForTwo.wrap(t, null);
		}

		public Class<? extends Visible> getVisibleClass() {
			return t;
		}

		@Override
		public Boolean apply(Visible input) {
			if (input instanceof Individual) {
				return super.apply(input) && ((Individual) input).deriveBehaviourTowards(Domain.getIndividual(hostId)) == behaviour && ((Individual) input).isAlive() == alive;
			}

			return false;
		}

		@Override
		public String getDetailedDescription(Individual host) {
			return "This routine occurs when " + behaviour.description.toLowerCase() + " " + t.getAnnotation(Name.class).name() + " are visible to " + Domain.getIndividual(hostId).getId().getSimpleName();
		}

		@Override
		@SuppressWarnings("unchecked")
		public WrapperForTwo<Class<? extends Individual>, Individual> getEntity() {
			return wrapper;
		}
	}


	/**
	 * Used to set the time the {@link EntityVisibleRoutine} takes place
	 *
	 * @author Matt
	 */
	@Copyright("Matthew Peck 2015")
	public class EntityVisibleInfoPanel extends RoutinePanel {
		private Button changeVisibleEntityButton;

		protected EntityVisibleInfoPanel(Component parent) {
			super(parent);
			this.changeVisibleEntityButton = new Button(
				"Change visible entity",
				Fonts.defaultFont,
				0,
				0,
				210,
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
							Wiring.injector().getInstance(routineClass).getEntityVisibleRoutineContextMenu(getHost(), EntityVisibleRoutine.this)
						)
					);
				}

				parent.setActive(false);
				copy.add(menu);
			}

			if (changeVisibleEntityButton.click()) {
				parent.setActive(false);
				ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), false);

				menu.addMenuItem(
					new MenuItem(
						"Choose specific entity",
						() -> {},
						Color.ORANGE,
						Color.GREEN,
						Color.GRAY,
						null
					)
				);

				Wrapper<Behaviour> args = new Wrapper<>(null);
				ContextMenu deriveIndividualTypeContextMenu = deriveIndividualTypeContextMenu(args);

				menu.addMenuItem(
					new MenuItem(
						"Choose entity type",
						() -> {},
						Color.ORANGE,
						Color.GREEN,
						Color.GRAY,
						new ContextMenu(getMouseScreenX(), getMouseScreenY(), false,
							new MenuItem("Individual", () -> {}, Color.ORANGE, Color.GREEN, Color.GRAY,
								new ContextMenu(getMouseScreenX(), getMouseScreenY(), false,
									new MenuItem("Friendly", () -> {
										args.t = Behaviour.FRIENDLY;
									}, Color.ORANGE, Color.GREEN, Color.GRAY, deriveIndividualTypeContextMenu),
									new MenuItem("Hostile", () -> {
										args.t = Behaviour.HOSTILE;
									}, Color.ORANGE, Color.GREEN, Color.GRAY, deriveIndividualTypeContextMenu),
									new MenuItem("Neutral", () -> {
										args.t = Behaviour.NEUTRAL;
									}, Color.ORANGE, Color.GREEN, Color.GRAY, deriveIndividualTypeContextMenu),
									new MenuItem("Any", () -> {}, Color.ORANGE, Color.GREEN, Color.GRAY, deriveIndividualTypeContextMenu)
								)
							),
							new MenuItem("Prop", () -> {}, Color.ORANGE, Color.GREEN, Color.GRAY,
								new ContextMenu(getMouseScreenX(), getMouseScreenY(), false
									// TODO
								)
							)
						)
					)
				);
				copy.add(menu);
			}

			return super.leftClick(copy, windowsCopy) || changeVisibleEntityButton.click();
		}

		private ContextMenu deriveIndividualTypeContextMenu(Wrapper<Behaviour> args) {
			ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), false);
			for (Entry<String, List<Class<? extends Individual>>> category : Individual.getAllIndividualClasses().entrySet()) {
				ContextMenu secondaryMenu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

				for (Class<? extends Individual> clazz : category.getValue()) {
					secondaryMenu.addMenuItem(
						new MenuItem(
							clazz.getAnnotation(Name.class).name(),
							() -> {
								EntityVisibleRoutine.this.identificationFunction = new IndividualEntityVisible(
									EntityVisibleRoutine.this.getHost().getId().getId(),
									clazz,
									args.t,
									true
								);

								EntityVisibleRoutine.this.aiTaskGenerator = null;
							},
							Color.ORANGE,
							Color.GREEN,
							Color.GRAY,
							null
						)
					);
				}

				menu.addMenuItem(
					new MenuItem(
						category.getKey(),
						() -> {},
						Color.ORANGE,
						Color.GREEN,
						Color.GRAY,
						secondaryMenu
					)
				);
			}

			return menu;
		}

		@Override
		public void render() {
			super.render();
			defaultFont.setColor(parent.isActive() ? Colors.modulateAlpha(Color.ORANGE, parent.getAlpha()) : Colors.modulateAlpha(Color.ORANGE, 0.6f * parent.getAlpha()));

			defaultFont.drawWrapped(
				getGraphics().getSpriteBatch(),
				identificationFunction.getDetailedDescription(getHost()),
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
					aiTaskGenerator.getEntityVisibleRoutineDetailedDescription(),
					x + 10,
					y - 117,
					width - 5
				);
			}


			changeVisibleEntityButton.render(x + 114, y - height + 70, parent.isActive(), parent.getAlpha());
		}
	}
}