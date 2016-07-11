package bloodandmithril.graphics.renderers;

import static bloodandmithril.world.topography.Topography.CHUNK_SIZE;
import static bloodandmithril.world.topography.Topography.TILE_SIZE;

import org.lwjgl.opengl.Display;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.util.Operator;
import bloodandmithril.world.topography.Topography;

/**
 * Renders a {@link Topography}
 */
@Copyright("Matthew Peck 2016")
public class TopographyRenderer {

	/** The texture atlas containing all textures for tiles */
	public static Texture atlas;


	public static final void setup() {
		atlas = new Texture(Gdx.files.internal("data/image/textureAtlas.png"));
	}

	/**
	 * Renders the background
	 */
	public final void renderBackGround(final Topography topo, final int camX, final int camY, final ShaderProgram shader, final Operator<ShaderProgram> uniformSettings, final Graphics graphics) {
		final int bottomLeftX 	= (camX - Display.getWidth() / 2) / (CHUNK_SIZE * TILE_SIZE);
		final int bottomLeftY 	= (camY - Display.getHeight() / 2) / (CHUNK_SIZE * TILE_SIZE);
		final int topRightX 		= bottomLeftX + Display.getWidth() / (CHUNK_SIZE * TILE_SIZE);
		final int topRightY		= bottomLeftY + Display.getHeight() / (CHUNK_SIZE * TILE_SIZE);

		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		atlas.bind();
		for (int x = bottomLeftX - 2; x <= topRightX + 2; x++) {
			for (int y = bottomLeftY - 2; y <= topRightY + 2; y++) {
				if (topo.getChunkMap().get(x) != null && topo.getChunkMap().get(x).get(y) != null) {
					topo.getChunkMap().get(x).get(y).checkMesh();
					topo.getChunkMap().get(x).get(y).render(false, graphics.getCam(), shader, uniformSettings);
				}
			}
		}
	}


	/**
	 * Renders the foreground
	 */
	public final void renderForeGround(final Topography topo,final int camX, final int camY, final ShaderProgram shader, final Operator<ShaderProgram> uniformSettings, final Graphics graphics) {
		final int bottomLeftX 	= (camX - Display.getWidth() / 2) / (CHUNK_SIZE * TILE_SIZE);
		final int bottomLeftY 	= (camY - Display.getHeight() / 2) / (CHUNK_SIZE * TILE_SIZE);
		final int topRightX 		= bottomLeftX + Display.getWidth() / (CHUNK_SIZE * TILE_SIZE);
		final int topRightY		= bottomLeftY + Display.getHeight() / (CHUNK_SIZE * TILE_SIZE);

		atlas.bind();
		for (int x = bottomLeftX - 2; x <= topRightX + 2; x++) {
			for (int y = bottomLeftY - 2; y <= topRightY + 2; y++) {
				if (topo.getChunkMap().get(x) != null && topo.getChunkMap().get(x).get(y) != null) {
					topo.getChunkMap().get(x).get(y).checkMesh();
					topo.getChunkMap().get(x).get(y).render(true, graphics.getCam(), shader, uniformSettings);
				}
			}
		}
	}
}
