package bloodandmithril.ui.components.window;

import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;

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

import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.routine.DailyRoutine;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.StimulusDrivenRoutine;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.util.Fonts;
import bloodandmithril.world.Domain;

/**
 * {@link Window} for displaying and editting AI {@link Routine}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class AIRoutinesWindow extends ScrollableListingWindow<Routine, String> {
	
	private Button add;

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


	private static Map<Routine, String> buildMap(Individual individual) {
		Map<Routine, String> map = Maps.newHashMap();
		for (Routine r : individual.getAI().getAiRoutines()) {
			map.put(r, Integer.toString(r.getPriority()));
		}
		
		return map;
	}
	
	
	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		if (add.click()) {
			Individual individual = Domain.getIndividual(individualId);
			copy.add(
				new ContextMenu(
					BloodAndMithrilClient.getMouseScreenX(), 
					BloodAndMithrilClient.getMouseScreenY(), 
					true,
					new MenuItem("Daily Routine", () -> {
						DailyRoutine routine = new DailyRoutine(individual.getId(), 0f, 0f);
						routine.setEnabled(false);
						individual.getAI().addRoutine(routine);
						refresh();
					}, Color.GREEN, Color.WHITE, Color.GRAY, null),
					new MenuItem("Stimulus Driven Routine", () -> {
						StimulusDrivenRoutine routine = new StimulusDrivenRoutine(individual.getId());
						routine.setEnabled(false);
						individual.getAI().addRoutine(routine);
						refresh();
					}, Color.GREEN, Color.WHITE, Color.GRAY, null),
					new MenuItem("Entity Visible Routine", () -> {
						EntityVisibleRoutine routine = new EntityVisibleRoutine(individual.getId(), null);
						routine.setEnabled(false);
						individual.getAI().addRoutine(routine);
						refresh();
					}, Color.GREEN, Color.WHITE, Color.GRAY, null),
					new MenuItem("Condition Routine", () -> {
						IndividualConditionRoutine routine = new IndividualConditionRoutine(individual.getId());
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
	protected void internalWindowRender() {
		if (!Domain.getIndividual(individualId).isAlive()) {
			setClosing(true);
		}
		
		
		add.render(x + 62, y - height + 32, isActive() && UserInterface.contextMenus.isEmpty(), getAlpha());
		super.internalWindowRender();
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
						AIRoutinesWindow.this.refresh();
					}, "Confirm", true, tEntry.getKey().getShortDescription())
				);
			},
			Color.WHITE,
			Color.GREEN,
			Color.GRAY,
			null
		);
		
		MenuItem remove = new MenuItem(
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