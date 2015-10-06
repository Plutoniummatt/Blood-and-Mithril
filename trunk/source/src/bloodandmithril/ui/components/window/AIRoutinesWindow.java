package bloodandmithril.ui.components.window;

import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

/**
 * {@link Window} for displaying and editting AI {@link Routine}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class AIRoutinesWindow extends ScrollableListingWindow<Routine, String> {

	private static Function<Routine, String> fn = new Function<Routine, String>() {
		@Override
		public String apply(Routine r) {
			return r.getShortDescription();
		}
	};

	private static Comparator<Routine> routineComparator = new Comparator<Routine>() {
		@Override
		public int compare(Routine o1, Routine o2) {
			return new Integer(o2.getPriority()).compareTo(o1.getPriority());
		}
	};

	private int individualId;

	/**
	 * Construction
	 */
	public AIRoutinesWindow(
		Individual individual
	) {
		super(400, 500, "AI Routines for " + individual.getId().getSimpleName(), true, 400, 500, true, true,
			buildMap(individual),
			fn,
			routineComparator
		);
		this.individualId = individual.getId().getId();

		refresh();
	}


	private static Map<Routine, String> buildMap(Individual individual) {
		Map<Routine, String> map = Maps.newHashMap();
		for (Routine r : individual.getAI().getAiRoutines()) {
			map.put(r, Integer.toString(r.getPriority()));
		}
		return map;
	}


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
					entry.getKey().button.setOverColor(Color.GRAY);
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
	protected ContextMenu buttonContextMenu(Entry<Routine, String> tEntry) {
		ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		bloodandmithril.util.Function<LinkedList<Routine>> routinesFunction = () -> {
			return Domain.getIndividual(individualId).getAI().getAiRoutines();
		};

		MenuItem moveUp = new MenuItem(
			"Move up",
			() -> {
				LinkedList<Routine> routines = routinesFunction.call();

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

		MenuItem moveDown = new MenuItem(
			"Move down",
			() -> {
				LinkedList<Routine> routines = routinesFunction.call();

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

		MenuItem edit = new MenuItem(
			"Edit",
			() -> {
				UserInterface.addLayeredComponentUnique(
					new EditAIRoutineWindow(Domain.getIndividual(individualId).getId(), tEntry.getKey())
				);
			},
			Color.WHITE,
			Color.GREEN,
			Color.GRAY,
			null
		);

		MenuItem rename = new MenuItem(
			"Rename",
			() -> {
				UserInterface.addLayeredComponentUnique(
					new TextInputWindow(500, 100, "Input name", 250, 100, args -> {
						tEntry.getKey().setDescription((String) args[0]);
					}, "Confirm", true, tEntry.getKey().getShortDescription())
				);
				AIRoutinesWindow.this.refresh();
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

		return menu;
	}
}