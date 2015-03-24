package bloodandmithril.ui.components.panel;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.util.Fonts.defaultFont;
import static bloodandmithril.util.Util.Colors.modulateAlpha;

import java.util.Deque;
import java.util.List;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.Panel;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Util.Colors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A composite panel, consisting of multiple panels
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class ItemInfoPopupPanel extends Panel {

	private Item item;

	/**
	 * Constructor
	 */
	public ItemInfoPopupPanel(Component parent, Item item) {
		super(parent);
		this.item = item;
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
			defaultFont.setColor(Colors.modulateAlpha(Color.ORANGE, 1.0f));
		} else {
			defaultFont.setColor(Colors.modulateAlpha(Color.ORANGE, parent.getAlpha() * (parent.isActive() ? 1.0f : 0.6f)));
		}
		spriteBatch.setShader(Shaders.text);

		// Description
		defaultFont.drawWrapped(
			spriteBatch,
			item.getDescription(),
			x,
			y - 74,
			width
		);

		// Title
		if (parent == null) {
			defaultFont.setColor(Colors.modulateAlpha(Color.GREEN, 1.0f));
		} else {
			defaultFont.setColor(Colors.modulateAlpha(Color.GREEN, parent.getAlpha() * (parent.isActive() ? 1.0f : 0.6f)));
		}
		defaultFont.draw(
			spriteBatch,
			item.getSingular(true),
			x,
			y
		);
		defaultFont.draw(
			spriteBatch,
			"Weight: " + String.format("%.2f", item.getMass()),
			x,
			y - 23
		);
		defaultFont.draw(
			spriteBatch,
			"Volume: " + item.getVolume(),
			x,
			y - 46
		);

		spriteBatch.flush();

		renderRectangle(x + width - 76 + 3, y + 3, 64, 64, true, modulateAlpha(Color.BLACK, 1f));
		renderBox(x + width - 76, y, 64, 64, true, Color.GRAY);
		TextureRegion icon = item.getIconTextureRegion();
		if (icon != null) {
			spriteBatch.setShader(Shaders.filter);
			Shaders.filter.setUniformf("color", 1f, 1f, 1f, getAlpha());
			spriteBatch.draw(icon, x + width - 74, y - 64);
		}
		spriteBatch.flush();
	}


	@Override
	public float getAlpha() {
		return parent == null ? getAlpha() : parent.getAlpha();
	}


	@Override
	public boolean keyPressed(int keyCode) {
		return false;
	}
}