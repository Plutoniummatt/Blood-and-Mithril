package bloodandmithril.ui;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.control.InputUtilities.isKeyPressed;
import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;
import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.graphics.GL20.GL_BLEND;
import static com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA;
import static com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualKineticsProcessingData;
import bloodandmithril.character.individuals.IndividualState;
import bloodandmithril.control.Controls;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.world.topography.Topography;

/**
 * Renders the UI decorations for individuals
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class IndividualUIDecorationsRenderer {

	@Inject private Graphics graphics;
	@Inject private Controls controls;
	@Inject private GameClientStateTracker gameClientStateTracker;

	/**
	 * Renders the UI decorations for the given individual
	 *
	 * @param individual
	 */
	public void render(final Individual individual, final ShapeRenderer shapeRenderer, final boolean debug, final OrthographicCamera uiTrackingCamera, final OrthographicCamera uiCamera) {
		final SpriteBatch batch = graphics.getSpriteBatch();
		final IndividualState state = individual.getState();

		if (gameClientStateTracker.isIndividualSelected(individual)) {
			batch.setShader(Shaders.filter);

			Shaders.filter.setUniformf("color",
				(float)sin(PI * (1f - state.health/state.maxHealth) / 2),
				(float)cos(PI * (1f - state.health/state.maxHealth) / 2),
				0f,
				1f
			);

			Shaders.filter.setUniformMatrix("u_projTrans", uiTrackingCamera.combined);
			batch.draw(UserInterface.currentArrow, state.position.x - 5, state.position.y + individual.getHeight() + 10);
		}

		if (debug) {
			shapeRenderer.begin(ShapeType.Line);
			Gdx.gl.glLineWidth(2f);
			shapeRenderer.setColor(Color.ORANGE);
			final Box interactionBox = individual.getInteractionBox();
			final Box hitBox = individual.getHitBox();
			shapeRenderer.rect(
				worldToScreenX(interactionBox.position.x - interactionBox.width / 2),
				worldToScreenY(interactionBox.position.y - interactionBox.height / 2),
				interactionBox.width,
				interactionBox.height
			);
			shapeRenderer.setColor(Color.RED);
			shapeRenderer.rect(
				worldToScreenX(hitBox.position.x - hitBox.width / 2),
				worldToScreenY(hitBox.position.y - hitBox.height / 2),
				hitBox.width,
				hitBox.height
			);
			shapeRenderer.end();

			gl.glEnable(GL_BLEND);
			gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(0.2f, 0.5f, 1f, 0.8f);
			final IndividualKineticsProcessingData kinematicsData = individual.getKinematicsData();
			if (kinematicsData.tileDirectlyBelowX != null && kinematicsData.tileDirectlyBelowY != null) {
				shapeRenderer.rect(
					worldToScreenX(Topography.convertToWorldCoord(kinematicsData.tileDirectlyBelowX, true)),
					worldToScreenY(Topography.convertToWorldCoord(kinematicsData.tileDirectlyBelowY, true)),
					Topography.TILE_SIZE,
					Topography.TILE_SIZE
				);
			}
			shapeRenderer.setColor(1f, 0.3f, 1f, 0.7f);
			if (kinematicsData.mostRecentTileX != null && kinematicsData.mostRecentTileY != null) {
				shapeRenderer.rect(
					worldToScreenX(Topography.convertToWorldCoord(kinematicsData.mostRecentTileX, true)),
					worldToScreenY(Topography.convertToWorldCoord(kinematicsData.mostRecentTileY, true)),
					Topography.TILE_SIZE,
					Topography.TILE_SIZE
				);
			}
			shapeRenderer.end();
			gl.glDisable(GL_BLEND);
		}

		if (individual.isAlive() && individual.isMouseOver() && isKeyPressed(controls.attack.keyCode) && !isKeyPressed(controls.rangedAttack.keyCode)) {
			if (gameClientStateTracker.getSelectedIndividuals().size() > 0 && (!gameClientStateTracker.isIndividualSelected(individual) || gameClientStateTracker.getSelectedIndividuals().size() > 1)) {
				batch.setShader(Shaders.filter);
				Shaders.filter.setUniformMatrix("u_projTrans", uiCamera.combined);
				Shaders.filter.setUniformf("color", Color.BLACK);
				Fonts.defaultFont.draw(batch, "Attack Melee", getMouseScreenX() + 14, getMouseScreenY() - 26);
				batch.flush();
				Shaders.filter.setUniformf("color", Color.ORANGE);
				Fonts.defaultFont.draw(batch, "Attack Melee", getMouseScreenX() + 15, getMouseScreenY() - 25);
			}
		}
	}
}