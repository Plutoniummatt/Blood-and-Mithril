package bloodandmithril.ui.components.panel;

import java.util.Deque;
import java.util.List;

import bloodandmithril.Fortress;
import bloodandmithril.ui.KeyMappings;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.Panel;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

/**
 * {@link Panel} for text input
 *
 * @author Matt
 */
public class TextInputFieldPanel extends Panel {

	/** Text being input */
	private String inputText = "";

	/** Current beginning index of the input string */
	private int currentBeginningIndex;

	/**
	 * Constructor
	 */
	public TextInputFieldPanel(Component parent) {
		super(parent);
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
		Component.shapeRenderer.begin(ShapeType.FilledRectangle);
		Component.shapeRenderer.setColor(0f, 0f, 0f, parent.active ? 0.9f : 0.4f);
		Component.shapeRenderer.filledRect(x, y - height, width, 22);
		Component.shapeRenderer.end();

		Component.shapeRenderer.begin(ShapeType.Rectangle);
		Component.shapeRenderer.setColor(1f, 1f, 1f, parent.active ? 0.9f : 0.4f);
		Component.shapeRenderer.rect(x, y - height, width, 22);
		Component.shapeRenderer.end();

		Fortress.spriteBatch.end();
		Fortress.spriteBatch.begin();
		Fonts.defaultFont.setColor(Color.ORANGE);
		Fonts.defaultFont.draw(Fortress.spriteBatch, inputText == null ? "" : Util.fitToTextInputBox(inputText, width, currentBeginningIndex), x + 4, y - height + 16);
	}


	@Override
	public boolean keyPressed(int keyCode) {
		inputText = inputText + KeyMappings.keyMap.get(keyCode);
		return true;
	}
}