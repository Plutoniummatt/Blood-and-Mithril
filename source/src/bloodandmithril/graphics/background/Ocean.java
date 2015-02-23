package bloodandmithril.graphics.background;

import bloodandmithril.graphics.background.BackgroundImages.Background;
import bloodandmithril.util.Shaders;
import bloodandmithril.world.weather.Weather;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Ocean extends Background {

	@Override
	public TextureRegion getTextureRegion() {
		return BackgroundImages.repeatingOcean;
	}

	
	@Override
	public float getDistanceX() {
		return 1f;
	}


	@Override
	public float getDistanceY() {
		return 0.85f;
	}


	@Override
	public float getOffsetY() {
		return -100;
	}


	@Override
	public void preRender(SpriteBatch spriteBatch) {
		spriteBatch.setShader(Shaders.filter);
		float r = Weather.getDaylightColor().r;
		Shaders.filter.setUniformf("color", Weather.getSunColor().mul(new Color(r, r, r, 1f)));
	}
}