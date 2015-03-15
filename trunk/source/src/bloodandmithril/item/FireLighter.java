package bloodandmithril.item;

import bloodandmithril.prop.Prop;

/**
 * Indicates that something can light a fire
 *
 * @author Matt
 */
public interface FireLighter {
	
	public void fireLightingEffect(Prop prop);
	public boolean canLightFire();
}