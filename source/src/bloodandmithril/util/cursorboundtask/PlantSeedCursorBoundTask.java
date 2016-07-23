package bloodandmithril.util.cursorboundtask;

import static bloodandmithril.control.InputUtilities.getMouseWorldX;
import static bloodandmithril.control.InputUtilities.getMouseWorldY;
import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;
import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.graphics.GL20.GL_BLEND;
import static com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA;
import static com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.task.plantseed.PlantSeed;
import bloodandmithril.character.ai.task.plantseed.PlantSeed.PlantSeedTaskGenerator;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.control.BloodAndMithrilClientInputProcessor;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.item.items.food.plant.SeedItem;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.CursorBoundTask;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.tiles.SoilTile;

/**
 * {@link CursorBoundTask} to plant {@link SeedItem}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class PlantSeedCursorBoundTask extends CursorBoundTask {

	private final SeedItem toPlant;
	private final List<Vector2> plantingLocations = Lists.newArrayList();
	private final Routine routine;
	private final Individual planter;

	@Inject private GameClientStateTracker gameClientStateTracker;

	/**
	 * Constructor
	 */
	public PlantSeedCursorBoundTask(final SeedItem seed, final Individual planter, final Routine routine) {
		super(
			args -> {
				final bloodandmithril.prop.plant.seed.SeedProp propSeed = seed.getPropSeed();
				Vector2 coords;
				try {
					coords = Wiring.injector().getInstance(GameClientStateTracker.class).getActiveWorld().getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(getMouseWorldX(), getMouseWorldY(), true);
				} catch (final NoTileFoundException e) {
					return;
				}
				propSeed.position.x = getMouseWorldX();
				propSeed.position.y = coords.y;

				if (planter instanceof Individual) {
					if (ClientServerInterface.isServer()) {
						try {
							planter.getAI().setCurrentTask(new PlantSeed(planter, propSeed));
						} catch (final NoTileFoundException e) {}
					} else {
						ClientServerInterface.SendRequest.sendPlantSeedRequest(planter, propSeed);
					}
				}
			},
			true
		);
		this.toPlant = seed;
		this.routine = routine;
		this.planter = planter;
	}


	@Override
	public final void renderUIGuide(final Graphics graphics) {
		try {
			final Vector2 coords = gameClientStateTracker.getActiveWorld().getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(getMouseWorldX(), getMouseWorldY(), true);

			final float x = worldToScreenX(getMouseWorldX());
			final float y = worldToScreenY(coords.y);

			gl.glEnable(GL_BLEND);
			gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			graphics.getSpriteBatch().begin();
			graphics.getSpriteBatch().setColor(executionConditionMet() ? Color.GREEN : Color.RED);
			graphics.getSpriteBatch().draw(UserInterface.currentArrow, x - 5, y);
			for (final Vector2 location : plantingLocations) {
				graphics.getSpriteBatch().draw(UserInterface.currentArrow, worldToScreenX(location.x), worldToScreenY(location.y));
			}
			graphics.getSpriteBatch().end();
			gl.glDisable(GL_BLEND);

		} catch (final NoTileFoundException e) {}
	}


	@Override
	public final boolean executionConditionMet() {
		try {
			final Vector2 coords = gameClientStateTracker.getActiveWorld().getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(getMouseWorldX(), getMouseWorldY(), true);

			final Tile tile = gameClientStateTracker.getActiveWorld().getTopography().getTile(getMouseWorldX(), coords.y - Topography.TILE_SIZE / 2, true);
			return tile instanceof SoilTile && ((SoilTile) tile).canPlant(toPlant);
		} catch (final NoTileFoundException e) {
			return false;
		}
	}


	@Override
	public final String getShortDescription() {
		return "Plant " + toPlant.getSingular(false);
	}


	@Override
	public final boolean canCancel() {
		return true;
	}


	@Override
	public CursorBoundTask getImmediateTask() {
		return null;
	}


	public final List<Vector2> getPlantingLocations() {
		return plantingLocations;
	}


	@Override
	public final void keyPressed(final int keyCode) {
		routine.setAiTaskGenerator(new PlantSeedTaskGenerator(getPlantingLocations(), planter.getId().getId(), toPlant));
		Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).setCursorBoundTask(null);
	}
}