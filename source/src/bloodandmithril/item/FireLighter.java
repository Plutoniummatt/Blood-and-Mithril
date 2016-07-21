package bloodandmithril.item;

import bloodandmithril.core.Copyright;
import bloodandmithril.prop.Prop;

/**
 * Indicates that something can light a fire
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public interface FireLighter {

	public void fireLightingEffect(Prop prop);
	public boolean canLightFire();
}