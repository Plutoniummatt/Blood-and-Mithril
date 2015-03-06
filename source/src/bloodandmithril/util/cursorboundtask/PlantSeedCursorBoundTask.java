package bloodandmithril.util.cursorboundtask;

import static bloodandmithril.core.BloodAndMithrilClient.getMouseWorldX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseWorldY;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.core.BloodAndMithrilClient.worldToScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.worldToScreenY;
import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.graphics.GL20.GL_BLEND;
import static com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA;
import static com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA;
import bloodandmithril.character.ai.task.PlantSeed;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.item.items.food.plant.Seed;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.CursorBoundTask;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.tiles.SoilTile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/**
 * {@link CursorBoundTask} to plant {@link Seed}s
 *
 * @author Matt
 */
public class PlantSeedCursorBoundTask extends CursorBoundTask {

	private final Seed toPlant;

	/**
	 * Constructor
	 */
	public PlantSeedCursorBoundTask(Seed seed, Container planter) {
		super(
			args -> {
				bloodandmithril.prop.plant.seed.SeedProp propSeed = seed.getPropSeed();
				Vector2 coords;
				try {
					coords = Domain.getActiveWorld().getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(getMouseWorldX(), getMouseWorldY(), true);
				} catch (NoTileFoundException e) {
					return;
				}
				propSeed.position.x = getMouseWorldX();
				propSeed.position.y = coords.y;

				if (planter instanceof Individual) {
					if (ClientServerInterface.isServer()) {
						try {
							((Individual) planter).getAI().setCurrentTask(new PlantSeed((Individual) planter, propSeed));
						} catch (NoTileFoundException e) {}
					} else {
						ClientServerInterface.SendRequest.sendPlantSeedRequest((Individual) planter, propSeed);
					}
				}
			},
			true
		);
		this.toPlant = seed;
	}


	@Override
	public void renderUIGuide() {
		try {
			Vector2 coords = Domain.getActiveWorld().getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(getMouseWorldX(), getMouseWorldY(), true);

			float x = worldToScreenX(getMouseWorldX());
			float y = worldToScreenY(coords.y);

			gl.glEnable(GL_BLEND);
			gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			spriteBatch.begin();
			spriteBatch.setColor(executionConditionMet() ? Color.GREEN : Color.RED);
			spriteBatch.draw(UserInterface.currentArrow, x - 5, y);
			spriteBatch.end();
			gl.glDisable(GL_BLEND);
		} catch (NoTileFoundException e) {}
	}


	@Override
	public boolean executionConditionMet() {
		try {
			Vector2 coords = Domain.getActiveWorld().getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(getMouseWorldX(), getMouseWorldY(), true);

			Tile tile = Domain.getActiveWorld().getTopography().getTile(getMouseWorldX(), coords.y - Topography.TILE_SIZE / 2, true);
			return tile instanceof SoilTile && ((SoilTile) tile).canPlant(toPlant);
		} catch (NoTileFoundException e) {
			return false;
		}
	}


	@Override
	public String getShortDescription() {
		return "Plant " + toPlant.getSingular(false);
	}


	@Override
	public boolean canCancel() {
		return true;
	}
}