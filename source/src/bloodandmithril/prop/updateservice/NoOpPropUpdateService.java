package bloodandmithril.prop.updateservice;

import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.prop.Prop;

/**
 * No-Op implementation
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class NoOpPropUpdateService implements PropUpdateService {

	@Override
	public void update(Prop prop, float delta) {
	}
}