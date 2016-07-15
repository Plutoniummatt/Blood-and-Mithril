package bloodandmithril.prop.updateservice;

import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.prop.Prop;

/**
 * Serice to update {@link Prop}s
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public interface PropUpdateService {

	/**
	 * Performs {@link Prop} class specific updates
	 */
	public void update(Prop prop, float delta);
}