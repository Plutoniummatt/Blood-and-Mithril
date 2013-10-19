package bloodandmithril.ui.components.window;

import static bloodandmithril.util.Fonts.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;


import bloodandmithril.Fortress;
import bloodandmithril.ui.KeyMappings;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Task;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * A window, able to be dragged, minimized, stays open until closed (unless game decides it should close).
 *
 * @author Matt
 */
public abstract class Window extends Component {

	/** Colors of this window */
	protected Color borderColor, backGroundColor;

	/** Position of this window */
	public int x, y, length, height, oldLength, oldHeight, mx, my, oldX, oldY, minLength, minHeight;

	/** {@link ShapeRenderer} to use */
	protected ShapeRenderer shapeRenderer = new ShapeRenderer();

	/** Whether or not this {@link Window} is currently being resized */
	private boolean resizing = false;

	/** Whether or not this {@link Window} is currently being positioned */
	private boolean positioning = false;

	/** Whether or not this {@link Window} can be minimized*/
	public final boolean minimizable;

	/** True if minimized */
	public boolean minimized = false;

	/** Title of this {@link Window} */
	public String title;

	/** Buttons for closing and minimizing this {@link Window} */
	private Button closeButton, minimizeButton, resizeButton;

	/**
	 * Constructor
	 */
	public Window(int x, int y, int length, int height, Color borderColor, Color backGroundColor, String title, boolean active, int minLength, int minHeight, boolean minimizable) {
		this.x = x;
		this.y = y;
		this.length = length;
		this.height = height;
		this.borderColor = borderColor;
		this.backGroundColor = backGroundColor;
		this.title = title;
		this.active = active;
		this.minLength = minLength;
		this.minHeight = minHeight;
		this.minimizable = minimizable;

		loadButtons();
	}


	/**
	 * Overloaded contructor, uses default colors
	 */
	public Window(int x, int y, int length, int height, String title, boolean active, int minLength, int minHeight, boolean minimizable) {
		this(x, y, length, height, Color.GRAY, Color.BLACK, title, active, minLength, minHeight, minimizable);
	}


	/** Truncates the string based on length of window */
	protected String truncate(String string) {
		String answer = string.substring(0, length / 10 - 6 < 0 ? 0 : length / 10 - 6 > string.length() ? string.length() : length / 10 - 6);
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
			if (closeButton.click()) {
				closing = true;
				return false;
			}

			if (minimizable && minimizeButton.click()) {
				minimized = true;
				return true;
			}

			if (active && isWithin()) {
				resizeButton.click();
				internalLeftClick(copy, windowsCopy);
				determinePositioning();
				return true;
			} else if (isWithin()) {
				windowsCopy.remove(this);
				windowsCopy.addLast(this);
				resizeButton.click();
				internalLeftClick(copy, windowsCopy);
				determinePositioning();
				active = true;
				return true;
			} else {
				active = false;
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
			if (active && isWithin()) {
				return true;
			} else if (isWithin()) {
				windowsCopy.remove(this);
				windowsCopy.addLast(this);
				active = true;
				return true;
			} else {
				active = false;
				return false;
			}
		}
		return false;
	}


	/** True if mouse is within the window */
	private boolean isWithin() {
		int posX = Fortress.getMouseScreenX();
		int posY = Fortress.getMouseScreenY();
		return posX > x && posX < x + length && posY < y && posY > y - height;
	}


	/** If mouse clicked on the header, then we're positioning */
	private void determinePositioning() {
		oldX = x;
		oldY = y;
		mx = Fortress.getMouseScreenX();
		my = Fortress.getMouseScreenY();

		if (mx > x && mx < x + length && my < y && my > y - 25) {
			positioning = true;
		}
	}


	/** Loads buttons */
	private void loadButtons() {
		closeButton = new Button(
			UserInterface.uiTexture,
			x + length - 9,
			y - close.getRegionHeight() - top.getRegionHeight() + 5,
			29,
			0,
			12,
			12,
			new Task() {
				@Override
				public void execute() {
				}
			},
			UIRef.BL
		);

		minimizeButton = new Button(
			UserInterface.uiTexture,
			x + length - 26,
			y - close.getRegionHeight() - top.getRegionHeight() + 5,
			15,
			0,
			12,
			12,
			new Task() {
				@Override
				public void execute() {
				}
			},
			UIRef.BL
		);

		resizeButton = new Button(
			UserInterface.uiTexture,
			x + length - 26,
			y - height,
			41,
			0,
			12,
			12,
			new Task() {
				@Override
				public void execute() {
					mx = Fortress.getMouseScreenX();
					my = Fortress.getMouseScreenY();
					oldLength = length;
					oldHeight = height;
					resizing = true;
				}
			},
			UIRef.BL
		);
	}


	/** Render implementation specific rendering of this {@link Window} */
	protected abstract void internalWindowRender();

	/** Implementation-specific left click method */
	protected abstract void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy);


	/** Renders this {@link Window} */
	@Override
	protected void internalComponentRender() {
		resize();
		reposition();

		Fortress.spriteBatch.begin();
		renderRectangle(x + bottomLeft.getRegionWidth(), y + bottomLeft.getRegionHeight(), length, height, active, backGroundColor);
		renderBox(x, y, length, height, active, borderColor);
		renderSeparator();
		renderWindowButtons();
		renderTitle();

		internalWindowRender();
		Fortress.spriteBatch.end();
	}


	/** Handles the repositioning of the {@link Window} */
	private void reposition() {
		if (positioning) {
			if (Gdx.input.isButtonPressed(KeyMappings.leftClick)) {
				x = oldX + Fortress.getMouseScreenX() - mx;
				y = oldY + Fortress.getMouseScreenY() - my;
			} else {
				positioning = false;
			}
		}
	}


	/** Handles the resizing of the {@link Window} */
	private void resize() {
		if (resizing) {
			if (Gdx.input.isButtonPressed(KeyMappings.leftClick)) {
				int calcualtedNewLength = oldLength + Fortress.getMouseScreenX() - mx;
				int calcualtedNewHeight = oldHeight - Fortress.getMouseScreenY() + my;
				length = calcualtedNewLength < minLength ? minLength : calcualtedNewLength;
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
		Fortress.spriteBatch.draw(separatorEnd, x + left.getRegionWidth() + 4, y - 20);
		Fortress.spriteBatch.draw(separatorBody, x + left.getRegionWidth() + 5, y - 21, length - 10, separatorBody.getRegionHeight());
		Fortress.spriteBatch.draw(separatorEnd, x + length - 3, y - 20);
	}


	/**
	 * Handles closing of this component
	 */
	public void close(ArrayDeque<Component> windowsCopy) {
		if (closing) {
			if (alpha == 0f) {
				windowsCopy.remove(this);
				windowsCopy.remove(this);
			}
		}
	}


	/**
	 * Renders the window buttons of this {@link Window}
	 */
	private void renderWindowButtons() {
		Fortress.spriteBatch.setShader(Shaders.filter);

		closeButton.render(
			x + length - 7,
			y - close.getRegionHeight() - top.getRegionHeight() + 5,
			active,
			alpha
		);

		if (minimizable) {
			minimizeButton.render(
				x + length - 24,
				y - close.getRegionHeight() - top.getRegionHeight() + 5,
				active,
				alpha
			);
		}

		resizeButton.render(
			x + length - 7,
			y - height + 9,
			active,
			alpha
		);
	}


	/**
	 * Render the title of this window
	 */
	private void renderTitle() {
		Fortress.spriteBatch.setShader(Shaders.text);
		if (active) {
			defaultFont.setColor(1f, 1f, 1f, 1f * alpha);
		} else {
			defaultFont.setColor(0.5f, 0.5f, 0.5f, 0.7f * alpha);
		}
		defaultFont.draw(Fortress.spriteBatch, truncate(title), x + 6, y - 3);
		defaultFont.setColor(1f, 1f, 1f, 1f);
	}
}
