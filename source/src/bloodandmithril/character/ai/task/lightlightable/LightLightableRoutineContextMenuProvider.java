package bloodandmithril.character.ai.task.lightlightable;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.control.InputUtilities.getMouseWorldX;
import static bloodandmithril.control.InputUtilities.getMouseWorldY;
import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.google.inject.Singleton;

import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.RoutineTaskContextMenuProvider;
import bloodandmithril.character.ai.routine.daily.DailyRoutine;
import bloodandmithril.character.ai.routine.entityvisible.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.entityvisible.EntityVisibleRoutine.EntityVisible;
import bloodandmithril.character.ai.routine.individualcondition.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.stimulusdriven.StimulusDrivenRoutine;
import bloodandmithril.character.ai.task.lightlightable.LightLightable.GenerateLightAnyVisibleLightables;
import bloodandmithril.character.ai.task.lightlightable.LightLightable.LightLightablesInAreaTaskGenerator;
import bloodandmithril.character.ai.task.lightlightable.LightLightable.LightSelectedLightablesTaskGenerator;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.control.BloodAndMithrilClientInputProcessor;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.prop.Lightable;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.util.CursorBoundTask;
import bloodandmithril.util.cursorboundtask.ChooseAreaCursorBoundTask;
import bloodandmithril.util.cursorboundtask.ChooseMultipleEntityCursorBoundTask;

/**
 * Provides Context menus for {@link LightLightable}
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class LightLightableRoutineContextMenuProvider implements RoutineTaskContextMenuProvider {

	@Override
	public ContextMenu getDailyRoutineContextMenu(final Individual host, final DailyRoutine routine) {
		return getContextMenu(routine, host);
	}


	@Override
	public ContextMenu getEntityVisibleRoutineContextMenu(final Individual host, final EntityVisibleRoutine routine) {
		final ContextMenu contextMenu = getContextMenu(routine, host);

		final EntityVisible identificationFunction = routine.getIdentificationFunction();
		if (Lightable.class.isAssignableFrom(identificationFunction.getEntity().a)) {
			contextMenu.addFirst(
				new MenuItem(
					"Visible lightable entity",
					() -> {
						routine.setAiTaskGenerator(new GenerateLightAnyVisibleLightables(host.getId().getId()));
					},
					Color.MAGENTA,
					Color.GREEN,
					Color.GRAY,
					null
				)
			);
		}
		return contextMenu;
	}


	@Override
	public ContextMenu getIndividualConditionRoutineContextMenu(final Individual host, final IndividualConditionRoutine routine) {
		return getContextMenu(routine, host);
	}


	@Override
	public ContextMenu getStimulusDrivenRoutineContextMenu(final Individual host, final StimulusDrivenRoutine routine) {
		return getContextMenu(routine, host);
	}
	
	
	private ContextMenu getContextMenu(final Routine routine, final Individual host) {
		return new ContextMenu(getMouseScreenX(), getMouseScreenY(), true,
			new MenuItem(
				"Light lightables in area",
				() -> {
					Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).setCursorBoundTask(
						new ChooseAreaCursorBoundTask(
							args -> {
								routine.setAiTaskGenerator(new LightLightablesInAreaTaskGenerator((Vector2) args[0], (Vector2) args[1], host.getId().getId()));
							},
							true
						) {
							@Override
							public String getShortDescription() {
								return "Choose area";
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
							public CursorBoundTask getImmediateTask() {
								return null;
							}
							@Override
							public void keyPressed(final int keyCode) {
							}
						}
					);
				},
				Color.ORANGE,
				Color.GREEN,
				Color.GRAY,
				null
			),
			new MenuItem(
				"Light selected",
				() -> {
					Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).setCursorBoundTask(
						new ChooseMultipleEntityCursorBoundTask<Prop, Integer>(true, Prop.class) {
							@Override
							public boolean canAdd(final Prop f) {
								return Lightable.class.isAssignableFrom(f.getClass());
							}
							@Override
							public Integer transform(final Prop f) {
								return f.id;
							}
							@Override
							public void renderUIGuide(final Graphics graphics) {
								final UserInterface userInterface = Wiring.injector().getInstance(UserInterface.class);
								userInterface.getShapeRenderer().begin(ShapeType.Line);
								userInterface.getShapeRenderer().setColor(Color.RED);
								Gdx.gl20.glLineWidth(2f);
								for (final int i : entities) {
									final Prop p = Wiring.injector().getInstance(GameClientStateTracker.class).getActiveWorld().props().getProp(i);
									final Vector2 position = p.position;

									userInterface.getShapeRenderer().rect(
										worldToScreenX(position.x) - p.width/2,
										worldToScreenY(position.y),
										p.width,
										p.height
									);

								}
								userInterface.getShapeRenderer().end();
							}
							@Override
							public boolean executionConditionMet() {
								for (final Prop prop : Wiring.injector().getInstance(GameClientStateTracker.class).getActiveWorld().getPositionalIndexMap().getNearbyEntities(Prop.class, getMouseWorldX(), getMouseWorldY())) {
									if (Lightable.class.isAssignableFrom(prop.getClass())) {
										return true;
									}
								}
								return false;
							}
							@Override
							public String getShortDescription() {
								return "Select lightables";
							}
							@Override
							public void keyPressed(final int keyCode) {
								if (keyCode == Keys.ENTER) {
									routine.setAiTaskGenerator(new LightSelectedLightablesTaskGenerator(host.getId().getId(), entities, host.getWorldId()));
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
			)
		);
	}
}