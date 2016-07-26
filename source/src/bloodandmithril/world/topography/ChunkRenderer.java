package bloodandmithril.world.topography;

import org.lwjgl.opengl.GL11;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.util.Operator;

/**
 * Renders {@link Chunk}
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class ChunkRenderer {

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

		Gdx.gl.glDisable(GL20.GL_BLEND);
	}
}