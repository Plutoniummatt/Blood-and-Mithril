package bloodandmithril.ui.components.window;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.control.InputUtilities.isButtonPressed;
import static bloodandmithril.graphics.Graphics.getGdxHeight;
import static bloodandmithril.graphics.Graphics.getGdxWidth;
import static bloodandmithril.util.Fonts.defaultFont;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.google.inject.Inject;

import bloodandmithril.control.Controls;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.util.Shaders;

/**
 * A window, able to be dragged, minimized, stays open until closed (unless game decides it should close).
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class Window extends Component {

	@Inject private UserInterface userInterface;

	/** Colors of this window */
	public Color borderColor, backGroundColor;

	/** Position of this window */
	public int x, y, width, height, oldLength, oldHeight, mx, my, oldX, oldY, minLength, minHeight;

	/** Whether or not this {@link Window} is currently being resized */
	private boolean resizing = false;

	/** Whether or not this {@link Window} is currently being positioned */
	private boolean positioning = false;

	/** Whether or not this {@link Window} can be minimized/closed */
	public final boolean minimizable, closeable;

	/** True if this window is always active */
	private boolean alwaysActive;

	/** True if minimized */
	public boolean minimized = false;

	/** Title of this {@link Window} */
	public String title;

	/** Buttons for closing and minimizing this {@link Window} */
	private Button closeButton, minimizeButton, resizeButton;

	/** Whether or not this {@link Window} can be resized */
	private boolean resizeable;

	/**
	 * Constructor
	 */
	public Window(final int x, final int y, final int length, final int height, final Color borderColor, final Color backGroundColor, final String title, final boolean active, final int minLength, final int minHeight, final boolean minimizable, final boolean resizeable, final boolean closeable) {
		this.x = x;
		this.y = y;
		this.width = length;
		this.height = height;
		this.borderColor = borderColor;
		this.backGroundColor = backGroundColor;
		this.title = title;
		this.resizeable = resizeable;
		this.closeable = closeable;
		this.setActive(active);
		this.minLength = minLength;
		this.minHeight = minHeight;
		this.minimizable = minimizable;

		loadButtons();
		Wiring.injector().injectMembers(this);
	}


	/**
	 * Overloaded contructor, uses default colors
	 */
	public Window(final int length, final int height, final String title, final boolean active, final int minLength, final int minHeight, final boolean minimizable, final boolean resizeable, final boolean closeable) {
		this((getGdxWidth() - length) / 2, (getGdxHeight() + height) / 2, length, height, Color.GRAY, Color.BLACK, title, active, minLength, minHeight, minimizable, resizeable, closeable);
	}


	/**
	 * Overloaded contructor, uses default colors
	 */
	public Window(final int x, final int y, final int length, final int height, final String title, final boolean active, final int minLength, final int minHeight, final boolean minimizable, final boolean resizeable, final boolean closeable) {
		this(x, y, length, height, Color.GRAY, Color.BLACK, title, active, minLength, minHeight, minimizable, resizeable, closeable);
	}


	/**
	 * Overloaded contructor, uses default colors, position set to centre of screen, min width/height set to initial values
	 */
	public Window(final int length, final int height, final String title, final boolean active, final boolean minimizable, final boolean resizeable, final boolean closeable) {
		this((getGdxWidth() - length) / 2, (getGdxHeight() + height) / 2, length, height, Color.GRAY, Color.BLACK, title, active, length, height, minimizable, resizeable, closeable);
	}


	/** Truncates the string based on length of window */
	protected String truncate(final String string) {
		String answer = string.substring(0, width / 10 - 6 < 0 ? 0 : width / 10 - 6 > string.length() ? string.length() : width / 10 - 6);
		if (answer.length() < string.length()) {
			answer = answer + "...";
		}
		return answer;
	}


	@Override
	public boolean keyPressed(final int keyCode) {
		if (keyCode == Input.Keys.ESCAPE && closeable) {
			setClosing(true);
			return true;
		}
		return false;
	}


	/** Called when left clicked */
	@Override
	public boolean leftClick(final List<ContextMenu> copy, final Deque<Component> windowsCopy) {
		if (minimized) {
			return false;
		}

		if (userInterface.getContextMenus().isEmpty()) {
			if (isMouseWithin()) {

				if (closeButton.click() && closeable) {
					setClosing(true);
					return false;
				}

				if (minimizable && minimizeButton.click()) {
					minimized = true;
					return true;
				}

				if (resizeable) {
					resizeButton.click();
				}

				if (isActive()) {
					if (!isClosing()) {
						internalLeftClick(copy, windowsCopy);
					}
					determinePositioning();

					if (alwaysActive) {
						windowsCopy.remove(this);
						windowsCopy.addLast(this);
						determinePositioning();
					}

					return true;
				} else {
					windowsCopy.remove(this);
					windowsCopy.addLast(this);
					determinePositioning();
					setActive(true);
					return true;
				}
			} else {
				setActive(false);
				return false;
			}
		}

		return false;
	}


	/** Called when right clicked */
	public boolean rightClick(final Deque<Component> windowsCopy) {
		if (minimized) {
			return false;
		}

		if (userInterface.getContextMenus().isEmpty()) {
			if (isActive() && isMouseWithin()) {
				return true;
			} else if (isMouseWithin()) {
				windowsCopy.remove(this);
				windowsCopy.addLast(this);
				setActive(true);
				return true;
			} else {
				setActive(false);
				return false;
			}
		}
		return false;
	}


	/** If mouse clicked on the header, then we're positioning */
	private void determinePositioning() {
		oldX = x;
		oldY = y;
		mx = getMouseScreenX();
		my = getMouseScreenY();

		if (mx > x && mx < x + width && my < y && my > y - 25) {
			positioning = true;
		}
	}


	/** True if mouse is within the window */
	private boolean isMouseWithin() {
		final int posX = getMouseScreenX();
		final int posY = getMouseScreenY();
		return posX > x && posX < x + width && posY < y && posY > y - height;
	}


	/** Loads buttons */
	private void loadButtons() {
		closeButton = new Button(
			UserInterface.uiTexture,
			x + width - 9,
			y - CLOSE.getRegionHeight() - TOP.getRegionHeight() + 5,
			29,
			0,
			12,
			12,
			() -> {},
			UIRef.BL
		);

		minimizeButton = new Button(
			UserInterface.uiTexture,
			x + width - 26,
			y - CLOSE.getRegionHeight() - TOP.getRegionHeight() + 5,
			15,
			0,
			12,
			12,
			() -> {},
			UIRef.BL
		);

		resizeButton = new Button(
			UserInterface.uiTexture,
			x + width - 26,
			y - height,
			41,
			0,
			12,
			12,
			() -> {
				mx = getMouseScreenX();
				my = getMouseScreenY();
				oldLength = width;
				oldHeight = height;
				resizing = true;
			},
			UIRef.BL
		);
	}

	/** Render world-level UI guides */
	public void renderWorldUIGuide() {}

	/** Render implementation specific rendering of this {@link Window} */
	protected abstract void internalWindowRender(Graphics graphics);

	/** Implementation-specific left click method */
	protected abstract void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy);

	/** Called when this window is closed */
	protected abstract void uponClose();

	/** Renders this {@link Window} */
	@Override
	protected void internalComponentRender(final Graphics graphics) {
		resize();
		reposition();

		final SpriteBatch batch = graphics.getSpriteBatch();

		batch.begin();
		renderRectangle(x + BOTTOM_LEFT.getRegionWidth(), y + BOTTOM_LEFT.getRegionHeight(), width, height, isActive(), backGroundColor, graphics);
		renderBox(x, y, width, height, isActive(), borderColor, graphics);
		renderSeparator(batch);
		renderWindowButtons(graphics);
		renderTitle(batch);
		batch.end();

		batch.begin();
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		internalWindowRender(graphics);
		Gdx.gl.glDisable(GL20.GL_BLEND);
		batch.end();
	}


	/** Handles the repositioning of the {@link Window} */
	private void reposition() {
		if (positioning) {
			if (isButtonPressed(Wiring.injector().getInstance(Controls.class).leftClick.keyCode)) {
				x = oldX + getMouseScreenX() - mx;
				y = oldY + getMouseScreenY() - my;
			} else {
				positioning = false;
			}
		}
	}


	/** Handles the resizing of the {@link Window} */
	private void resize() {
		if (resizing) {
			if (isButtonPressed(Wiring.injector().getInstance(Controls.class).leftClick.keyCode)) {
				final int calcualtedNewLength = oldLength + getMouseScreenX() - mx;
				final int calcualtedNewHeight = oldHeight - getMouseScreenY() + my;
				width = calcualtedNewLength < minLength ? minLength : calcualtedNewLength;
				height = calcualtedNewHeight < minHeight ? minHeight : calcualtedNewHeight;
			} else {
				resizing = false;
			}
		}
	}


	/**
	 * Renders the separator that separates the body of the window from the head
	 */
	private void renderSeparator(final SpriteBatch batch) {
		batch.draw(SEPARATOR_END, x + LEFT.getRegionWidth() + 4, y - 20);
		batch.draw(SEPARATOR_BODY, x + LEFT.getRegionWidth() + 5, y - 21, width - 10, SEPARATOR_BODY.getRegionHeight());
		batch.draw(SEPARATOR_END, x + width - 3, y - 20);
	}


	/**
	 * Handles closing of this component
	 */
	public void close(final ArrayDeque<Component> windowsCopy) {
		if (isClosing()) {
			if (getAlpha() == 0f) {
				windowsCopy.remove(this);
				uponClose();
			}
		}
	}


	/**
	 * Renders the window buttons of this {@link Window}
	 */
	private void renderWindowButtons(final Graphics graphics) {
		if (closeable) {
			closeButton.render(
				x + width - 7,
				y - CLOSE.getRegionHeight() - TOP.getRegionHeight() + 5,
				isActive(),
				isActive() ? getAlpha() : getAlpha() * 0.5f,
				graphics
			);
		}

		if (minimizable) {
			minimizeButton.render(
				x + width - 24,
				y - CLOSE.getRegionHeight() - TOP.getRegionHeight() + 5,
				isActive(),
				isActive() ? getAlpha() : getAlpha() * 0.5f,
				graphics
			);
		}

		if (resizeable) {
			resizeButton.render(
				x + width - 7,
				y - height + 9,
				isActive(),
				isActive() ? getAlpha() : getAlpha() * 0.5f,
				graphics
			);
		}
	}


	/**
	 * Render the title of this window
	 */
	private void renderTitle(final SpriteBatch batch) {
		batch.setShader(Shaders.text);
		defaultFont.setColor(1f, 1f, 1f, 1f * getAlpha() * (isActive() ? 1f : 0.7f));
		defaultFont.draw(batch, truncate(title), x + 6, y - 3);
		defaultFont.setColor(1f, 1f, 1f, 1f);
	}


	public void setAlwaysActive(final boolean alwaysActive) {
		this.alwaysActive = alwaysActive;
	}


	/**
	 * @return some sort of unique but deterministic {@link Object} to determine uniqueness when using {@link UserInterface#addLayeredComponentUnique(Component)}
	 */
	public abstract Object getUniqueIdentifier();
}
