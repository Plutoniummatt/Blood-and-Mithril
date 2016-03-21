package bloodandmithril.prop.plant;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;

import java.util.Collection;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Lists;

import bloodandmithril.character.ai.task.Harvest;
import bloodandmithril.character.ai.task.Trading;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.food.plant.CarrotItem.CarrotSeedItem;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Harvestable;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.tiles.SoilTile;

/**
 * The {@link Prop} equivalent to {@link bloodandmithril.item.items.food.plant.CarrotItem}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class CarrotProp extends PlantProp implements Harvestable {
	private static final long serialVersionUID = -4581900482709094877L;

	/** {@link TextureRegion} of the {@link CarrotProp} */
	public static TextureRegion halfCarrot;
	public static TextureRegion carrot;

	/**
	 * Constructor
	 */
	public CarrotProp(float x, float y) {
		super(x, y, 12, 17, Depth.MIDDLEGROUND, new SoilTilesOnly(), false);
	}


	@Override
	public void render(SpriteBatch batch) {
		if (getGrowthProgress() < 1.0f) {
			batch.draw(halfCarrot, position.x - width / 2, position.y);
		} else {
			batch.draw(carrot, position.x - width / 2, position.y);
		}
	}


	@Override
	public void synchronizeProp(Prop other) {
		// Don't need to synchronize a carrot
	}


	@Override
	public ContextMenu getContextMenu() {
		ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);
		final Harvestable thisCarrot = this;

		menu.addMenuItem(
			new MenuItem(
				"Show info",
				() -> {
					UserInterface.addLayeredComponent(
						new MessageWindow(
							bloodandmithril.item.items.food.plant.CarrotItem.description + (getGrowthProgress() == 1f ? "" : " This carrot is still growing."),
							Color.ORANGE,
							500,
							250,
							"Carrot",
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

		if (Domain.getSelectedIndividuals().size() == 1 &&
		  !(Domain.getSelectedIndividuals().iterator().next().getAI().getCurrentTask() instanceof Trading) && getGrowthProgress() == 1f) {

			menu.addMenuItem(
				new MenuItem(
					"Harvest",
					() -> {
						Individual individual = Domain.getSelectedIndividuals().iterator().next();
						if (ClientServerInterface.isServer()) {
							try {
								individual.getAI().setCurrentTask(
									new Harvest(individual, thisCarrot)
								);
							} catch (NoTileFoundException e) {
							}
						} else {
							ClientServerInterface.SendRequest.sendHarvestRequest(individual.getId().getId(), id);
						}
					},
					Color.WHITE,
					Color.GREEN,
					Color.GRAY,
					null
				)
			);
		}

		return menu;
	}


	@Override
	public Collection<Item> harvest(boolean canReceive) {
		List<Item> items = Lists.newArrayList();
		items.add(new bloodandmithril.item.items.food.plant.CarrotItem());
		items.add(new CarrotSeedItem());
		items.add(new CarrotSeedItem());
		return items;
	}


	@Override
	public boolean destroyUponHarvest() {
		return true;
	}


	@Override
	public void update(float delta) {
		grow(delta / 100f);
	}


	@Override
	public String getContextMenuItemLabel() {
		return "Carrot";
	}


	@Override
	public void preRender() {
	}


	@Override
	public boolean canBeUsedAsFireSource() {
		return false;
	}


	public static class SoilTilesOnly extends SerializableMappingFunction<Tile, Boolean> {
		private static final long serialVersionUID = 698418294898570694L;

		@Override
		public Boolean apply(Tile input) {
			return input instanceof SoilTile;
		}
	}
}