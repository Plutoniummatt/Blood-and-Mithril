package bloodandmithril.ui.components.panel;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Deque;
import java.util.List;

import bloodandmithril.core.Copyright;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.Panel;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;

import com.badlogic.gdx.graphics.Color;

/**
 * Panel to display some text
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class TextPanel extends Panel {

	private String text;
	private Color textColor;

	/**
	 * Constructor
	 */
	public TextPanel(Component parent, String text, Color textColor) {
		super(parent);
		this.text = text;
		this.textColor = textColor;
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
		if (parent == null) {
			defaultFont.setColor(Colors.modulateAlpha(textColor, 1.0f));
		} else {
			defaultFont.setColor(Colors.modulateAlpha(textColor, parent.getAlpha() * (parent.isActive() ? 1.0f : 0.6f)));
		}
		defaultFont.drawMultiLine(
			spriteBatch,
			Util.fitToWindow(text, width, height / 20 - 1),
			x,
			y
		);
		spriteBatch.flush();
	}


	@Override
	public boolean keyPressed(int keyCode) {
		return false;
	}
}