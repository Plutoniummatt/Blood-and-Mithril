package bloodandmithril.world.weather;

import static bloodandmithril.core.BloodAndMithrilClient.HEIGHT;
import static bloodandmithril.core.BloodAndMithrilClient.WIDTH;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.world.WorldState.getCurrentEpoch;
import static com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888;
import static java.lang.Math.exp;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Epoch;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector2;

/**
 * Weather class, renderable, changes with {@link Epoch}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Weather {

	private static Color dayTopColor 					= new Color(33f/150f, 169f/255f, 255f/255f, 1f);
	private static Color dayBottomColor 				= new Color(0f, 144f/255f, 1f, 1f);
	private static Color nightTopColor 					= new Color(33f/255f, 0f, 150f/255f, 1f);
	private static Color nightBottomColor 				= new Color(60f/255f, 0f, 152f/255f, 1f);

	private static FrameBuffer skyBuffer				= new FrameBuffer(RGBA8888, WIDTH, HEIGHT, false);
	private static FrameBuffer working					= new FrameBuffer(RGBA8888, 1, 1, false);

	private static Vector2 sunPosition					= new Vector2();

	/** Load resources */
	public static void setup() {
	}


	/**
	 * Renders the {@link Weather}
	 */
	public static void render() {
		renderSky();
		renderSun();
	}


	private static void renderSun() {
		float time = getCurrentEpoch().getTime();
		Vector2 pivot = new Vector2(WIDTH/2, 0);
		float radius = WIDTH/2;
		Vector2 position = pivot.cpy().add(new Vector2(0f, radius).rotate(-((time - 12f) / 12f) * 180f));

		sunPosition.x = position.x;
		sunPosition.y = position.y;
	}


	public static Color getDaylightColor() {
		float time = getCurrentEpoch().getTime();
		Color filter = new Color();

		if (time < 10) {
			filter.r = (float) (0.06D + 1.1D * exp(-0.100*pow(time-10, 2)));
			filter.g = (float) (0.06D + 0.9D * exp(-0.150*pow(time-10, 2)));
			filter.b = (float) (0.06D + 0.7D * exp(-0.200*pow(time-10, 2)));
		} else if (time >= 10 && time < 14) {
			filter.r = 1.16f;
			filter.g = 0.96f;
			filter.b = 0.76f;
		} else {
			filter.r = (float) (0.06D + 1.1D * exp(-0.100*pow(time-14, 2)));
			filter.g = (float) (0.06D + 0.9D * exp(-0.150*pow(time-14, 2)));
			filter.b = (float) (0.06D + 0.7D * exp(-0.200*pow(time-14, 2)));
		}
		filter.a = 1f;

		return filter;
	}


	public static Color getSunColor() {
		float time = getCurrentEpoch().getTime();
		Color filter = new Color();

		if (time < 10) {
			filter.r = (float) (0.5D + 0.5D * exp(-0.050*pow(time-10, 2)));
			filter.g = (float) (0.2D + 0.8D * exp(-0.150*pow(time-10, 2)));
			filter.b = (float) (0.1D + 0.9D * exp(-0.200*pow(time-10, 2)));
		} else if (time >= 10 && time < 14) {
			filter.r = 1.1f;
			filter.g = 1.1f;
			filter.b = 1.1f;
		} else {
			filter.r = (float) (0.5D + 0.5D * exp(-0.050*pow(time-14, 2)));
			filter.g = (float) (0.2D + 0.8D * exp(-0.150*pow(time-14, 2)));
			filter.b = (float) (0.1D + 0.9D * exp(-0.200*pow(time-14, 2)));
		}
		filter.a = 1f;

		return filter;
	}


	/** Renders the sky */
	private static void renderSky() {
		skyBuffer.begin();
		spriteBatch.begin();
		spriteBatch.setShader(Shaders.sky);
		Color filter = getDaylightColor();

		Color topColor = dayTopColor.cpy().mul(getCurrentEpoch().dayLight()).add(nightTopColor.cpy().mul(1f - getCurrentEpoch().dayLight())).mul(filter);
		Color bottomColor = dayBottomColor.cpy().mul(getCurrentEpoch().dayLight()).add(nightBottomColor.cpy().mul(1f - getCurrentEpoch().dayLight())).mul(filter);

		Shaders.sky.setUniformf("top", topColor);
		Shaders.sky.setUniformf("bottom", bottomColor);
		
		spriteBatch.draw(working.getColorBufferTexture(), 0, 0, WIDTH, HEIGHT);
		spriteBatch.end();
		skyBuffer.end();

		float time = getCurrentEpoch().getTime();

		spriteBatch.begin();
		spriteBatch.setShader(Shaders.sun);
		Shaders.sun.setUniformf("resolution", WIDTH, HEIGHT);
		Shaders.sun.setUniformf("sunPosition", sunPosition);
		Shaders.sun.setUniformf("filter", Colors.modulateAlpha(getSunColor(), glareAlpha(time)));
		Shaders.sun.setUniformf("epoch", getCurrentEpoch().getTime());
		Shaders.sun.setUniformf("nightSuppression", nightSuppression(time));
		spriteBatch.draw(skyBuffer.getColorBufferTexture(), 0, 0);
		spriteBatch.end();
	}


	private static float glareAlpha(float time) {
		if (time <= 4f || time >= 20f) {
			return 1f;
		}
		return 1f - (float) pow(sin((time - 4f) * (Math.PI / 16f)), 2f);
	}


	private static float nightSuppression(float time) {
		if (time >= 4f || time <= 20f) {
			return 1f;
		}
		return 1f - (float) pow(sin((time + 4f) * (Math.PI / 8f)), 2f);
	}
}
