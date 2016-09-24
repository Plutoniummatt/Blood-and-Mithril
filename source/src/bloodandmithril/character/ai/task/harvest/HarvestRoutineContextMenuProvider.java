package bloodandmithril.character.ai.task.harvest;

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
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.RoutineTaskContextMenuProvider;
import bloodandmithril.character.ai.routine.daily.DailyRoutine;
import bloodandmithril.character.ai.routine.entityvisible.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.entityvisible.EntityVisibleRoutine.EntityVisible;
import bloodandmithril.character.ai.routine.individualcondition.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.stimulusdriven.StimulusDrivenRoutine;
import bloodandmithril.character.ai.task.harvest.Harvest.HarvestAreaTaskGenerator;
import bloodandmithril.character.ai.task.harvest.Harvest.HarvestSelectedHarvestablesTaskGenerator;
import bloodandmithril.character.ai.task.harvest.Harvest.HarvestVisibleEntityTaskGenerator;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.control.BloodAndMithrilClientInputProcessor;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.prop.Harvestable;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.util.CursorBoundTask;
import bloodandmithril.util.cursorboundtask.ChooseAreaCursorBoundTask;
import bloodandmithril.util.cursorboundtask.ChooseMultipleEntityCursorBoundTask;

/**
 * Provides context menus for {@link Harvest}
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class HarvestRoutineContextMenuProvider implements RoutineTaskContextMenuProvider {
	
	@Inject private GameClientStateTracker gameClientStateTracker;

	@Override
	public final ContextMenu getDailyRoutineContextMenu(final Individual host, final DailyRoutine routine) {
		return getChoices(routine, host);
	}


	@Override
	public final ContextMenu getEntityVisibleRoutineContextMenu(final Individual host, final EntityVisibleRoutine routine) {
		final ContextMenu choices = getChoices(routine, host);

		final EntityVisible identificationFunction = routine.getIdentificationFunction();
		if (Harvestable.class.isAssignableFrom(identificationFunction.getEntity().a)) {
			choices.addFirst(
				new MenuItem(
					"Visible harvestable entity",
					() -> {
						routine.setAiTaskGenerator(new HarvestVisibleEntityTaskGenerator(host.getId().getId(), host.getWorldId(), new EntityVisibleRoutine.VisiblePropFuture(routine)));
					},
					Color.MAGENTA,
					Color.GREEN,
					Color.GRAY,
					null
				)
			);
		}

		return choices;
	}


	@Override
	public final ContextMenu getIndividualConditionRoutineContextMenu(final Individual host, final IndividualConditionRoutine routine) {
		return getChoices(routine, host);
	}


	@Override
	public final ContextMenu getStimulusDrivenRoutineContextMenu(final Individual host, final StimulusDrivenRoutine routine) {
		return getChoices(routine, host);
	}
	
	
	private final ContextMenu getChoices(final Routine routine, final Individual host) {
		return new ContextMenu(getMouseScreenX(), getMouseScreenY(), true,
			new ContextMenu.MenuItem(
				"Harvest area",
				() -> {
					Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).setCursorBoundTask(
						new ChooseAreaCursorBoundTask(
							args -> {
								routine.setAiTaskGenerator(new HarvestAreaTaskGenerator((Vector2) args[0], (Vector2) args[1], host.getId().getId()));
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
			new ContextMenu.MenuItem(
				"Harvest selected",
				() -> {
					Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).setCursorBoundTask(
						new ChooseMultipleEntityCursorBoundTask<Prop, Integer>(true, Prop.class) {
							@Override
							public boolean canAdd(final Prop f) {
								return Harvestable.class.isAssignableFrom(f.getClass());
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
									final Prop p = gameClientStateTracker.getActiveWorld().props().getProp(i);
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
								final Collection<Prop> nearbyEntities = gameClientStateTracker.getActiveWorld().getPositionalIndexChunkMap().getNearbyEntities(Prop.class, getMouseWorldX(), getMouseWorldY());
								for (final Prop p : nearbyEntities) {
									if (p.isMouseOver() && Harvestable.class.isAssignableFrom(p.getClass())) {
										return true;
									}
								}
								return false;
							}
							@Override
							public String getShortDescription() {
								return "Choose entities to harvest (Press enter to finalise)";
							}
							@Override
							public void keyPressed(final int keyCode) {
								if (keyCode == Keys.ENTER) {
									routine.setAiTaskGenerator(new HarvestSelectedHarvestablesTaskGenerator(host.getId().getId(), entities, gameClientStateTracker.getActiveWorld().getWorldId()));
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