package bloodandmithril.prop.crafting;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static com.google.common.collect.Maps.newHashMap;

import java.util.List;
import java.util.Map;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.task.OpenCraftingStation;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.item.Item;
import bloodandmithril.item.equipment.Craftable;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.building.Construction;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Superclass for all {@link Prop}s that craft {@link Item}s
 *
 * @author Matt
 */
public abstract class CraftingStation extends Construction {
	private static final long serialVersionUID = 2177296386331588828L;

	/**
	 * Constructor
	 */
	protected CraftingStation(float x, float y, int width, int height) {
		super(x, y, width, height, true, 0f);
	}


	/** Returns the {@link TextureRegion} of the {@link CraftingStation} */
	protected abstract TextureRegion getTextureRegion();

	/** Returns the string description of this {@link CraftingStation} */
	protected abstract String getDescription();

	/** Returns the string title of this {@link CraftingStation} */
	protected abstract String getTitle();

	/** Returns the verb that describes the action of this {@link CraftingStation} */
	public abstract String getAction();

	/** Returns the list of {@link Craftable} {@link Item}s */
	public abstract List<Item> getCraftables();


	@Override
	protected void internalRender(float constructionProgress) {
		spriteBatch.draw(getTextureRegion(), position.x - width / 2, position.y);
	}


	@Override
	protected Map<Item, Integer> getRequiredMaterials() {
		return newHashMap();
	}


	@Override
	protected ContextMenu getCompletedContextMenu() {
		ContextMenu menu = new ContextMenu(BloodAndMithrilClient.getMouseScreenX(), BloodAndMithrilClient.getMouseScreenY());

		menu.addMenuItem(
			new MenuItem(
				"Show info",
				() -> {
					UserInterface.addLayeredComponent(
						new MessageWindow(
							getDescription(),
							Color.ORANGE,
							BloodAndMithrilClient.WIDTH/2 - 250,
							BloodAndMithrilClient.HEIGHT/2 + 125,
							500,
							250,
							getTitle(),
							true,
							300,
							150
						)
					);
				},
				Color.WHITE,
				Color.GREEN,
				Color.GRAY,
				null
			)
		);

		if (Domain.getSelectedIndividuals().size() > 0) {
			final Individual selected = Domain.getSelectedIndividuals().iterator().next();
			menu.addMenuItem(
				new MenuItem(
					getAction(),
					() -> {
						if (ClientServerInterface.isServer()) {
							selected.getAI().setCurrentTask(new OpenCraftingStation(selected, this));
						} else {
							ClientServerInterface.SendRequest.sendOpenCraftingStationRequest(selected, this);
						}
					},
					Domain.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.WHITE,
					Domain.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.GREEN,
					Domain.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.GRAY,
					new ContextMenu(0, 0, new MenuItem("You have multiple individuals selected", () -> {}, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, null)),
					() -> {
						return Domain.getSelectedIndividuals().size() > 1;
					}
				)
			);
		}

		return menu;
	}


	@Override
	public void synchronizeProp(Prop other) {
		// TODO Auto-generated method stub
	}


	@Override
	public void update(float delta) {
		// TODO Auto-generated method stub
	}
}