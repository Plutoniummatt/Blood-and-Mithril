package bloodandmithril.ui.components.window;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;

import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.graphics.Color;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.routine.daily.DailyRoutine;
import bloodandmithril.character.ai.routine.entityvisible.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.individualcondition.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.stimulusdriven.StimulusDrivenRoutine;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;

/**
 * {@link Window} for displaying and editting AI {@link Routine}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class AIRoutinesWindow extends ScrollableListingWindow<Routine, String> {

	@Inject
	private UserInterface userInterface;

	private Button add;

	private static Function<Routine, String> fn = new Function<Routine, String>() {
		@Override
		public String apply(final Routine r) {
			return r.getShortDescription();
		}
	};

	private static Comparator<Routine> routineComparator = new Comparator<Routine>() {
		@Override
		public int compare(final Routine o1, final Routine o2) {
			return new Integer(o2.getPriority()).compareTo(o1.getPriority());
		}
	};

	private int individualId;

	/**
	 * Construction
	 */
	public AIRoutinesWindow(
		final Individual individual
	) {
		super(400, 500, "AI Routines for " + individual.getId().getSimpleName(), true, 400, 500, true, true,
			buildMap(individual),
			fn,
			routineComparator
		);
		this.individualId = individual.getId().getId();

		refresh();

		add = new Button(
			"Add routine",
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


	private static Map<Routine, String> buildMap(final Individual individual) {
		final Map<Routine, String> map = Maps.newHashMap();
		for (final Routine r : individual.getAI().getAiRoutines()) {
			map.put(r, Integer.toString(r.getPriority()));
		}

		return map;
	}


	@Override
	protected void internalLeftClick(final List<ContextMenu> copy, final Deque<Component> windowsCopy) {
		if (add.click()) {
			final Individual individual = Domain.getIndividual(individualId);
			copy.add(
				new ContextMenu(
					getMouseScreenX(),
					getMouseScreenY(),
					true,
					new MenuItem("Daily Routine", () -> {
						final DailyRoutine routine = new DailyRoutine(individual.getId(), null, 0f);
						routine.setEnabled(false);
						individual.getAI().addRoutine(routine);
						refresh();
					}, Color.GREEN, Color.WHITE, Color.GRAY, null),
					new MenuItem("Stimulus Driven Routine", () -> {
						final StimulusDrivenRoutine routine = new StimulusDrivenRoutine(individual.getId());
						routine.setEnabled(false);
						individual.getAI().addRoutine(routine);
						refresh();
					}, Color.GREEN, Color.WHITE, Color.GRAY, null),
					new MenuItem("Entity Visible Routine", () -> {
						final EntityVisibleRoutine routine = new EntityVisibleRoutine(individual.getId(), null);
						routine.setEnabled(false);
						individual.getAI().addRoutine(routine);
						refresh();
					}, Color.GREEN, Color.WHITE, Color.GRAY, null),
					new MenuItem("Condition Routine", () -> {
						final IndividualConditionRoutine routine = new IndividualConditionRoutine(individual.getId());
						routine.setEnabled(false);
						individual.getAI().addRoutine(routine);
						refresh();
					}, Color.GREEN, Color.WHITE, Color.GRAY, null)
				)
			);

		}
		super.internalLeftClick(copy, windowsCopy);
	}


	@Override
	protected void internalWindowRender(final Graphics graphics) {
		if (!Domain.getIndividual(individualId).isAlive()) {
			setClosing(true);
		}


		add.render(x + 62, y - height + 32, isActive() && userInterface.getContextMenus().isEmpty(), getAlpha(), graphics);
		super.internalWindowRender(graphics);
	};


	@Override
	public void refresh() {
		buildListing(
			buildMap(Domain.getIndividual(individualId)),
			routineComparator
		);

		getListing().getListing().stream().forEach(item -> {
			item.entrySet().forEach(entry -> {
				if (!entry.getKey().t.isEnabled()) {
					entry.getKey().button.setIdleColor(Color.GRAY);
					entry.getKey().button.setOverColor(Colors.UI_DARK_GREEN);
					entry.getKey().button.setDownColor(Color.GRAY);
				}
			});
		});
	}


	@Override
	public Object getUniqueIdentifier() {
		return getClass().getSimpleName() + individualId;
	}


	@Override
	protected ContextMenu buttonContextMenu(final Entry<Routine, String> tEntry) {
		final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		final bloodandmithril.util.Function<LinkedList<Routine>> routinesFunction = () -> {
			return Domain.getIndividual(individualId).getAI().getAiRoutines();
		};

		final MenuItem moveUp = new MenuItem(
			"Move up",
			() -> {
				final LinkedList<Routine> routines = routinesFunction.call();

				if (tEntry.getKey().getPriority() + 1 == routines.size()) {
					return;
				}

				Iterables.tryFind(routines, r -> {
					return r.getPriority() == tEntry.getKey().getPriority() + 1;
				}).get().setPriority(tEntry.getKey().getPriority());

				tEntry.getKey().setPriority(tEntry.getKey().getPriority() + 1);

				refresh();
			},
			Color.WHITE,
			Color.GREEN,
			Color.GRAY,
			null
		);

		final MenuItem moveDown = new MenuItem(
			"Move down",
			() -> {
				final LinkedList<Routine> routines = routinesFunction.call();

				if (tEntry.getKey().getPriority() == 0) {
					return;
				}

				Iterables.tryFind(routines, r -> {
					return r.getPriority() == tEntry.getKey().getPriority() - 1;
				}).get().setPriority(tEntry.getKey().getPriority());

				tEntry.getKey().setPriority(tEntry.getKey().getPriority() - 1);

				refresh();
			},
			Color.WHITE,
			Color.GREEN,
			Color.GRAY,
			null
		);

		final MenuItem edit = new MenuItem(
			"Edit",
			() -> {
				userInterface.addLayeredComponentUnique(
					new EditAIRoutineWindow(Domain.getIndividual(individualId).getId(), tEntry.getKey())
				);
			},
			Color.WHITE,
			Color.GREEN,
			Color.GRAY,
			null
		);

		final MenuItem rename = new MenuItem(
			"Rename",
			() -> {
				userInterface.addLayeredComponentUnique(
					new TextInputWindow("renameRoutine", 500, 100, "Input name", 250, 100, args -> {
						tEntry.getKey().setDescription((String) args[0]);
						AIRoutinesWindow.this.refresh();
					}, "Confirm", true, tEntry.getKey().getShortDescription())
				);
			},
			Color.WHITE,
			Color.GREEN,
			Color.GRAY,
			null
		);

		final MenuItem remove = new MenuItem(
			"Remove",
			() -> {
				Domain.getIndividual(individualId).getAI().removeRoutine(tEntry.getKey());
				refresh();
			},
			Color.WHITE,
			Color.GREEN,
			Color.GRAY,
			null
		);

		menu.addMenuItem(moveUp);
		menu.addMenuItem(moveDown);
		menu.addMenuItem(edit);
		menu.addMenuItem(rename);
		menu.addMenuItem(remove);

		return menu;
	}
}