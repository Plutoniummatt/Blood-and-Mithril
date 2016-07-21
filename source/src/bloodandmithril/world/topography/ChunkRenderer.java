package bloodandmithril.world.topography;

import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;

import org.lwjgl.opengl.GL11;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.Operator;

/**
 * Renders {@link Chunk}
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class ChunkRenderer {

	@Inject private UserInterface userInterface;

	/**
	 * Renders this chunk
	 */
	public final void render(final Chunk chunk, final boolean foreGround, final Camera camera, final ShaderProgram shader, final Operator<ShaderProgram> uniformSettings) {
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		if (foreGround) {
			shader.begin();
			shader.setUniformMatrix("u_projTrans", camera.combined);
			shader.setUniformi("u_texture", 0);
			uniformSettings.operate(shader);
			chunk.getFMesh().render(shader, GL11.GL_QUADS);
			shader.end();
		} else {
			shader.begin();
			shader.setUniformMatrix("u_projTrans", camera.combined);
			shader.setUniformi("u_texture", 0);
			uniformSettings.operate(shader);
			chunk.getBMesh().render(shader, GL11.GL_QUADS);
			shader.end();
		}

		if (userInterface.DEBUG) {
			userInterface.getShapeRenderer().begin(ShapeType.Line);
			userInterface.getShapeRenderer().setColor(new Color(1f, 0.5f, 1f, 0.15f));
			final float x = chunk.getFData().xChunkCoord * Topography.CHUNK_SIZE * Topography.TILE_SIZE;
			final float y = chunk.getFData().yChunkCoord * Topography.CHUNK_SIZE * Topography.TILE_SIZE;
			userInterface.getShapeRenderer().rect(worldToScreenX(x), worldToScreenY(y), Topography.CHUNK_SIZE * Topography.TILE_SIZE, Topography.CHUNK_SIZE * Topography.TILE_SIZE);
			userInterface.getShapeRenderer().end();
		}
		Gdx.gl.glDisable(GL20.GL_BLEND);
	}
}