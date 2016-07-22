package bloodandmithril.character.ai.routine.entityvisible;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import bloodandmithril.character.ai.ExecutedBy;
import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.perception.Observer;
import bloodandmithril.character.ai.perception.Visible;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.Individual.Behaviour;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.item.items.Item;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.components.Panel;
import bloodandmithril.ui.components.window.EditAIRoutineWindow;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.util.datastructure.WrapperForTwo;
import bloodandmithril.world.Domain;

/**
 * A {@link Routine} that depends on the outcome of a {@link Condition} to trigger the following tasks
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
@ExecutedBy(EntityVisibleExecutor.class)
public final class EntityVisibleRoutine extends Routine {
	private static final long serialVersionUID = -5762591639048417273L;

	EntityVisible identificationFunction;

	/**
	 * Constructor
	 */
	public EntityVisibleRoutine(final IndividualIdentifier hostId, final EntityVisible identificationFunction) {
		super(hostId);
		this.identificationFunction = identificationFunction;
		setDescription("Entity visible routine");
	}


	@Override
	public final boolean areExecutionConditionsMet() {
		if (identificationFunction == null) {
			return false;
		}

		final Individual individual = Domain.getIndividual(hostId.getId());
		final List<Visible> observed = ((Observer) individual).observe(individual.getWorldId(), individual.getId().getId());
		for (final Visible v : observed) {
			if (identificationFunction.apply(v)) {
				return true && aiTaskGenerator != null;
			}
		}

		return false;
	}


	/**
	 * @return the visible entity, or null if nothing is visible
	 */
	public final Visible getVisibleEntity() {
		if (identificationFunction == null) {
			return null;
		}

		final Individual individual = Domain.getIndividual(hostId.getId());
		final List<Visible> observed = ((Observer) individual).observe(individual.getWorldId(), individual.getId().getId());
		for (final Visible v : observed) {
			if (identificationFunction.apply(v)) {
				return v;
			}
		}

		return null;
	}


	@Override
	public final Object getTaskGenerationParameter() {
		return getVisibleEntity();
	}


	@Override
	public final Deque<Panel> constructEditWizard(final EditAIRoutineWindow parent) {
		final Deque<Panel> wizard = new ArrayDeque<>();
		wizard.add(new EntityVisibleInfoPanel(parent, this));
		return wizard;
	}


	public final EntityVisible getIdentificationFunction() {
		return identificationFunction;
	}


	/**
	 * Trigger function for a specific {@link Visible}
	 *
	 * @author Matt
	 */
	@Copyright("Matthew Peck 2015")
	public static final class SpecificEntityVisible<T extends Visible> extends EntityVisible {
		private static final long serialVersionUID = -5442698966769008090L;
		private T t;
		private WrapperForTwo<Class<? extends Visible>, T> wrapper;

		public SpecificEntityVisible(final T t) {
			this.t = t;
			this.wrapper = WrapperForTwo.wrap(t.getClass(), t);
		}

		@Override
		public final Boolean apply(final Visible input) {
			return t.sameAs(input);
		}

		@Override
		public final String getDetailedDescription(final Individual host) {
			return null;
		}

		@Override
		@SuppressWarnings("unchecked")
		public final WrapperForTwo<Class<? extends Visible>, T> getEntity() {
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

		TypeEntityVisible(final Class<? extends Visible> t) {
			this.t = t;
		}

		@Override
		public Boolean apply(final Visible input) {
			return t.isAssignableFrom(input.getClass());
		}
	}

	public static final class IndividualEntityVisible extends TypeEntityVisible {
		private static final long serialVersionUID = 1633442019980027732L;
		private final Behaviour behaviour;
		private final int hostId;
		private final WrapperForTwo<Class<? extends Visible>, Individual> wrapper;
		private final boolean alive;

		public IndividualEntityVisible(final int hostId, final Class<? extends Visible> t, final Behaviour b, final boolean alive) {
			super(t);
			this.hostId = hostId;
			this.behaviour = b;
			this.alive = alive;
			this.wrapper = WrapperForTwo.wrap(t, null);
		}

		public final Class<? extends Visible> getVisibleClass() {
			return t;
		}

		@Override
		public final Boolean apply(final Visible input) {
			if (input instanceof Individual) {
				return super.apply(input) && (behaviour == null || ((Individual) input).deriveBehaviourTowards(Domain.getIndividual(hostId)) == behaviour) && ((Individual) input).isAlive() == alive;
			}

			return false;
		}

		@Override
		public final String getDetailedDescription(final Individual host) {
			return "This routine occurs when " + (behaviour == null ? "" : behaviour.description.toLowerCase() + " ") + t.getAnnotation(Name.class).name() + " are visible to " + Domain.getIndividual(hostId).getId().getSimpleName();
		}

		@Override
		@SuppressWarnings("unchecked")
		public final WrapperForTwo<Class<? extends Visible>, Individual> getEntity() {
			return wrapper;
		}
	}


	public static final class VisibleItemFuture implements SerializableFunction<Integer> {
		private static final long serialVersionUID = -3026958963883212173L;
		private final EntityVisibleRoutine routine;

		public VisibleItemFuture(final EntityVisibleRoutine routine) {
			this.routine = routine;
		}

		@Override
		public final Integer call() {
			final Visible visibleEntity = routine.getVisibleEntity();
			if (visibleEntity instanceof Item) {
				return ((Item) visibleEntity).getId();
			}

			throw new RuntimeException();
		}
	}


	public static final class VisiblePropFuture implements SerializableFunction<Integer> {
		private static final long serialVersionUID = -3026958963883212173L;
		private final EntityVisibleRoutine routine;

		public VisiblePropFuture(final EntityVisibleRoutine routine) {
			this.routine = routine;
		}

		@Override
		public final Integer call() {
			final Visible visibleEntity = routine.getVisibleEntity();
			if (visibleEntity instanceof Prop) {
				return ((Prop) visibleEntity).id;
			}

			throw new RuntimeException();
		}
	}


	public static class VisibleIndividualFuture implements SerializableFunction<Integer> {
		private static final long serialVersionUID = 3527567985423803956L;
		private EntityVisibleRoutine routine;

		public VisibleIndividualFuture(final EntityVisibleRoutine routine) {
			this.routine = routine;
		}

		@Override
		public Integer call() {
			final Visible visibleEntity = routine.getVisibleEntity();
			if (visibleEntity instanceof Individual) {
				return ((Individual) visibleEntity).getId().getId();
			}

			return null;
		}
	}
}