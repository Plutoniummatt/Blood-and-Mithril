package bloodandmithril.util.cursorboundtask;

import static bloodandmithril.core.BloodAndMithrilClient.getMouseWorldX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseWorldY;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.core.BloodAndMithrilClient.worldToScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.worldToScreenY;
import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.graphics.GL10.GL_BLEND;
import static com.badlogic.gdx.graphics.GL10.GL_ONE_MINUS_SRC_ALPHA;
import static com.badlogic.gdx.graphics.GL10.GL_SRC_ALPHA;
import bloodandmithril.item.items.food.plant.Seed;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.CursorBoundTask;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
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
	public PlantSeedCursorBoundTask(Seed seed) {
		super(
			args -> {

			},
			true
		);
		this.toPlant = seed;
	}


	@Override
	public void renderUIGuide() {
		Vector2 coords = Domain.getActiveWorld().getTopography().getLowestEmptyOrPlatformTileWorldCoords(getMouseWorldX(), getMouseWorldY(), true);

		float x = worldToScreenX(getMouseWorldX());
		float y = worldToScreenY(coords.y);

		Tile tile = Domain.getActiveWorld().getTopography().getTile(getMouseWorldX(), coords.y - Topography.TILE_SIZE / 2, true);
		boolean canPlant = tile instanceof SoilTile && ((SoilTile) tile).canPlant(toPlant);

		gl.glEnable(GL_BLEND);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		spriteBatch.begin();
		spriteBatch.setColor(canPlant ? Color.GREEN : Color.RED);
		spriteBatch.draw(UserInterface.currentArrow, x - 5, y);
		spriteBatch.end();
		gl.glDisable(GL_BLEND);
	}


	@Override
	public boolean executionConditionMet() {
		return false;
	}


	@Override
	public String getShortDescription() {
		return "Plant " + toPlant.getSingular(false);
	}
}