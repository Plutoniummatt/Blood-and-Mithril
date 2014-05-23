package bloodandmithril.ui.components.window;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.util.Fonts.defaultFont;
import static bloodandmithril.util.Util.Colors.modulateAlpha;

import java.util.Deque;
import java.util.List;

import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.material.Material;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.Panel;
import bloodandmithril.ui.components.panel.TextPanel;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Util.Colors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Information window to display stats and description of an {@link Item}
 *
 * @author Matt
 */
public class ItemInfoWindow extends Window {

	private final Item item;
	private Panel customPanel;

	/**
	 * Constructor
	 */
	public ItemInfoWindow(Item item, int x, int y, int length, int height) {
		super(x, y, length, height, item.getSingular(true), true, 400, 300, false, true);
		this.item = item;
		instantiatePanel(item);
	}


	/**
	 * Instantiates the {@link #customPanel}
	 */
	private void instantiatePanel(Item item) {
		if (item instanceof Material) {
			customPanel = new TextPanel(this, item.getDescription(), Color.ORANGE);
		}
	}


	@Override
	protected void internalWindowRender() {
		renderBasicStats();
		renderCustomPanel();
		renderItemIcon();
	}


	private void renderCustomPanel() {
		if (customPanel == null) {
			return;
		}

		customPanel.x = x + 10;
		customPanel.y = y - 120;
		customPanel.width = width;
		customPanel.height = height - 120;

		customPanel.render();
	}


	/**
	 * Renders basic item stats
	 */
	private void renderBasicStats() {
		defaultFont.setColor(Colors.modulateAlpha(Color.GREEN, getAlpha() * (isActive() ? 1f : 0.6f)));
		defaultFont.draw(spriteBatch, "Type: " + item.getType(), x + 10, y - 33);
		defaultFont.draw(spriteBatch, "Mass: " + item.getMass() + "kg", x + 10, y - 53);
		defaultFont.draw(spriteBatch, "Value: " + item.getValue(), x + 10, y - 73);
		spriteBatch.flush();
	}


	/**
	 * Renders the item icon
	 */
	private void renderItemIcon() {
		renderRectangle(x + width - 74, y - 30, 64, 64, isActive(), modulateAlpha(Color.BLACK, 1f));
		renderBox(x + width - 76, y - 32, 64, 64, isActive(), Color.GRAY);

		TextureRegion icon = item.getIconTextureRegion();
		if (icon != null) {
			spriteBatch.setShader(Shaders.filter);
			Shaders.filter.setUniformf("color", 1f, 1f, 1f, getAlpha() * (isActive() ? 1f : 0.6f));
			spriteBatch.draw(icon, x + width - 74, y - 96);
		}
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
	}


	@Override
	protected void uponClose() {
	}


	@Override
	public Object getUniqueIdentifier() {
		return item;
	}


	@Override
	public void leftClickReleased() {
	}
}