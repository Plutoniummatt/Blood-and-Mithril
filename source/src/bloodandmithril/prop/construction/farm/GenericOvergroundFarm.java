package bloodandmithril.prop.construction.farm;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;

import java.util.Map;
import java.util.Set;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Growable;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.plant.Carrot;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.tiles.SoilTile;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Overground farm for farming....stuff
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class GenericOvergroundFarm extends Farm {
	private static final long serialVersionUID = 4591966874731461699L;
	
	public static TextureRegion texture;
	
	public GenericOvergroundFarm(float x, float y) {
		super(x, y, 250, 70, 0.1f, new CanBuildOnTopOfSoilTile());
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return texture;
	}


	@Override
	public String getDescription() {
		return "A farm, primarily used for the production of food and other harvestable material.";
	}


	@Override
	public String getTitle() {
		return "Farm";
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		return Maps.newHashMap();
	}


	@Override
	public void synchronizeProp(Prop other) {
		this.currentCrop = (((GenericOvergroundFarm) other).getCurrentCrop());
	}


	@Override
	public void update(float delta) {
		if (getCurrentCrop() == null) {
			return;
		}
		
		for (Growable crop : getCurrentCrop()) {
			crop.grow(delta/crop.getGrowthTime());
			if (crop.getGrowthProgress() >= 1f) {
				giveItem(crop.harvest());
				crop.grow(-1f);
				
				if (ClientServerInterface.isClient()) {
					UserInterface.addUITask(() -> {
						UserInterface.refreshRefreshableWindows();	
					});
				} else {
					ClientServerInterface.SendNotification.notifyRefreshWindows();
				}
			}	
		}
	}


	@Override
	protected void internalRender(float constructionProgress) {
		spriteBatch.draw(getTextureRegion(), position.x - width / 2, position.y);
	}


	public static class CanBuildOnTopOfSoilTile extends SerializableMappingFunction<Tile, Boolean> {
		private static final long serialVersionUID = 6075548657431497319L;

		@Override
		public Boolean apply(Tile t) {
			return t instanceof SoilTile;
		}
	}


	@Override
	public Set<Growable> getGrowables() {
		return Sets.newHashSet(new Carrot(0, 0));
	}
}