package bloodandmithril.prop.updateservice;

import com.google.inject.Singleton;

import bloodandmithril.character.ai.task.craft.Craft;
import bloodandmithril.core.Copyright;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.construction.craftingstation.CraftingStation;
import bloodandmithril.world.Domain;

/**
 * Updates {@link CraftingStation}s
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class CraftingStationUpdateService implements PropUpdateService {

	@Override
	public void update(Prop prop, float delta) {
		CraftingStation station = (CraftingStation) prop;
		
		if (station.getCurrentlyBeingCrafted() != null && station.getOccupier() != null) {
			station.setOccupier(Domain.getIndividual(station.getOccupier()).getAI().getCurrentTask() instanceof Craft ? station.getOccupier() : null);
		}
	}
}