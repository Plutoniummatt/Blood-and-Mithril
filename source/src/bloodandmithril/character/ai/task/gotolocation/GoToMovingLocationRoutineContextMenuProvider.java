package bloodandmithril.character.ai.task.gotolocation;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.control.InputUtilities.getMouseWorldX;
import static bloodandmithril.control.InputUtilities.getMouseWorldY;
import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;
import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.graphics.GL20.GL_BLEND;
import static com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA;
import static com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.RoutineTaskContextMenuProvider;
import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.routine.daily.DailyRoutine;
import bloodandmithril.character.ai.routine.entityvisible.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.individualcondition.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.stimulusdriven.StimulusDrivenRoutine;
import bloodandmithril.character.ai.task.gotolocation.GoToMovingLocation.GoToMovingLocationTaskGenerator;
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
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * Provides context menus for {@link GoToMovingLocation}
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class GoToMovingLocationRoutineContextMenuProvider implements RoutineTaskContextMenuProvider {

	@Inject private GameClientStateTracker gameClientStateTracker;

	@Override
	public ContextMenu getDailyRoutineContextMenu(final Individual host, final DailyRoutine routine) {
		final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			chooseLocationMenuItem(host, routine)
		);

		return menu;
	}


	@Override
	public ContextMenu getEntityVisibleRoutineContextMenu(final Individual host, final EntityVisibleRoutine routine) {
		final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			chooseLocationMenuItem(host, routine)
		);

		return menu;
	}


	@Override
	public ContextMenu getIndividualConditionRoutineContextMenu(final Individual host, final IndividualConditionRoutine routine) {
		final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			chooseLocationMenuItem(host, routine)
		);

		return menu;
	}


	@Override
	public ContextMenu getStimulusDrivenRoutineContextMenu(final Individual host, final StimulusDrivenRoutine routine) {
		final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			chooseLocationMenuItem(host, routine)
		);

		return menu;
	}
	
	
	private MenuItem chooseLocationMenuItem(final Individual host, final Routine routine) {
		return new MenuItem(
			"Choose location",
			() -> {
				final JITTask task = new JITTask() {
					@Override
					public void execute(final Object... args) {
						Vector2 coords;
						try {
							coords = gameClientStateTracker.getActiveWorld().getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(getMouseWorldX(), getMouseWorldY(), true);
						} catch (final NoTileFoundException e) {
							return;
						}

						final float x = getMouseWorldX();
						final float y = coords.y;

						routine.setAiTaskGenerator(
							new GoToMovingLocationTaskGenerator(x, y, host.getId().getId())
						);
					}
				};

				Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).setCursorBoundTask(new CursorBoundTask(task, true) {
					@Override
					public void renderUIGuide(final Graphics graphics) {
						try {
							final Vector2 coords = gameClientStateTracker.getActiveWorld().getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(getMouseWorldX(), getMouseWorldY(), true);

							final float x = worldToScreenX(getMouseWorldX());
							final float y = worldToScreenY(coords.y);

							gl.glEnable(GL_BLEND);
							gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
							graphics.getSpriteBatch().begin();
							graphics.getSpriteBatch().setColor(executionConditionMet() ? Color.GREEN : Color.RED);
							graphics.getSpriteBatch().draw(UserInterface.currentArrow, x - 5, y);
							graphics.getSpriteBatch().end();
							gl.glDisable(GL_BLEND);
						} catch (final NoTileFoundException e) {}
					}

					@Override
					public String getShortDescription() {
						return "Choose location";
					}

					@Override
					public CursorBoundTask getImmediateTask() {
						return null;
					}

					@Override
					public boolean executionConditionMet() {
						return true;
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