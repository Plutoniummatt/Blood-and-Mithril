package bloodandmithril.graphics;

import static bloodandmithril.BloodAndMithrilClient.HEIGHT;
import static bloodandmithril.BloodAndMithrilClient.WIDTH;
import static bloodandmithril.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.BloodAndMithrilClient.worldToScreenX;
import static bloodandmithril.BloodAndMithrilClient.worldToScreenY;
import static bloodandmithril.world.Domain.fBuffer;                          
import static bloodandmithril.world.Domain.mBuffer;                          
import static bloodandmithril.world.Domain.mBufferLit;                       
import static bloodandmithril.world.Domain.bBuffer;                          
import static bloodandmithril.world.Domain.bBufferProcessedForDaylightShader;
import static bloodandmithril.world.Domain.bBufferLit;                
import static bloodandmithril.world.WorldState.currentEpoch;
import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.Gdx.gl20;
import static com.badlogic.gdx.graphics.GL10.GL_TEXTURE0;
import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888;

import java.util.ArrayList;

import bloodandmithril.util.Shaders;
import bloodandmithril.world.Domain;
import bloodandmithril.world.weather.Weather;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
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
			if (light.x - light.size < camX + WIDTH/2 &&
				light.x + light.size > camX - WIDTH/2 &&
				light.y - light.size < camY + HEIGHT/2 &&
				light.y + light.size > camY - HEIGHT/2) {
				tempLights.add(light);
			}
		}

		for (Light light : tempLights) {
			
			if (light.fOcclusion == null) {
				light.fShadowMap = new FrameBuffer(RGBA8888, light.size, 1, true);
				light.mShadowMap = new FrameBuffer(RGBA8888, light.size, 1, true);
				light.fOcclusion = new FrameBuffer(RGBA8888, light.size, light.size, true);
				light.mOcclusion = new FrameBuffer(RGBA8888, light.size, light.size, true);
			}
			
			if (light.renderSwitch) {
				light.renderSwitch = !light.renderSwitch;
				continue;
			}

			//Draw foreground to occlusion map
			light.fOcclusion.begin();
			spriteBatch.begin();
			spriteBatch.setShader(Shaders.pass);
			gl20.glClear(GL_COLOR_BUFFER_BIT);
			spriteBatch.draw(
				fBuffer.getColorBufferTexture(),
				0f,
				0f,
				WIDTH,
				HEIGHT,
				(int)worldToScreenX(light.x) - light.size/2,
				(int)worldToScreenY(light.y) - light.size/2,
				light.size,
				light.size,
				false,
				false
			);
			spriteBatch.end();
			light.fOcclusion.end();

			light.mOcclusion.begin();
			spriteBatch.begin();
			spriteBatch.setShader(Shaders.pass);
			gl20.glClear(GL_COLOR_BUFFER_BIT);
			spriteBatch.draw(
				mBuffer.getColorBufferTexture(),
				0f,
				0f,
				WIDTH,
				HEIGHT,
				(int)worldToScreenX(light.x) - light.size/2,
				(int)worldToScreenY(light.y) - light.size/2,
				light.size,
				light.size,
				false,
				false
			);
			spriteBatch.draw(
				fBuffer.getColorBufferTexture(),
				0f,
				0f,
				WIDTH,
				HEIGHT,
				(int)worldToScreenX(light.x) - light.size/2,
				(int)worldToScreenY(light.y) - light.size/2,
				light.size,
				light.size,
				false,
				false
			);
			spriteBatch.end();
			light.mOcclusion.end();

			//Calculate 1D shadow map
			light.fShadowMap.begin();
			spriteBatch.begin();
			spriteBatch.setShader(Shaders.shadowMap);
			Shaders.shadowMap.setUniformf("resolution", light.fOcclusion.getWidth(), light.fOcclusion.getHeight());
			Shaders.shadowMap.setUniformf("span", light.spanBegin, light.spanEnd);
			spriteBatch.draw(light.fOcclusion.getColorBufferTexture(), 0f, 0f, WIDTH, HEIGHT, 0, 0, light.size, light.size, false, false);
			spriteBatch.end();
			light.fShadowMap.end();

			light.mShadowMap.begin();
			spriteBatch.begin();
			spriteBatch.setShader(Shaders.shadowMap);
			Shaders.shadowMap.setUniformf("resolution", light.mOcclusion.getWidth(), light.mOcclusion.getHeight());
			Shaders.shadowMap.setUniformf("span", light.spanBegin, light.spanEnd);
			spriteBatch.draw(light.mOcclusion.getColorBufferTexture(), 0f, 0f, WIDTH, HEIGHT, 0, 0, light.size, light.size, false, false);
			spriteBatch.end();
			light.mShadowMap.end();
			
			light.renderSwitch = !light.renderSwitch;
		}

		//Begin rendering----------------------------------//
		Weather.render();
		spriteBatch.begin();

		bBufferLit.begin();
		
		gl20.glClear(GL_COLOR_BUFFER_BIT);
		
		// Still render through the shader if no lights are present
		if (tempLights.isEmpty()) {
			spriteBatch.setShader(Shaders.defaultBackGroundTiles);
			bBufferProcessedForDaylightShader.getColorBufferTexture().bind(1);
			gl.glActiveTexture(GL_TEXTURE0);
			Shaders.defaultBackGroundTiles.setUniformi("u_texture2", 1);
			spriteBatch.draw(bBuffer.getColorBufferTexture(), 0, 0, WIDTH, HEIGHT, 0, 0, WIDTH, HEIGHT, false, true);
			spriteBatch.flush();
		}
		
		for (Light light : tempLights) {
			spriteBatch.setShader(Shaders.defaultBackGroundTiles);
			bBufferProcessedForDaylightShader.getColorBufferTexture().bind(1);
			gl.glActiveTexture(GL_TEXTURE0);
			Shaders.defaultBackGroundTiles.setUniformi("u_texture2", 1);
			Shaders.defaultBackGroundTiles.setUniformf("resolution", WIDTH, HEIGHT);
			Shaders.defaultBackGroundTiles.setUniformf("size", light.size);
			Shaders.defaultBackGroundTiles.setUniformf("color", light.color.r, light.color.g, light.color.b, light.color.a);
			Shaders.defaultBackGroundTiles.setUniformf("lightSource", (int)worldToScreenX(light.x), (int)worldToScreenY(light.y));
			spriteBatch.draw(bBuffer.getColorBufferTexture(), 0, 0, WIDTH, HEIGHT, 0, 0, WIDTH, HEIGHT, false, true);
			spriteBatch.flush();
		}
		bBufferLit.end();
		
		mBufferLit.begin();
		gl20.glClear(GL_COLOR_BUFFER_BIT);
		for (Light light : tempLights) {
			spriteBatch.setShader(Shaders.defaultBackGroundTiles);
			bBufferProcessedForDaylightShader.getColorBufferTexture().bind(1);
			gl.glActiveTexture(GL10.GL_TEXTURE0);
			Shaders.defaultBackGroundTiles.setUniformi("u_texture2", 1);
			Shaders.defaultBackGroundTiles.setUniformf("resolution", WIDTH, HEIGHT);
			Shaders.defaultBackGroundTiles.setUniformf("size", light.size);
			Shaders.defaultBackGroundTiles.setUniformf("color", light.color.r, light.color.g, light.color.b, light.color.a);
			Shaders.defaultBackGroundTiles.setUniformf("lightSource", (int)worldToScreenX(light.x), (int)worldToScreenY(light.y));
			spriteBatch.draw(mBuffer.getColorBufferTexture(), 0, 0, WIDTH, HEIGHT, 0, 0, WIDTH, HEIGHT, false, true);
			spriteBatch.flush();
		}
		mBufferLit.end();

		//Render the background
		spriteBatch.setShader(Shaders.black);
		Shaders.black.setUniformf("color", new Color(0f, 0f, 0f, 1f));
		spriteBatch.draw(bBuffer.getColorBufferTexture(), 0, 0, WIDTH, HEIGHT, 0, 0, WIDTH, HEIGHT, false, true);
		spriteBatch.setShader(Shaders.pass);
		spriteBatch.draw(bBufferLit.getColorBufferTexture(), 0, 0, WIDTH, HEIGHT, 0, 0, WIDTH, HEIGHT, false, true);

		//Render the light rays
		for (Light light: tempLights) {
			spriteBatch.setShader(Shaders.shadow);
			Shaders.shadow.setUniformf("resolution", light.fOcclusion.getWidth(), light.fOcclusion.getHeight());
			
			Shaders.shadow.setUniformf("color", light.color.r, light.color.g, light.color.b, 0.4f * light.color.a/20f);
			Shaders.shadow.setUniformf("intensity", light.intensity);
			spriteBatch.draw(light.mShadowMap.getColorBufferTexture(),  (int)worldToScreenX(light.x) - light.size/2,  (int)worldToScreenY(light.y) - light.size/2, light.size, light.size, 0, 0, light.size, 1, false, true);
			spriteBatch.flush();
			
			Shaders.shadow.setUniformf("color", light.color.r, light.color.g, light.color.b, 0.7f * light.color.a/20f);
			Shaders.shadow.setUniformf("intensity", light.intensity);
			spriteBatch.draw(light.fShadowMap.getColorBufferTexture(),  (int)worldToScreenX(light.x) - light.size/2,  (int)worldToScreenY(light.y) - light.size/2, light.size, light.size, 0, 0, light.size, 1, false, true);
			spriteBatch.flush();
		}

		//Render middleground without lighting
		if (SEE_ALL) {
			spriteBatch.setShader(Shaders.pass);
			spriteBatch.draw(mBuffer.getColorBufferTexture(), 0, 0, WIDTH, HEIGHT, 0, 0, WIDTH, HEIGHT, false, true);
		} else {
			spriteBatch.setShader(Shaders.black);
			float color = currentEpoch.dayLight() * 0.15f;
			Shaders.black.setUniformf("color", new Color(color, color, color, 1f));
			spriteBatch.draw(mBuffer.getColorBufferTexture(), 0, 0, WIDTH, HEIGHT, 0, 0, WIDTH, HEIGHT, false, true);
			
			spriteBatch.setShader(Shaders.pass);
			spriteBatch.draw(mBufferLit.getColorBufferTexture(), 0, 0, WIDTH, HEIGHT, 0, 0, WIDTH, HEIGHT, false, true);
		}

		//Render the middleground, affected by lighting
		for (Light light : tempLights) {
			spriteBatch.setShader(Shaders.defaultForeGroundTiles);
			light.mShadowMap.getColorBufferTexture().bind(1);
			gl.glActiveTexture(GL_TEXTURE0);
			Shaders.defaultForeGroundTiles.setUniformi("u_texture2", 1);
			Shaders.defaultForeGroundTiles.setUniformf("penetration", 0.10f);
			Shaders.defaultForeGroundTiles.setUniformf("color", light.color.r, light.color.g, light.color.b, light.color.a * 0.8f * light.intensity);
			spriteBatch.draw(light.mOcclusion.getColorBufferTexture(),  (int)worldToScreenX(light.x) - light.size/2,  (int)worldToScreenY(light.y) - light.size/2, light.size, light.size);
		}

		//Render foreground without lighting
		if (SEE_ALL) {
			spriteBatch.setShader(Shaders.pass);
			spriteBatch.draw(fBuffer.getColorBufferTexture(), 0, 0, WIDTH, HEIGHT, 0, 0, WIDTH, HEIGHT, false, true);
		} else {
			spriteBatch.setShader(Shaders.daylightShader);
			bBufferProcessedForDaylightShader.getColorBufferTexture().bind(1);
			gl.glActiveTexture(GL_TEXTURE0);
			Shaders.daylightShader.setUniformi("u_texture2", 1);
			spriteBatch.draw(fBuffer.getColorBufferTexture(), 0, 0, WIDTH, HEIGHT, 0, 0, WIDTH, HEIGHT, false, true);
		}

		//Render the foreground, affected by lighting
		for (Light light : tempLights) {
			spriteBatch.setShader(Shaders.defaultForeGroundTiles);
			light.fShadowMap.getColorBufferTexture().bind(1);
			gl.glActiveTexture(GL_TEXTURE0);
			Shaders.defaultForeGroundTiles.setUniformi("u_texture2", 1);
			Shaders.defaultForeGroundTiles.setUniformf("penetration", 0.07f);
			Shaders.defaultForeGroundTiles.setUniformf("color", light.color.r, light.color.g, light.color.b, light.color.a * light.intensity);
			spriteBatch.draw(light.fOcclusion.getColorBufferTexture(),  (int)worldToScreenX(light.x) - light.size/2,  (int)worldToScreenY(light.y) - light.size/2, light.size, light.size);
		}

		spriteBatch.end();
		//End rendering----------------------------------//
	}
}