package bloodandmithril.prop.updateservice;

import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.plant.CarrotProp;

/**
 * Updates {@link CarrotProp}s
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class CarrotPropUpdateService implements PropUpdateService {

	@Override
	public void update(Prop prop, float delta) {
		((CarrotProp) prop).grow(delta / 100f);
	}
}
