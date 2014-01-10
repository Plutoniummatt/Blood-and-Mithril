package bloodandmithril.util;


import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.world.WorldState;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Helper class to create shaders
 *
 * @author Matt
 */
public class Shaders {

	public static ShaderProgram pass = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/pass.fp"));

	public static ShaderProgram sun = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/sun.fp"));

	public static ShaderProgram shadow = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/shadow.fp"));

	public static ShaderProgram shadowMap = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/1DShadowMap.fp"));

	public static ShaderProgram defaultBackGroundTiles = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/backGroundTileShader.fp"));

	public static ShaderProgram defaultForeGroundTiles = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/foreGroundTileShader.fp"));

	public static ShaderProgram text = SpriteBatch.createDefaultShader();

	public static ShaderProgram dark = colorFilterShader(new Color(0.2f, 0.2f, 0.2f, 1f));

	public static ShaderProgram black = colorFilterShader(Color.BLACK);

	public static ShaderProgram filter = colorFilterShader();

	public static ShaderProgram moon =  new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/moonShader.fp"));

	public static ShaderProgram elfHighLight = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/elf/highLight.fp"));

	public static ShaderProgram elfDayLight = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/elf/dayLight.fp"));


	public static void setup() {
		ShaderProgram.pedantic = false;
		Logger.generalDebug("Shaders loaded", LogLevel.DEBUG);
	}

	/**
	 * Color filter shader
	 */
	public static ShaderProgram colorFilterShader() {
		return new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/filter.fp"));
	}

	/**
	 * Color filter shader
	 */
	public static ShaderProgram colorFilterShader(Color color) {
		ShaderProgram shaderProgram = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/filter.fp"));
		shaderProgram.begin();
		shaderProgram.setUniformf("color", color);
		shaderProgram.end();
		return shaderProgram;
	}

	/**
	 * Updates the uniform variables on shaders
	 */
	public static void updateShaderUniforms() {
		defaultBackGroundTiles.begin();
		defaultBackGroundTiles.setUniformf("dayLight", WorldState.currentEpoch.dayLight());
		defaultBackGroundTiles.setUniformf("lightSource", BloodAndMithrilClient.getMouseScreenX(), BloodAndMithrilClient.getMouseScreenY());
		defaultBackGroundTiles.setUniformf("resolution", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		defaultBackGroundTiles.end();

		defaultForeGroundTiles.begin();
		defaultForeGroundTiles.setUniformf("debugSwitch", "true".equals(System.getProperty("seeAll")) ? 1f : 0f);
		defaultForeGroundTiles.end();
	}
}