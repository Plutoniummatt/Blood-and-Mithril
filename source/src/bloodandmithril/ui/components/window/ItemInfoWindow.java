package bloodandmithril.ui.components.window;

import static bloodandmithril.util.Fonts.defaultFont;
import static bloodandmithril.util.Util.Colors.modulateAlpha;

import java.util.Deque;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.MeleeWeapon;
import bloodandmithril.item.items.equipment.weapon.Weapon;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.Panel;
import bloodandmithril.ui.components.panel.TextPanel;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Util.Colors;

/**
 * Information window to display stats and description of an {@link Item}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class ItemInfoWindow extends Window {

	private final Item item;
	private Panel customPanel;

	/**
	 * Constructor
	 */
	public ItemInfoWindow(Item item, int length, int height) {
		super(length, height, item.getSingular(true), true, 400, 350, false, true, true);
		this.item = item;
		instantiatePanel(item);
	}


	/**
	 * Instantiates the {@link #customPanel}
	 */
	private void instantiatePanel(Item item) {
		customPanel = new TextPanel(this, item.getDescription(), Color.ORANGE);
	}


	@Override
	protected void internalWindowRender(Graphics graphics) {
		renderBasicStats(graphics);
		renderCustomPanel(graphics);
		renderItemIcon(graphics);
	}


	private void renderCustomPanel(Graphics graphics) {
		if (customPanel == null) {
			return;
		}

		customPanel.x = x + 10;
		customPanel.y = y - getCustomPanelOffset();
		customPanel.width = width;
		customPanel.height = height - getCustomPanelOffset();

		customPanel.render(graphics);
	}


	private int getCustomPanelOffset() {
		if (item instanceof Weapon) {
			return 240;
		}

		return 120;
	}


	/**
	 * Renders basic item stats
	 */
	@SuppressWarnings("rawtypes")
	private void renderBasicStats(Graphics graphics) {
		defaultFont.setColor(Colors.modulateAlpha(Color.GREEN, getAlpha() * (isActive() ? 1f : 0.6f)));
		defaultFont.draw(graphics.getSpriteBatch(), "Type: " + item.getType().getValue(), x + 10, y - 33);
		defaultFont.draw(graphics.getSpriteBatch(), "Mass: " + item.getMass() + "kg", x + 10, y - 53);
		defaultFont.draw(graphics.getSpriteBatch(), "Value: " + item.getValue(), x + 10, y - 73);

		if (item instanceof Weapon) {
			weapon((Weapon) item, graphics);
		}

		graphics.getSpriteBatch().flush();
	}


	@SuppressWarnings("rawtypes")
	private void weapon(Weapon weapon, Graphics graphics) {
		if (weapon instanceof MeleeWeapon) {
			MeleeWeapon meleeWeapon = (MeleeWeapon) weapon;
			defaultFont.draw(graphics.getSpriteBatch(), "Parry chance: " + String.format("%.0f", meleeWeapon.getParryChance() * 100f) + "%", x + 10, y - 103);
			defaultFont.draw(graphics.getSpriteBatch(), "Parry ignore: " + String.format("%.0f", meleeWeapon.getParryChanceIgnored() * 100f) + "%", x + 10, y - 123);
			defaultFont.draw(graphics.getSpriteBatch(), "Base damage: " + String.format("%.1f", meleeWeapon.getBaseMinDamage()) + " - " + String.format("%.1f", meleeWeapon.getBaseMaxDamage()), x + 10, y - 143);
			defaultFont.draw(graphics.getSpriteBatch(), "Base attack duration: " + String.format("%.1f", meleeWeapon.getBaseAttackPeriod()) + "s", x + 10, y - 163);
			defaultFont.draw(graphics.getSpriteBatch(), "DPS: " + String.format("%.1f", (meleeWeapon.getBaseMinDamage() + meleeWeapon.getBaseMaxDamage()) / 2f /meleeWeapon.getBaseAttackPeriod()), x + 10, y - 183);
		}
	}


	/**
	 * Renders the item icon
	 */
	private void renderItemIcon(Graphics graphics) {
		renderRectangle(x + width - 74, y - 30, 64, 64, isActive(), modulateAlpha(Color.BLACK, 1f));
		renderBox(x + width - 76, y - 32, 64, 64, isActive(), Color.GRAY, graphics);

		TextureRegion icon = item.getIconTextureRegion();
		if (icon != null) {
			graphics.getSpriteBatch().setShader(Shaders.filter);
			Shaders.filter.setUniformf("color", 1f, 1f, 1f, getAlpha() * (isActive() ? 1f : 0.6f));
			graphics.getSpriteBatch().draw(icon, x + width - 74, y - 96);
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