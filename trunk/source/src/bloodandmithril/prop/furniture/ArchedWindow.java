package bloodandmithril.prop.furniture;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;

import java.util.Map;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.item.Item;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.building.Construction;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

/**
 * An arched window
 *
 * @author Matt
 */
public class ArchedWindow extends Construction {
	private static final long serialVersionUID = 2920616011118940071L;

	/** {@link TextureRegion} of the {@link ArchedWindow} */
	public static TextureRegion archedWindow;

	public ArchedWindow(float x, float y) {
		super(
			Math.round(x) / 16 * 16,
			Math.round(y) / 16 * 16,
			116,
			196,
			false,
			0f
		);
	}


	@Override
	public String getTitle() {
		return "Arched window";
	}


	@Override
	protected void internalRender(float constructionProgress) {
		spriteBatch.draw(archedWindow, position.x - 2, position.y - 2);
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		return Maps.newHashMap();
	}


	@Override
	protected ContextMenu getCompletedContextMenu() {
		ContextMenu menu = new ContextMenu(BloodAndMithrilClient.getMouseScreenX(), BloodAndMithrilClient.getMouseScreenY());

		menu.addMenuItem(
			new MenuItem(
				"Uninstall",
				() -> {},
				Color.WHITE,
				Color.GREEN,
				Color.GRAY,
				null
			)
		);

		return menu;
	}


	@Override
	public void synchronizeProp(Prop other) {
	}


	@Override
	public void update(float delta) {
	}
}