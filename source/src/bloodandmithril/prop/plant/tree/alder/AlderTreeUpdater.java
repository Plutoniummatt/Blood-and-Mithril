package bloodandmithril.prop.plant.tree.alder;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.updateservice.PropUpdateService;
import bloodandmithril.world.Domain;
import bloodandmithril.world.weather.WeatherService;

/**
 * Updates {@link AlderTree}
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class AlderTreeUpdater implements PropUpdateService {
	
	@Inject private WeatherService weatherService;

	@Override
	public void update(Prop prop, float delta) {
		AlderTree alder = (AlderTree) prop;
		float wind = weatherService.getWind(Domain.getWorld(prop.getWorldId()), alder.position);
		
		// Treat curvature like simple harmonic (trunk elasticity) oscillator with dampening (leaves) and dynamic linear force (wind)
		alder.acceleration = 
				-(
					alder.rigidity * alder.curvature + 	// Elastic
					wind * 2f + 						// Wind
					alder.velocity / 2f					// Drag
				) / (
					0.2f * alder.getHeight()			// "Mass"
				);
		
		alder.velocity += alder.acceleration * 0.016f;
		alder.curvature += alder.velocity * 0.016f;
	}
}