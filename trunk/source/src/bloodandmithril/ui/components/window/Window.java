package bloodandmithril.ui.components.window;

import static bloodandmithril.util.Fonts.defaultFont;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.ui.KeyMappings;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.util.Shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;

/**
 * A window, able to be dragged, minimized, stays open until closed (unless game decides it should close).
 *
 * @author Matt
 */
public abstract class Window extends Component {

	/** Colors of this window */
	public Color borderColor, backGroundColor;

	/** Position of this window */
	public int x, y, width, height, oldLength, oldHeight, mx, my, oldX, oldY, minLength, minHeight;

	/** Whether or not this {@link Window} is currently being resized */
	private boolean resizing = false;

	/** Whether or not this {@link Window} is currently being positioned */
	private boolean positioning = false;

	/** Whether or not this {@link Window} can be minimized */
	public final boolean minimizable;

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
	public Window(int x, int y, int length, int height, Color borderColor, Color backGroundColor, String title, boolean active, int minLength, int minHeight, boolean minimizable, boolean resizeable) {
		this.x = x;
		this.y = y;
		this.width = length;
		this.height = height;
		this.borderColor = borderColor;
		this.backGroundColor = backGroundColor;
		this.title = title;
		this.resizeable = resizeable;
		this.setActive(active);
		this.minLength = minLength;
		this.minHeight = minHeight;
		this.minimizable = minimizable;

		loadButtons();
	}


	/**
	 * Overloaded contructor, uses default colors
	 */
	public Window(int x, int y, int length, int height, String title, boolean active, int minLength, int minHeight, boolean minimizable, boolean resizeable) {
		this(x, y, length, height, Color.GRAY, Color.BLACK, title, active, minLength, minHeight, minimizable, resizeable);
	}


	/** Truncates the string based on length of window */
	protected String truncate(String string) {
		String answer = string.substring(0, width / 10 - 6 < 0 ? 0 : width / 10 - 6 > string.length() ? string.length() : width / 10 - 6);
		if (answer.length() < string.length()) {
			answer = answer + "...";
		}
		return answer;
	}


	/** Called when left clicked */
	@Override
	public boolean leftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		if (minimized) {
			return false;
		}

		if (UserInterface.contextMenus.isEmpty()) {
			if (isWithin()) {

				if (closeButton.click()) {
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
					internalLeftClick(copy, windowsCopy);
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
	public boolean rightClick(Deque<Component> windowsCopy) {
		if (minimized) {
			return false;
		}

		if (UserInterface.contextMenus.isEmpty()) {
			if (isActive() && isWithin()) {
				return true;
			} else if (isWithin()) {
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


	/** True if mouse is within the window */
	private boolean isWithin() {
		int posX = BloodAndMithrilClient.getMouseScreenX();
		int posY = BloodAndMithrilClient.getMouseScreenY();
		return posX > x && posX < x + width && posY < y && posY > y - height;
	}


	/** If mouse clicked on the header, then we're positioning */
	private void determinePositioning() {
		oldX = x;
		oldY = y;
		mx = BloodAndMithrilClient.getMouseScreenX();
		my = BloodAndMithrilClient.getMouseScreenY();

		if (mx > x && mx < x + width && my < y && my > y - 25) {
			positioning = true;
		}
	}


	/** Loads buttons */
	private void loadButtons() {
		closeButton = new Button(
			UserInterface.uiTexture,
			x + width - 9,
			y - close.getRegionHeight() - top.getRegionHeight() + 5,
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
			y - close.getRegionHeight() - top.getRegionHeight() + 5,
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
				mx = BloodAndMithrilClient.getMouseScreenX();
				my = BloodAndMithrilClient.getMouseScreenY();
				oldLength = width;
				oldHeight = height;
				resizing = true;
			},
			UIRef.BL
		);
	}


	/** Render implementation specific rendering of this {@link Window} */
	protected abstract void internalWindowRender();

	/** Implementation-specific left click method */
	protected abstract void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy);

	/** Called when this window is closed */
	protected abstract void uponClose();

	/** Renders this {@link Window} */
	@Override
	protected void internalComponentRender() {
		resize();
		reposition();

		BloodAndMithrilClient.spriteBatch.begin();
		renderRectangle(x + bottomLeft.getRegionWidth(), y + bottomLeft.getRegionHeight(), width, height, isActive(), backGroundColor);
		renderBox(x, y, width, height, isActive(), borderColor);
		renderSeparator();
		renderWindowButtons();
		renderTitle();
		BloodAndMithrilClient.spriteBatch.end();

		BloodAndMithrilClient.spriteBatch.begin();
		Gdx.gl.glEnable(GL10.GL_BLEND);
		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		internalWindowRender();
		Gdx.gl.glDisable(GL10.GL_BLEND);
		BloodAndMithrilClient.spriteBatch.end();
	}


	/** Handles the repositioning of the {@link Window} */
	private void reposition() {
		if (positioning) {
			if (Gdx.input.isButtonPressed(KeyMappings.leftClick)) {
				x = oldX + BloodAndMithrilClient.getMouseScreenX() - mx;
				y = oldY + BloodAndMithrilClient.getMouseScreenY() - my;
			} else {
				positioning = false;
			}
		}
	}


	/** Handles the resizing of the {@link Window} */
	private void resize() {
		if (resizing) {
			if (Gdx.input.isButtonPressed(KeyMappings.leftClick)) {
				int calcualtedNewLength = oldLength + BloodAndMithrilClient.getMouseScreenX() - mx;
				int calcualtedNewHeight = oldHeight - BloodAndMithrilClient.getMouseScreenY() + my;
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
	private void renderSeparator() {
		BloodAndMithrilClient.spriteBatch.draw(separatorEnd, x + left.getRegionWidth() + 4, y - 20);
		BloodAndMithrilClient.spriteBatch.draw(separatorBody, x + left.getRegionWidth() + 5, y - 21, width - 10, separatorBody.getRegionHeight());
		BloodAndMithrilClient.spriteBatch.draw(separatorEnd, x + width - 3, y - 20);
	}


	/**
	 * Handles closing of this component
	 */
	public void close(ArrayDeque<Component> windowsCopy) {
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
	private void renderWindowButtons() {
		closeButton.render(
			x + width - 7,
			y - close.getRegionHeight() - top.getRegionHeight() + 5,
			isActive(),
			isActive() ? getAlpha() : getAlpha() * 0.5f
		);

		if (minimizable) {
			minimizeButton.render(
				x + width - 24,
				y - close.getRegionHeight() - top.getRegionHeight() + 5,
				isActive(),
				isActive() ? getAlpha() : getAlpha() * 0.5f
			);
		}

		if (resizeable) {
			resizeButton.render(
				x + width - 7,
				y - height + 9,
				isActive(),
				isActive() ? getAlpha() : getAlpha() * 0.5f
			);
		}
	}


	/**
	 * Render the title of this window
	 */
	private void renderTitle() {
		BloodAndMithrilClient.spriteBatch.setShader(Shaders.text);
		defaultFont.setColor(1f, 1f, 1f, 1f * getAlpha() * (isActive() ? 1f : 0.7f));
		defaultFont.draw(BloodAndMithrilClient.spriteBatch, truncate(title), x + 6, y - 3);
		defaultFont.setColor(1f, 1f, 1f, 1f);
	}


	public void setAlwaysActive(boolean alwaysActive) {
		this.alwaysActive = alwaysActive;
	}
}
