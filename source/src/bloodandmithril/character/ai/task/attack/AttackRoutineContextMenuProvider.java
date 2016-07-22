package bloodandmithril.character.ai.task.attack;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.control.InputUtilities.getMouseWorldX;
import static bloodandmithril.control.InputUtilities.getMouseWorldY;
import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;

import java.util.Collection;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.google.inject.Singleton;

import bloodandmithril.RoutineTaskContextMenuProvider;
import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.routine.daily.DailyRoutine;
import bloodandmithril.character.ai.routine.entityvisible.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.entityvisible.EntityVisibleRoutine.EntityVisible;
import bloodandmithril.character.ai.routine.individualcondition.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.stimulusdriven.StimulusDrivenRoutine;
import bloodandmithril.character.ai.task.attack.Attack.AttackTaskGenerator;
import bloodandmithril.character.ai.task.attack.Attack.AttackVisibleIndividualTaskGenerator;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.control.BloodAndMithrilClientInputProcessor;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.util.cursorboundtask.ChooseMultipleEntityCursorBoundTask;
import bloodandmithril.world.Domain;

/**
 * {@link RoutineTaskContextMenuProvider} for {@link Attack}
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class AttackRoutineContextMenuProvider implements RoutineTaskContextMenuProvider {

	@Override
	public final ContextMenu getDailyRoutineContextMenu(final Individual host, final DailyRoutine routine) {
		final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			chooseTargetMenuItem(host, routine)
		);

		return menu;
	}


	@Override
	public final ContextMenu getEntityVisibleRoutineContextMenu(final Individual host, final EntityVisibleRoutine routine) {
		final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			chooseTargetMenuItem(host, routine)
		);

		final EntityVisible identificationFunction = routine.getIdentificationFunction();
		if (Individual.class.isAssignableFrom(identificationFunction.getEntity().a)) {
			menu.addFirst(
				new MenuItem(
					"Visible individual",
					() -> {
						routine.setAiTaskGenerator(new AttackVisibleIndividualTaskGenerator(host.getId().getId(), new EntityVisibleRoutine.VisibleIndividualFuture(routine), "visible individual"));
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
	public final ContextMenu getIndividualConditionRoutineContextMenu(final Individual host, final IndividualConditionRoutine routine) {
		final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			chooseTargetMenuItem(host, routine)
		);

		return menu;
	}


	@Override
	public final ContextMenu getStimulusDrivenRoutineContextMenu(final Individual host, final StimulusDrivenRoutine routine) {
		final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			chooseTargetMenuItem(host, routine)
		);

		return menu;
	}


	private final MenuItem chooseTargetMenuItem(final Individual host, final Routine routine) {
		return new MenuItem(
			"Choose targets",
			() -> {
				Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).setCursorBoundTask(
					new ChooseMultipleEntityCursorBoundTask<Individual, Integer>(true, Individual.class) {
						@Override
						public boolean canAdd(final Individual f) {
							return f.isAlive() && f.getId().getId() != host.getId().getId();
						}
						@Override
						public Integer transform(final Individual f) {
							return f.getId().getId();
						}
						@Override
						public void renderUIGuide(final Graphics graphics) {
							final UserInterface userInterface = Wiring.injector().getInstance(UserInterface.class);

							userInterface.getShapeRenderer().begin(ShapeType.Line);
							userInterface.getShapeRenderer().setColor(Color.RED);
							Gdx.gl20.glLineWidth(2f);
							for (final int i : entities) {
								final Individual individual = Domain.getIndividual(i);
								final Vector2 position = individual.getState().position;

								userInterface.getShapeRenderer().rect(
									worldToScreenX(position.x) - individual.getWidth()/2,
									worldToScreenY(position.y),
									individual.getWidth(),
									individual.getHeight()
								);

							}
							userInterface.getShapeRenderer().end();
						}
						@Override
						public boolean executionConditionMet() {
							final Collection<Individual> nearbyEntities = Domain.getWorld(host.getWorldId()).getPositionalIndexMap().getNearbyEntities(Individual.class, getMouseWorldX(), getMouseWorldY());
							for (final Individual indi : nearbyEntities) {
								if (indi.isMouseOver()) {
									return true;
								}
							}
							return false;
						}
						@Override
						public String getShortDescription() {
							return "Choose targets (Press enter to finalise)";
						}
						@Override
						public void keyPressed(final int keyCode) {
							if (keyCode == Keys.ENTER) {
								routine.setAiTaskGenerator(new AttackTaskGenerator(host.getId().getId(), entities));
								Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).setCursorBoundTask(null);
							}
						}
					}
				);
			},
			Color.ORANGE,
			Color.GREEN,
			Color.GRAY,
			null
		);
	}
}