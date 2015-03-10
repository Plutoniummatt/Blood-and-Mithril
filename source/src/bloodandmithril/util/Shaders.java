package bloodandmithril.util;

import bloodandmithril.core.Copyright;
import bloodandmithril.util.Logger.LogLevel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Helper class to create shaders
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Shaders {

	public static ShaderProgram pass = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/pass.fp"));
	public static ShaderProgram text = SpriteBatch.createDefaultShader();
	public static ShaderProgram filter = colorFilterShader();
	public static ShaderProgram filter2 = colorFilterShader();
	public static ShaderProgram replaceColor = replaceColorShader();
	public static ShaderProgram filterIgnoreReplace = filterReplaceIgnoreColorShader();

	public static ShaderProgram colorize = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/colorize.fp"));
	public static ShaderProgram invertY = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/defaultRenderer/invertYAxis.fp"));
	public static ShaderProgram backgroundShader = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/defaultRenderer/backgroundShader.fp"));
	public static ShaderProgram invertYBlendWithOcclusion = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/defaultRenderer/invertYBlendWithOcclusion.fp"));
	public static ShaderProgram foregroundShader = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/defaultRenderer/foregroundShader.fp"));
	public static ShaderProgram invertAlphaSolidColor = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/defaultRenderer/invertAlphaSolidColor.fp"));
	public static ShaderProgram invertAlphaSolidColorBlend = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/defaultRenderer/invertAlphaSolidColorBlend.fp"));
	public static ShaderProgram colorSmearLargeRadius = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/defaultRenderer/colorSmearLargeRadius.fp"));
	public static ShaderProgram colorSmearSmallRadius = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/defaultRenderer/colorSmearSmallRadius.fp"));
	public static ShaderProgram blendXYandYXSmears = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/defaultRenderer/blendXYandYXSmears.fp"));
	public static ShaderProgram tracerParticlesFBO = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/defaultRenderer/tracerParticleFbo.fp"));
	public static ShaderProgram lightingFBOBlend = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/defaultRenderer/lightingFboBlend.fp"));
	public static ShaderProgram sun = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/sun.fp"));
	public static ShaderProgram sky = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/sky.fp"));

	public static void setup() {
		ShaderProgram.pedantic = false;
		Logger.generalDebug("Shaders loaded", LogLevel.DEBUG);
		
		System.out.println(tracerParticlesFBO.getLog());
	}

	private static ShaderProgram filterReplaceIgnoreColorShader() {
		ShaderProgram shaderProgram = new ShaderProgram(Gdx.files.internal("data/shader/pass.vp"), Gdx.files.internal("data/shader/filterIgnoreReplace.fp"));
		return shaderProgram;
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
}