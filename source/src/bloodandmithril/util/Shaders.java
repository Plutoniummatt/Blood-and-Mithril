package bloodandmithril.util;

import bloodandmithril.graphics.DynamicLightingPostRenderer;
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
	public static ShaderProgram invertY = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/invertYAxis.fp"));
	public static ShaderProgram invertYBlendWithOcclusion = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/invertYBlendWithOcclusion.fp"));
	public static ShaderProgram text = SpriteBatch.createDefaultShader();
	public static ShaderProgram filter = colorFilterShader();
	public static ShaderProgram invertAlphaSolidColor = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/invertAlphaSolidColor.fp"));
	public static ShaderProgram colorSmear = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/colorSmearGaussian.fp"));

	public static ShaderProgram sun = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/legacy/sun.fp"));
	public static ShaderProgram shadow = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/legacy/shadow.fp"));
	public static ShaderProgram shadowMap = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/legacy/1DShadowMap.fp"));
	public static ShaderProgram defaultBackGroundTiles = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/legacy/backGroundTileShader.fp"));
	public static ShaderProgram defaultForeGroundTiles = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/legacy/foreGroundTileShader.fp"));
	public static ShaderProgram daylightShader = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/legacy/daylightShader.fp"));
	public static ShaderProgram black = colorFilterShader(Color.BLACK);
	public static ShaderProgram moon = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/legacy/moonShader.fp"));
	public static ShaderProgram elfHighLight = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/legacy/elf/highLight.fp"));
	public static ShaderProgram elfDayLight = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/legacy/elf/dayLight.fp"));

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
		defaultBackGroundTiles.setUniformf("dayLight", WorldState.getCurrentEpoch().dayLight());
		defaultBackGroundTiles.setUniformf("resolution", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		defaultBackGroundTiles.end();

		defaultForeGroundTiles.begin();
		defaultForeGroundTiles.setUniformf("debugSwitch", DynamicLightingPostRenderer.SEE_ALL ? 1f : 0f);
		defaultForeGroundTiles.end();
	}
}