package bloodandmithril.graphics;

import static com.badlogic.gdx.Gdx.files;
import static com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
import static com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import bloodandmithril.core.Copyright;
import bloodandmithril.prop.plant.tree.Tree;

/**
 * Holds all {@link Texture}s
 * 
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class Textures {
	/** Textures */
	public static Texture GAME_WORLD_TEXTURE;
	public static Texture INDIVIDUAL_TEXTURE;
	
	/** All trunk textures */
	public static Map<Class<? extends Tree>, Map<Integer, TextureRegion>> trunkTextures = newHashMap();
	
	static {
		GAME_WORLD_TEXTURE = new Texture(files.internal("data/image/gameWorld.png"));
		GAME_WORLD_TEXTURE.setFilter(Linear, Linear);

		INDIVIDUAL_TEXTURE = new Texture(files.internal("data/image/character/individual.png"));
		INDIVIDUAL_TEXTURE.setFilter(Nearest, Nearest);
	}
}