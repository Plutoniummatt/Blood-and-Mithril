package bloodandmithril.util;

import bloodandmithril.graphics.GaussianLightingRenderer;
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
	public static ShaderProgram text = SpriteBatch.createDefaultShader();
	public static ShaderProgram filter = colorFilterShader();
	public static ShaderProgram replaceColor = replaceColorShader();

	public static ShaderProgram invertY = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/defaultRenderer/invertYAxis.fp"));
	public static ShaderProgram invertYBlendWithOcclusionBackground = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/defaultRenderer/invertYBlendWithOcclusionBackground.fp"));
	public static ShaderProgram invertYBlendWithOcclusion = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/defaultRenderer/invertYBlendWithOcclusion.fp"));
	public static ShaderProgram invertYDoubleBlendWithTwoOcclusions = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/defaultRenderer/invertYDoubleBlendWithTwoOcclusions.fp"));
	public static ShaderProgram invertAlphaSolidColor = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/defaultRenderer/invertAlphaSolidColor.fp"));
	public static ShaderProgram invertAlphaSolidColorBlend = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/defaultRenderer/invertAlphaSolidColorBlend.fp"));
	public static ShaderProgram colorSmearLargeRadius = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/defaultRenderer/colorSmearLargeRadius.fp"));
	public static ShaderProgram colorSmearSmallRadius = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/defaultRenderer/colorSmearSmallRadius.fp"));
	public static ShaderProgram blendXYandYXSmears = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/defaultRenderer/blendXYandYXSmears.fp"));

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
	 * Color filter shader
	 */
	public static ShaderProgram replaceColorShader() {
		ShaderProgram shaderProgram = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/replaceColor.fp"));
		return shaderProgram;
	}

	/**
	 * Color filter shader
	 */
	public static ShaderProgram replaceColorShader(Color replace, Color with) {
		ShaderProgram shaderProgram = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/replaceColor.fp"));
		shaderProgram.begin();
		shaderProgram.setUniformf("toReplace", replace);
		shaderProgram.setUniformf("color", with);
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
		defaultForeGroundTiles.setUniformf("debugSwitch", GaussianLightingRenderer.SEE_ALL ? 1f : 0f);
		defaultForeGroundTiles.end();
	}
}