package bloodandmithril.ui.components;

import java.util.Deque;
import java.util.List;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.window.Window;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Util.Colors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

/**
 * A {@link UserInterface} Component.
 *
 * @author Matt
 */
public abstract class Component {

	protected static final TextureRegion topLeft 		= new TextureRegion(UserInterface.uiTexture, 0, 8, 2, 2);
	protected static final TextureRegion topRight 		= new TextureRegion(UserInterface.uiTexture, 12, 8, 2, 2);
	protected static final TextureRegion bottomLeft 	= new TextureRegion(UserInterface.uiTexture, 0, 30, 2, 2);
	protected static final TextureRegion bottomRight	= new TextureRegion(UserInterface.uiTexture, 12, 30, 2, 2);

	protected static final TextureRegion top			= new TextureRegion(UserInterface.uiTexture, 2, 8, 10, 2);
	protected static final TextureRegion bottom			= new TextureRegion(UserInterface.uiTexture, 2, 30, 10, 2);
	protected static final TextureRegion left			= new TextureRegion(UserInterface.uiTexture, 0, 10, 2, 20);
	protected static final TextureRegion right			= new TextureRegion(UserInterface.uiTexture, 12, 10, 2, 20);

	protected static final TextureRegion minimize		= new TextureRegion(UserInterface.uiTexture, 15, 0, 12, 12);
	protected static final TextureRegion close			= new TextureRegion(UserInterface.uiTexture, 29, 0, 12, 12);
	protected static final TextureRegion separatorBody	= new TextureRegion(UserInterface.uiTexture, 1, 32, 10, 2);
	protected static final TextureRegion separatorEnd	= new TextureRegion(UserInterface.uiTexture, 0, 32, 1, 1);
	protected static final TextureRegion resize			= new TextureRegion(UserInterface.uiTexture, 41, 0, 12, 12);

	/** Utility {@link ShapeRenderer} for {@link Component}s */
	protected static ShapeRenderer shapeRenderer 		= new ShapeRenderer();

	/** The alpha(transparent) value this {@link Component} should be rendered with */
	private float alpha = 0f;

	/** True if this is the active {@link Component} */
	private boolean active;

	/** True if this {@link Component} is currently fading out and will soon be closed */
	private boolean closing;

	/** Called when a key is pressed */
	public abstract boolean keyPressed(int keyCode);

	/** Called when left clicked */
	public abstract boolean leftClick(List<ContextMenu> copy, Deque<Component> windowsCopy);

	/** left click released method */
	public abstract void leftClickReleased();

	/** Component specific render */
	protected abstract void internalComponentRender();

	/**
	 * Renders this {@link Component}
	 */
	public void render() {
		if (isClosing()) {
			setAlpha(getAlpha() - 0.08f > 0f ? getAlpha() - 0.08f : 0f);
		} else if (this instanceof Window) {
			if (!((Window) this).minimized) {
				setAlpha(getAlpha() + 0.08f >= 1f ? 1f : getAlpha() + 0.08f);
			} else {
				setAlpha(getAlpha() - 0.08f > 0f ? getAlpha() - 0.08f : 0f);
			}
		} else {
			setAlpha(getAlpha() + 0.08f >= 1f ? 1f : getAlpha() + 0.08f);
		}
		internalComponentRender();
	}


	/**
	 * Initiates resources
	 */
	public static void setup() {
		Logger.generalDebug("UI Elements loaded", LogLevel.DEBUG);
	}


	/**
	 * Renders the text box for this {@link Component}
	 */
	protected void renderBox(int x, int y, int length, int height, boolean active, Color borderColor) {
		Shaders.filter.begin();
		Shaders.filter.setUniformf("color", borderColor.r, borderColor.g, borderColor.b, active ? borderColor.a * getAlpha() : borderColor.a * 0.4f * getAlpha());
		Shaders.filter.end();
		BloodAndMithrilClient.spriteBatch.setShader(Shaders.filter);

		BloodAndMithrilClient.spriteBatch.draw(topLeft, x, y);
		BloodAndMithrilClient.spriteBatch.draw(topRight, x + topLeft.getRegionWidth() + length, y);
		BloodAndMithrilClient.spriteBatch.draw(bottomLeft, x, y - height - bottom.getRegionHeight());
		BloodAndMithrilClient.spriteBatch.draw(bottomRight, x + topLeft.getRegionWidth() + length, y - height - bottom.getRegionHeight());


		BloodAndMithrilClient.spriteBatch.draw(
			top,
			x  + topLeft.getRegionWidth(),
			y,
			length,
			top.getRegionHeight()
		);

		BloodAndMithrilClient.spriteBatch.draw(
			bottom,
			x + topLeft.getRegionWidth(),
			y - height - bottomLeft.getRegionHeight(),
			length,
			bottom.getRegionHeight()
		);

		BloodAndMithrilClient.spriteBatch.draw(
			left,
			x,
			y - height,
			left.getRegionWidth(),
			height
		);

		BloodAndMithrilClient.spriteBatch.draw(
			right,
			x + topLeft.getRegionWidth() + length,
			y - height,
			right.getRegionWidth(),
			height
		);
	}


	/**
	 * Renders a rectangle
	 */
	protected void renderRectangle(int renderX, int renderY, int length, int height, boolean active, Color backGroundColor) {
		shapeRenderer.begin(ShapeType.FilledRectangle);
		shapeRenderer.setColor(1f, 0f, 0f, 0.5f * getAlpha());
		shapeRenderer.setProjectionMatrix(UserInterface.UICamera.combined);
		Gdx.gl.glEnable(GL10.GL_BLEND);
		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		float a = active ? 0.7f : 0.3f;
		Color color = Colors.modulateAlpha(backGroundColor, a * getAlpha());

		shapeRenderer.filledRect(
			renderX,
			renderY - height - bottomLeft.getRegionHeight(),
			length,
			height,
			color,
			color,
			color,
			color
		);

		shapeRenderer.flush();
		shapeRenderer.end();

		Gdx.gl.glDisable(GL10.GL_BLEND);
	}


	/**
	 * Renders a rectangle
	 */
	protected void renderRectangle(int renderX, int renderY, int length, int height, boolean active, Color backGroundColor, float alphaOverride) {
		shapeRenderer.begin(ShapeType.FilledRectangle);
		shapeRenderer.setColor(1f, 0f, 0f, 0.5f * alphaOverride);
		shapeRenderer.setProjectionMatrix(UserInterface.UICamera.combined);
		Gdx.gl.glEnable(GL10.GL_BLEND);
		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		float a = active ? 0.7f : 0.3f;
		Color color = Colors.modulateAlpha(backGroundColor, a * alphaOverride);

		shapeRenderer.filledRect(
			renderX,
			renderY - height - bottomLeft.getRegionHeight(),
			length,
			height,
			color,
			color,
			color,
			color
		);

		shapeRenderer.flush();
		shapeRenderer.end();

		Gdx.gl.glDisable(GL10.GL_BLEND);
	}


	public boolean isActive() {
		return active;
	}


	public void setActive(boolean active) {
		this.active = active;
	}


	public boolean isClosing() {
		return closing;
	}


	public void setClosing(boolean closing) {
		this.closing = closing;
	}


	public float getAlpha() {
		return alpha;
	}


	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}
}