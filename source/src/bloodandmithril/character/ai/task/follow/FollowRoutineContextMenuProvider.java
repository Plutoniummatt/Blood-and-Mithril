package bloodandmithril.character.ai.task.follow;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.control.InputUtilities.getMouseWorldX;
import static bloodandmithril.control.InputUtilities.getMouseWorldY;

import com.badlogic.gdx.graphics.Color;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.RoutineTaskContextMenuProvider;
import bloodandmithril.character.ai.routine.daily.DailyRoutine;
import bloodandmithril.character.ai.routine.entityvisible.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.entityvisible.EntityVisibleRoutine.EntityVisible;
import bloodandmithril.character.ai.routine.individualcondition.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.stimulusdriven.StimulusDrivenRoutine;
import bloodandmithril.character.ai.task.attack.Attack.ReturnVictimId;
import bloodandmithril.character.ai.task.follow.Follow.FollowTaskGenerator;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.control.BloodAndMithrilClientInputProcessor;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.util.CursorBoundTask;
import bloodandmithril.util.JITTask;
import bloodandmithril.world.Domain;

/**
 * {@link RoutineTaskContextMenuProvider} for {@link Follow}
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class FollowRoutineContextMenuProvider implements RoutineTaskContextMenuProvider {

	@Inject private GameClientStateTracker gameClientStateTracker;
	@Inject private UserInterface userInterface;

	@Override
	public ContextMenu getDailyRoutineContextMenu(final Individual host, final DailyRoutine routine) {
		final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);
		final ContextMenu toChooseFrom = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			chooseFolloweeMenuItem(host, routine, toChooseFrom)
		);

		return menu;
	}


	@Override
	public ContextMenu getEntityVisibleRoutineContextMenu(final Individual host, final EntityVisibleRoutine routine) {
		final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);
		final ContextMenu toChooseFrom = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			chooseFolloweeMenuItem(host, routine, toChooseFrom)
		);

		final EntityVisible identificationFunction = routine.getIdentificationFunction();
		if (Individual.class.isAssignableFrom(identificationFunction.getEntity().a)) {
			menu.addFirst(
				new MenuItem(
					"Visible individual",
					() -> {
						routine.setAiTaskGenerator(new FollowTaskGenerator(host.getId().getId(), new EntityVisibleRoutine.VisibleIndividualFuture(routine), "visible individual"));
					},
					Color.MAGENTA,
					Color.GREEN,
					Color.GRAY,
					null
				)
			);
		}

		return menu;
	}


	@Override
	public ContextMenu getIndividualConditionRoutineContextMenu(final Individual host, final IndividualConditionRoutine routine) {
		final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);
		final ContextMenu toChooseFrom = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			chooseFolloweeMenuItem(host, routine, toChooseFrom)
		);

		return menu;
	}


	@Override
	public ContextMenu getStimulusDrivenRoutineContextMenu(final Individual host, final StimulusDrivenRoutine routine) {
		final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);
		final ContextMenu toChooseFrom = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			chooseFolloweeMenuItem(host, routine, toChooseFrom)
		);

		return menu;
	}


	private MenuItem chooseFolloweeMenuItem(final Individual host, final Routine routine, final ContextMenu toChooseFrom) {
		return new MenuItem(
			"Choose individual to follow",
			() -> {
				final JITTask task = new JITTask() {
					@Override
					public void execute(final Object... args) {
						if (gameClientStateTracker.getActiveWorld() != null) {
							for (final int indiKey : gameClientStateTracker.getActiveWorld().getPositionalIndexChunkMap().getNearbyEntityIds(Individual.class, getMouseWorldX(), getMouseWorldY())) {
								final Individual indi = Domain.getIndividual(indiKey);
								if (indi.isMouseOver()) {
									toChooseFrom.addMenuItem(
										new MenuItem(
											"Follow " + indi.getId().getSimpleName(),
											() -> {
												routine.setAiTaskGenerator(new FollowTaskGenerator(host.getId().getId(), new ReturnVictimId(indi.getId().getId()), indi.getId().getSimpleName()));
											},
											Color.ORANGE,
											Color.GREEN,
											Color.GRAY,
											null
										)
									);
								}
							}
						}

						userInterface.getContextMenus().clear();
						toChooseFrom.x = getMouseScreenX();
						toChooseFrom.y = getMouseScreenY();
						userInterface.getContextMenus().add(toChooseFrom);
					}
				};

				Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).setCursorBoundTask(new CursorBoundTask(task, true) {
					@Override
					public void renderUIGuide(final Graphics graphics) {
					}

					@Override
					public String getShortDescription() {
						return "Choose individual to follow";
					}

					@Override
					public CursorBoundTask getImmediateTask() {
						return null;
					}

					@Override
					public boolean executionConditionMet() {
						if (gameClientStateTracker.getActiveWorld() != null) {
							for (final int indiKey : gameClientStateTracker.getActiveWorld().getPositionalIndexChunkMap().getNearbyEntityIds(Individual.class, getMouseWorldX(), getMouseWorldY())) {
								final Individual indi = Domain.getIndividual(indiKey);
								if (indi.isMouseOver()) {
									return true;
								}
							}
						}

						return false;
					}

					@Override
					public boolean canCancel() {
						return true;
					}

					@Override
					public void keyPressed(final int keyCode) {
					}
				});
			},
			Color.ORANGE,
			Color.GREEN,
			Color.GRAY,
			null
		);
	}
}