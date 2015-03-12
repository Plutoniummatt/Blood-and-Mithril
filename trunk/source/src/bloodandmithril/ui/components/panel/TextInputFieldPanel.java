package bloodandmithril.ui.components.panel;

import java.util.Deque;
import java.util.List;

import bloodandmithril.control.Controls;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.Panel;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

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

	/**
	 * Constructor
	 */
	public TextInputFieldPanel(Component parent, String defaultText) {
		super(parent);
		this.inputText = defaultText;
	}


	/** Gets the input text in its current state */
	public String getInputText() {
		return inputText;
	}


	@Override
	public boolean leftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		return false;
	}


	@Override
	public void leftClickReleased() {
	}


	@Override
	public void render() {
		Gdx.gl20.glLineWidth(2f);
		Component.shapeRenderer.begin(ShapeType.Filled);
		Component.shapeRenderer.setColor(0f, 0f, 0f, parent.isActive() ? 0.9f * parent.getAlpha(): 0.4f * parent.getAlpha());
		Component.shapeRenderer.rect(x, y - height, width, 22);
		Component.shapeRenderer.end();

		Component.shapeRenderer.begin(ShapeType.Line);
		Component.shapeRenderer.setColor(1f, 1f, 1f, parent.isActive() ? 0.9f * parent.getAlpha() : 0.4f * parent.getAlpha());
		Component.shapeRenderer.rect(x, y - height, width, 22);
		Component.shapeRenderer.end();

		BloodAndMithrilClient.spriteBatch.end();
		BloodAndMithrilClient.spriteBatch.begin();
		Fonts.defaultFont.setColor(Color.ORANGE.r, Color.ORANGE.g, Color.ORANGE.b, parent.isActive() ? parent.getAlpha() : 0.4f * parent.getAlpha());
		Fonts.defaultFont.draw(BloodAndMithrilClient.spriteBatch, inputText == null ? "" : Util.fitToTextInputBox(inputText, width, currentBeginningIndex, true), x + 4, y - height + 16);
	}


	public void clear() {
		inputText = "";
	}


	@Override
	public boolean keyPressed(int keyCode) {

		if (keyCode == Input.Keys.BACKSPACE) {
			if (inputText.length() == 0) {
				return true;
			}
			inputText = inputText.substring(0, inputText.length() - 1);
		}

		String string = Controls.keyMap.get(keyCode);
		if (string == null) {
			return true;
		}

		if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
			inputText = inputText + string.toUpperCase();
		} else {
			inputText = inputText + Controls.keyMap.get(keyCode);
		}

		return true;
	}
}