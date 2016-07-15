package bloodandmithril.prop.updateservice;

import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.prop.Growable;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.plant.seed.SeedProp;
import bloodandmithril.world.Domain;

/**
 * Updates {@link SeedProp}s
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class SeedPropUpdateService implements PropUpdateService {

	@Override
	public void update(Prop prop, float delta) {
		SeedProp seed = (SeedProp) prop;
		
		if (seed.getGerminationProgress() >= 1f) {
			seed.setGerminationProgress(1f);
			Growable germinate = seed.germinate();
			Domain.getWorld(seed.getWorldId()).props().removeProp(seed.id);
			Domain.getWorld(seed.getWorldId()).props().addProp(germinate);
		} else {
			seed.growth(delta);
		}
	}
}