package bloodandmithril.world.weather;

import static com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
import static com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest;

import java.util.Map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.graphics.Textures;
import bloodandmithril.graphics.background.Layer;
import bloodandmithril.util.Shaders;
import bloodandmithril.world.World;

/**
 * Renders {@link Cloud}s
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class CloudRenderer {

	public static final Map<Integer, TextureRegion> cloudTextures = Maps.newHashMap();
	
	@Inject private Graphics graphics;
	
	@Inject
	CloudRenderer() {
		cloudTextures.put(1, new TextureRegion(Textures.GAME_WORLD_TEXTURE, 1, 523, 199, 69));
		cloudTextures.put(2, new TextureRegion(Textures.GAME_WORLD_TEXTURE, 201, 523, 288, 87));
		cloudTextures.put(3, new TextureRegion(Textures.GAME_WORLD_TEXTURE, 490, 523, 218, 71));
	}
	
	
	public void renderClouds(World world) {
		Textures.GAME_WORLD_TEXTURE.setFilter(Linear, Linear);
		graphics.getSpriteBatch().begin();
		graphics.getSpriteBatch().setShader(Shaders.pass);
		for (Cloud cloud : world.getClouds()) {
			render(cloud);
			graphics.getSpriteBatch().flush();
		}
		graphics.getSpriteBatch().end();
		Textures.GAME_WORLD_TEXTURE.setFilter(Nearest, Nearest);
	}
	
	
	private void render(Cloud cloud) {
		graphics.getSpriteBatch().draw(
			cloudTextures.get(cloud.cloudTextureId), 
			cloud.getxPosition(), 
			Layer.getScreenHorizonY(graphics)
		);
		
		cloud.setxPosition(cloud.getxPosition() + 0.01f);
	}
}
