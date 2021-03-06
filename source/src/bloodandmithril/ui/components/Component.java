package bloodandmithril.ui.components;

import static bloodandmithril.ui.UserInterface.uiTexture;
import static bloodandmithril.util.Logger.generalDebug;
import static bloodandmithril.util.Logger.LogLevel.DEBUG;
import static bloodandmithril.util.Util.Colors.modulateAlpha;
import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.graphics.GL20.GL_BLEND;
import static com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA;
import static com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA;

import java.util.Deque;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.google.inject.Inject;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.window.Window;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Util.Colors;

/**
 * A {@link UserInterface} Component.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class Component {
	
	@Inject private UserInterface userInterface;

	protected static final TextureRegion TOP_LEFT 		= new TextureRegion(uiTexture, 0, 8, 2, 2);
	protected static final TextureRegion TOP_RIGHT 		= new TextureRegion(uiTexture, 12, 8, 2, 2);
	protected static final TextureRegion BOTTOM_LEFT 	= new TextureRegion(uiTexture, 0, 30, 2, 2);
	protected static final TextureRegion BOTTOM_RIGHT	= new TextureRegion(uiTexture, 12, 30, 2, 2);

	protected static final TextureRegion TOP			= new TextureRegion(uiTexture, 2, 8, 10, 2);
	protected static final TextureRegion BOTTOM			= new TextureRegion(uiTexture, 2, 30, 10, 2);
	protected static final TextureRegion LEFT			= new TextureRegion(uiTexture, 0, 10, 2, 20);
	protected static final TextureRegion RIGHT			= new TextureRegion(uiTexture, 12, 10, 2, 20);

	protected static final TextureRegion MINIMIZE		= new TextureRegion(uiTexture, 15, 0, 12, 12);
	protected static final TextureRegion CLOSE			= new TextureRegion(uiTexture, 29, 0, 12, 12);
	protected static final TextureRegion SEPARATOR_BODY	= new TextureRegion(uiTexture, 1, 32, 10, 2);
	protected static final TextureRegion SEPARATOR_END	= new TextureRegion(uiTexture, 0, 32, 1, 1);
	protected static final TextureRegion RESIZE			= new TextureRegion(uiTexture, 41, 0, 12, 12);

	/** Utility {@link ShapeRenderer} for {@link Component}s */
	protected static ShapeRenderer shapeRenderer = new ShapeRenderer();

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
	protected abstract void internalComponentRender(Graphics graphics);


	/**
	 * Protected constructor, injects dependencies
	 */
	protected Component() {
		Wiring.injector().injectMembers(this);
	}


	/**
	 * Called when the scroll wheel is scrolled.
	 */
	public boolean scrolled(int amount) {
		return false;
	}


	/**
	 * Renders this {@link Component}
	 */
	public void render(Graphics graphics) {
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
		internalComponentRender(graphics);
	}


	/**
	 * Initiates resources
	 */
	public static void setup() {
		generalDebug("UI Elements loaded", DEBUG);
	}


	/**
	 * Renders the text box for this {@link Component}
	 */
	protected void renderBox(int x, int y, int length, int height, boolean active, Color borderColor, Graphics graphics) {
		SpriteBatch batch = graphics.getSpriteBatch();

		Shaders.filter.begin();
		Shaders.filter.setUniformf("color", borderColor.r, borderColor.g, borderColor.b, active ? borderColor.a * getAlpha() : borderColor.a * 0.4f * getAlpha());
		Shaders.filter.end();
		batch.setShader(Shaders.filter);

		batch.draw(TOP_LEFT, x, y);
		batch.draw(TOP_RIGHT, x + TOP_LEFT.getRegionWidth() + length, y);
		batch.draw(BOTTOM_LEFT, x, y - height - BOTTOM.getRegionHeight());
		batch.draw(BOTTOM_RIGHT, x + TOP_LEFT.getRegionWidth() + length, y - height - BOTTOM.getRegionHeight());


		batch.draw(
			TOP,
			x  + TOP_LEFT.getRegionWidth(),
			y,
			length,
			TOP.getRegionHeight()
		);

		batch.draw(
			BOTTOM,
			x + TOP_LEFT.getRegionWidth(),
			y - height - BOTTOM_LEFT.getRegionHeight(),
			length,
			BOTTOM.getRegionHeight()
		);

		batch.draw(
			LEFT,
			x,
			y - height,
			LEFT.getRegionWidth(),
			height
		);

		batch.draw(
			RIGHT,
			x + TOP_LEFT.getRegionWidth() + length,
			y - height,
			RIGHT.getRegionWidth(),
			height
		);
	}


	/**
	 * Renders a rectangle
	 */
	protected void renderRectangle(int renderX, int renderY, int length, int height, boolean active, Color backGroundColor, Graphics graphics) {
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(1f, 0f, 0f, 0.5f * getAlpha());
		shapeRenderer.setProjectionMatrix(userInterface.getUICamera().combined);
		gl.glEnable(GL_BLEND);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		float a = active ? 0.7f : 0.3f;
		Color color = modulateAlpha(backGroundColor, a * getAlpha());

		shapeRenderer.rect(
			renderX,
			renderY - height - BOTTOM_LEFT.getRegionHeight(),
			length,
			height,
			color,
			color,
			color,
			color
		);

		shapeRenderer.flush();
		shapeRenderer.end();

		gl.glDisable(GL_BLEND);
	}


	/**
	 * Renders a rectangle
	 */
	protected void renderRectangle(int renderX, int renderY, int length, int height, boolean active, Color backGroundColor, float alphaOverride, Graphics graphics) {
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(1f, 0f, 0f, 0.5f * alphaOverride);
		shapeRenderer.setProjectionMatrix(userInterface.getUICamera().combined);
		gl.glEnable(GL_BLEND);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		float a = active ? 0.7f : 0.3f;
		Color color = Colors.modulateAlpha(backGroundColor, a * alphaOverride);

		shapeRenderer.rect(
			renderX,
			renderY - height - BOTTOM_LEFT.getRegionHeight(),
			length,
			height,
			color,
			color,
			color,
			color
		);

		shapeRenderer.flush();
		shapeRenderer.end();

		gl.glDisable(GL_BLEND);
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