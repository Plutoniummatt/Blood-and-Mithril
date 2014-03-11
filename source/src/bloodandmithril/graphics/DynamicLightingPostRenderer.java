package bloodandmithril.graphics;

import static bloodandmithril.world.Domain.fBuffer;                          
import static bloodandmithril.world.Domain.mBuffer;                          
import static bloodandmithril.world.Domain.mBufferLit;                       
import static bloodandmithril.world.Domain.bBuffer;                          
import static bloodandmithril.world.Domain.bBufferProcessedForDaylightShader;
import static bloodandmithril.world.Domain.bBufferLit;                

import java.util.ArrayList;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.util.Shaders;
import bloodandmithril.world.Domain;
import bloodandmithril.world.WorldState;
import bloodandmithril.world.weather.Weather;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

/**
 * Class to encapsulate post-rendering with dynamic lighting shaders.
 *
 * @author Matt
 */
public class DynamicLightingPostRenderer {
	public static boolean SEE_ALL = false;

	public static void render(float camX, float camY) {
		ArrayList<Light> tempLights = new ArrayList<Light>();

		//Do not bother with lights that are off screen
		for (Light light : Domain.getLights().values()) {
			if (light.x - light.size < camX + BloodAndMithrilClient.WIDTH/2 &&
				light.x + light.size > camX - BloodAndMithrilClient.WIDTH/2 &&
				light.y - light.size < camY + BloodAndMithrilClient.HEIGHT/2 &&
				light.y + light.size > camY - BloodAndMithrilClient.HEIGHT/2) {
				tempLights.add(light);
			}
		}

		for (Light light : tempLights) {
			
			if (light.fOcclusion == null) {
				light.fShadowMap = new FrameBuffer(Format.RGBA8888, light.size, 1, true);
				light.mShadowMap = new FrameBuffer(Format.RGBA8888, light.size, 1, true);
				light.fOcclusion = new FrameBuffer(Format.RGBA8888, light.size, light.size, true);
				light.mOcclusion = new FrameBuffer(Format.RGBA8888, light.size, light.size, true);
			}
			
			if (light.renderSwitch) {
				light.renderSwitch = !light.renderSwitch;
				continue;
			}

			//Draw foreground to occlusion map
			light.fOcclusion.begin();
			BloodAndMithrilClient.spriteBatch.begin();
			BloodAndMithrilClient.spriteBatch.setShader(Shaders.pass);
			Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
			BloodAndMithrilClient.spriteBatch.draw(
				fBuffer.getColorBufferTexture(),
				0f,
				0f,
				BloodAndMithrilClient.WIDTH,
				BloodAndMithrilClient.HEIGHT,
				(int)BloodAndMithrilClient.worldToScreenX(light.x) - light.size/2,
				(int)BloodAndMithrilClient.worldToScreenY(light.y) - light.size/2,
				light.size,
				light.size,
				false,
				false
			);
			BloodAndMithrilClient.spriteBatch.end();
			light.fOcclusion.end();

			light.mOcclusion.begin();
			BloodAndMithrilClient.spriteBatch.begin();
			BloodAndMithrilClient.spriteBatch.setShader(Shaders.pass);
			Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
			BloodAndMithrilClient.spriteBatch.draw(
				mBuffer.getColorBufferTexture(),
				0f,
				0f,
				BloodAndMithrilClient.WIDTH,
				BloodAndMithrilClient.HEIGHT,
				(int)BloodAndMithrilClient.worldToScreenX(light.x) - light.size/2,
				(int)BloodAndMithrilClient.worldToScreenY(light.y) - light.size/2,
				light.size,
				light.size,
				false,
				false
			);
			BloodAndMithrilClient.spriteBatch.draw(
				fBuffer.getColorBufferTexture(),
				0f,
				0f,
				BloodAndMithrilClient.WIDTH,
				BloodAndMithrilClient.HEIGHT,
				(int)BloodAndMithrilClient.worldToScreenX(light.x) - light.size/2,
				(int)BloodAndMithrilClient.worldToScreenY(light.y) - light.size/2,
				light.size,
				light.size,
				false,
				false
			);
			BloodAndMithrilClient.spriteBatch.end();
			light.mOcclusion.end();

			//Calculate 1D shadow map
			light.fShadowMap.begin();
			BloodAndMithrilClient.spriteBatch.begin();
			BloodAndMithrilClient.spriteBatch.setShader(Shaders.shadowMap);
			Shaders.shadowMap.setUniformf("resolution", light.fOcclusion.getWidth(), light.fOcclusion.getHeight());
			Shaders.shadowMap.setUniformf("span", light.spanBegin, light.spanEnd);
			BloodAndMithrilClient.spriteBatch.draw(light.fOcclusion.getColorBufferTexture(), 0f, 0f, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, 0, 0, light.size, light.size, false, false);
			BloodAndMithrilClient.spriteBatch.end();
			light.fShadowMap.end();

			light.mShadowMap.begin();
			BloodAndMithrilClient.spriteBatch.begin();
			BloodAndMithrilClient.spriteBatch.setShader(Shaders.shadowMap);
			Shaders.shadowMap.setUniformf("resolution", light.mOcclusion.getWidth(), light.mOcclusion.getHeight());
			Shaders.shadowMap.setUniformf("span", light.spanBegin, light.spanEnd);
			BloodAndMithrilClient.spriteBatch.draw(light.mOcclusion.getColorBufferTexture(), 0f, 0f, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, 0, 0, light.size, light.size, false, false);
			BloodAndMithrilClient.spriteBatch.end();
			light.mShadowMap.end();
			
			light.renderSwitch = !light.renderSwitch;
		}

		//Begin rendering----------------------------------//
		Weather.render();
		BloodAndMithrilClient.spriteBatch.begin();

		bBufferLit.begin();
		
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		// Still render through the shader if no lights are present
		if (tempLights.isEmpty()) {
			BloodAndMithrilClient.spriteBatch.setShader(Shaders.defaultBackGroundTiles);
			bBufferProcessedForDaylightShader.getColorBufferTexture().bind(1);
			Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0);
			Shaders.defaultBackGroundTiles.setUniformi("u_texture2", 1);
			BloodAndMithrilClient.spriteBatch.draw(bBuffer.getColorBufferTexture(), 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, false, true);
			BloodAndMithrilClient.spriteBatch.flush();
		}
		
		for (Light light : tempLights) {
			BloodAndMithrilClient.spriteBatch.setShader(Shaders.defaultBackGroundTiles);
			bBufferProcessedForDaylightShader.getColorBufferTexture().bind(1);
			Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0);
			Shaders.defaultBackGroundTiles.setUniformi("u_texture2", 1);
			Shaders.defaultBackGroundTiles.setUniformf("resolution", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			Shaders.defaultBackGroundTiles.setUniformf("size", light.size);
			Shaders.defaultBackGroundTiles.setUniformf("color", light.color.r, light.color.g, light.color.b, light.color.a);
			Shaders.defaultBackGroundTiles.setUniformf("lightSource", (int)BloodAndMithrilClient.worldToScreenX(light.x), (int)BloodAndMithrilClient.worldToScreenY(light.y));
			BloodAndMithrilClient.spriteBatch.draw(bBuffer.getColorBufferTexture(), 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, false, true);
			BloodAndMithrilClient.spriteBatch.flush();
		}
		bBufferLit.end();
		
		mBufferLit.begin();
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		for (Light light : tempLights) {
			BloodAndMithrilClient.spriteBatch.setShader(Shaders.defaultBackGroundTiles);
			bBufferProcessedForDaylightShader.getColorBufferTexture().bind(1);
			Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0);
			Shaders.defaultBackGroundTiles.setUniformi("u_texture2", 1);
			Shaders.defaultBackGroundTiles.setUniformf("resolution", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			Shaders.defaultBackGroundTiles.setUniformf("size", light.size);
			Shaders.defaultBackGroundTiles.setUniformf("color", light.color.r, light.color.g, light.color.b, light.color.a);
			Shaders.defaultBackGroundTiles.setUniformf("lightSource", (int)BloodAndMithrilClient.worldToScreenX(light.x), (int)BloodAndMithrilClient.worldToScreenY(light.y));
			BloodAndMithrilClient.spriteBatch.draw(mBuffer.getColorBufferTexture(), 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, false, true);
			BloodAndMithrilClient.spriteBatch.flush();
		}
		mBufferLit.end();

		//Render the background
		BloodAndMithrilClient.spriteBatch.setShader(Shaders.black);
		Shaders.black.setUniformf("color", new Color(0f, 0f, 0f, 1f));
		BloodAndMithrilClient.spriteBatch.draw(bBuffer.getColorBufferTexture(), 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, false, true);
		BloodAndMithrilClient.spriteBatch.setShader(Shaders.pass);
		BloodAndMithrilClient.spriteBatch.draw(bBufferLit.getColorBufferTexture(), 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, false, true);

		//Render the light rays
		for (Light light: tempLights) {
			BloodAndMithrilClient.spriteBatch.setShader(Shaders.shadow);
			Shaders.shadow.setUniformf("resolution", light.fOcclusion.getWidth(), light.fOcclusion.getHeight());
			
			Shaders.shadow.setUniformf("color", light.color.r, light.color.g, light.color.b, 0.4f * light.color.a/20f);
			Shaders.shadow.setUniformf("intensity", light.intensity);
			BloodAndMithrilClient.spriteBatch.draw(light.mShadowMap.getColorBufferTexture(),  (int)BloodAndMithrilClient.worldToScreenX(light.x) - light.size/2,  (int)BloodAndMithrilClient.worldToScreenY(light.y) - light.size/2, light.size, light.size, 0, 0, light.size, 1, false, true);
			BloodAndMithrilClient.spriteBatch.flush();
			
			Shaders.shadow.setUniformf("color", light.color.r, light.color.g, light.color.b, 0.7f * light.color.a/20f);
			Shaders.shadow.setUniformf("intensity", light.intensity);
			BloodAndMithrilClient.spriteBatch.draw(light.fShadowMap.getColorBufferTexture(),  (int)BloodAndMithrilClient.worldToScreenX(light.x) - light.size/2,  (int)BloodAndMithrilClient.worldToScreenY(light.y) - light.size/2, light.size, light.size, 0, 0, light.size, 1, false, true);
			BloodAndMithrilClient.spriteBatch.flush();
		}

		//Render middleground without lighting
		if (SEE_ALL) {
			BloodAndMithrilClient.spriteBatch.setShader(Shaders.pass);
			BloodAndMithrilClient.spriteBatch.draw(mBuffer.getColorBufferTexture(), 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, false, true);
		} else {
			BloodAndMithrilClient.spriteBatch.setShader(Shaders.black);
			float color = WorldState.currentEpoch.dayLight() * 0.15f;
			Shaders.black.setUniformf("color", new Color(color, color, color, 1f));
			BloodAndMithrilClient.spriteBatch.draw(mBuffer.getColorBufferTexture(), 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, false, true);
			
			BloodAndMithrilClient.spriteBatch.setShader(Shaders.pass);
			BloodAndMithrilClient.spriteBatch.draw(mBufferLit.getColorBufferTexture(), 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, false, true);
		}

		//Render the middleground, affected by lighting
		for (Light light : tempLights) {
			BloodAndMithrilClient.spriteBatch.setShader(Shaders.defaultForeGroundTiles);
			light.mShadowMap.getColorBufferTexture().bind(1);
			Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0);
			Shaders.defaultForeGroundTiles.setUniformi("u_texture2", 1);
			Shaders.defaultForeGroundTiles.setUniformf("penetration", 0.10f);
			Shaders.defaultForeGroundTiles.setUniformf("color", light.color.r, light.color.g, light.color.b, light.color.a * 0.8f * light.intensity);
			BloodAndMithrilClient.spriteBatch.draw(light.mOcclusion.getColorBufferTexture(),  (int)BloodAndMithrilClient.worldToScreenX(light.x) - light.size/2,  (int)BloodAndMithrilClient.worldToScreenY(light.y) - light.size/2, light.size, light.size);
		}

		//Render foreground without lighting
		if (SEE_ALL) {
			BloodAndMithrilClient.spriteBatch.setShader(Shaders.pass);
			BloodAndMithrilClient.spriteBatch.draw(fBuffer.getColorBufferTexture(), 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, false, true);
		} else {
			BloodAndMithrilClient.spriteBatch.setShader(Shaders.daylightShader);
			bBufferProcessedForDaylightShader.getColorBufferTexture().bind(1);
			Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0);
			Shaders.daylightShader.setUniformi("u_texture2", 1);
			BloodAndMithrilClient.spriteBatch.draw(fBuffer.getColorBufferTexture(), 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, 0, 0, BloodAndMithrilClient.WIDTH, BloodAndMithrilClient.HEIGHT, false, true);
		}

		//Render the foreground, affected by lighting
		for (Light light : tempLights) {
			BloodAndMithrilClient.spriteBatch.setShader(Shaders.defaultForeGroundTiles);
			light.fShadowMap.getColorBufferTexture().bind(1);
			Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0);
			Shaders.defaultForeGroundTiles.setUniformi("u_texture2", 1);
			Shaders.defaultForeGroundTiles.setUniformf("penetration", 0.07f);
			Shaders.defaultForeGroundTiles.setUniformf("color", light.color.r, light.color.g, light.color.b, light.color.a * light.intensity);
			BloodAndMithrilClient.spriteBatch.draw(light.fOcclusion.getColorBufferTexture(),  (int)BloodAndMithrilClient.worldToScreenX(light.x) - light.size/2,  (int)BloodAndMithrilClient.worldToScreenY(light.y) - light.size/2, light.size, light.size);
		}

		BloodAndMithrilClient.spriteBatch.end();
		//End rendering----------------------------------//
	}
}