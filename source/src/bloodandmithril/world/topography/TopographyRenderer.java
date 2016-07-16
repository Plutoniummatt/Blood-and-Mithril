package bloodandmithril.world.topography;

import static bloodandmithril.world.topography.Topography.CHUNK_SIZE;
import static bloodandmithril.world.topography.Topography.TILE_SIZE;

import org.lwjgl.opengl.Display;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.util.Operator;

/**
 * Renders {@link Topography}
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class TopographyRenderer {

	@Inject
	private ChunkRenderer chunkRenderer;
	
	/**
	 * Renders the background
	 */
	public final void renderBackGround(Topography topography, int camX, int camY, ShaderProgram shader, Operator<ShaderProgram> uniformSettings, Graphics graphics) {
		int bottomLeftX 	= (camX - Display.getWidth() / 2) / (CHUNK_SIZE * TILE_SIZE);
		int bottomLeftY 	= (camY - Display.getHeight() / 2) / (CHUNK_SIZE * TILE_SIZE);
		int topRightX 		= bottomLeftX + Display.getWidth() / (CHUNK_SIZE * TILE_SIZE);
		int topRightY		= bottomLeftY + Display.getHeight() / (CHUNK_SIZE * TILE_SIZE);

		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Topography.TILE_TEXTURE_ATLAS.bind();
		for (int x = bottomLeftX - 2; x <= topRightX + 2; x++) {
			for (int y = bottomLeftY - 2; y <= topRightY + 2; y++) {
				if (topography.getChunkMap().get(x) != null && topography.getChunkMap().get(x).get(y) != null) {
					topography.getChunkMap().get(x).get(y).checkMesh();
					chunkRenderer.render(topography.getChunkMap().get(x).get(y), false, graphics.getCam(), shader, uniformSettings);
				}
			}
		}
	}


	/**
	 * Renders the foreground
	 */
	public final void renderForeGround(Topography topography, int camX, int camY, ShaderProgram shader, Operator<ShaderProgram> uniformSettings, Graphics graphics) {
		int bottomLeftX 	= (camX - Display.getWidth() / 2) / (CHUNK_SIZE * TILE_SIZE);
		int bottomLeftY 	= (camY - Display.getHeight() / 2) / (CHUNK_SIZE * TILE_SIZE);
		int topRightX 		= bottomLeftX + Display.getWidth() / (CHUNK_SIZE * TILE_SIZE);
		int topRightY		= bottomLeftY + Display.getHeight() / (CHUNK_SIZE * TILE_SIZE);

		Topography.TILE_TEXTURE_ATLAS.bind();
		for (int x = bottomLeftX - 2; x <= topRightX + 2; x++) {
			for (int y = bottomLeftY - 2; y <= topRightY + 2; y++) {
				if (topography.getChunkMap().get(x) != null && topography.getChunkMap().get(x).get(y) != null) {
					topography.getChunkMap().get(x).get(y).checkMesh();
					chunkRenderer.render(topography.getChunkMap().get(x).get(y), true, graphics.getCam(), shader, uniformSettings);
				}
			}
		}
	}
}