package bloodandmithril.ui.components.panel;

import static bloodandmithril.control.InputUtilities.isKeyPressed;

import java.util.Deque;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.google.inject.Inject;

import bloodandmithril.control.Controls;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.Panel;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Util;
import bloodandmithril.util.datastructure.WrapperForTwo;

/**
 * {@link Panel} for text input
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class TextInputFieldPanel extends Panel {

	/** Text being input */
	private String inputText = "";

	/** Current beginning index of the input string */
	private int currentBeginningIndex;

	/** The amount of time backspace must be held before bulk backspace is triggered */
	private float timer;

	@Inject private Controls controls;

	/**
	 * Constructor
	 */
	public TextInputFieldPanel(final Component parent, final String defaultText) {
		super(parent);
		this.inputText = defaultText;
	}


	/** Gets the input text in its current state */
	public String getInputText() {
		return inputText;
	}


	@Override
	public boolean leftClick(final List<ContextMenu> copy, final Deque<Component> windowsCopy) {
		return false;
	}


	@Override
	public void leftClickReleased() {
	}


	@Override
	public void render(final Graphics graphics) {
		if (isKeyPressed(controls.deleteCharacter.keyCode)) {
			if (timer < 0) {
				keyPressed(controls.deleteCharacter.keyCode);
				timer = 0.02f;
			} else {
				timer -= Gdx.graphics.getDeltaTime();
			}
		} else {
			timer = 0.5f;
		}

		Gdx.gl20.glLineWidth(2f);
		Component.shapeRenderer.begin(ShapeType.Filled);
		Component.shapeRenderer.setColor(0f, 0f, 0f, parent.isActive() ? 0.9f * parent.getAlpha(): 0.4f * parent.getAlpha());
		Component.shapeRenderer.rect(x, y - height, width, 22);
		Component.shapeRenderer.end();

		Component.shapeRenderer.begin(ShapeType.Line);
		Component.shapeRenderer.setColor(1f, 1f, 1f, parent.isActive() ? 0.9f * parent.getAlpha() : 0.4f * parent.getAlpha());
		Component.shapeRenderer.rect(x, y - height, width, 22);
		Component.shapeRenderer.end();

		graphics.getSpriteBatch().end();
		graphics.getSpriteBatch().begin();
		Fonts.defaultFont.setColor(Color.ORANGE.r, Color.ORANGE.g, Color.ORANGE.b, parent.isActive() ? parent.getAlpha() : 0.4f * parent.getAlpha());
		Fonts.defaultFont.draw(graphics.getSpriteBatch(), inputText == null ? "" : Util.fitToTextInputBox(inputText, width, currentBeginningIndex, true), x + 4, y - height + 16);
	}


	public void clear() {
		inputText = "";
	}


	@Override
	public boolean keyPressed(final int keyCode) {
		if (keyCode == Input.Keys.SHIFT_LEFT || keyCode == Input.Keys.SHIFT_RIGHT) {
			return false;
		}

		if (keyCode == controls.deleteCharacter.keyCode) {
			if (inputText.length() == 0) {
				return true;
			}
			inputText = inputText.substring(0, inputText.length() - 1);
		}

		final WrapperForTwo<String, String> string = Controls.keyMap.get(keyCode);
		if (string == null) {
			return true;
		}

		if (isKeyPressed(Input.Keys.SHIFT_LEFT) || isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
			inputText = inputText + string.b;
		} else {
			inputText = inputText + Controls.keyMap.get(keyCode).a;
		}

		return true;
	}
}