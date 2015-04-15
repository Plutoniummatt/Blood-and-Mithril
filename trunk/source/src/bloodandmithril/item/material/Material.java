package bloodandmithril.item.material;

import java.io.Serializable;
import java.util.Map;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.material.metal.Metal;
import bloodandmithril.item.material.mineral.Mineral;
import bloodandmithril.item.material.wood.Wood;

import com.google.common.collect.Maps;

/**
 * Material interface
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class Material implements Serializable {
	private static final long serialVersionUID = 5771671188230385115L;

	/**
	 * Map from a class of material to a singleton instance of that material
	 */
	private static final Map<Class<? extends Material>, Material> materials = Maps.newHashMap();

	static {
		Metal.metals(materials);
		Mineral.minerals(materials);
		Wood.woods(materials);
	}


	@SuppressWarnings("unchecked")
	public static <T extends Material> T getMaterial(Class<T> clazz) {
		return (T) materials.get(clazz);
	}

	/**
	 * @return the Name of this {@link Material}
	 */
	public abstract String getName();

	/**
	 * @return the multiplier value for things made from this {@link Material}
	 */
	public abstract float getCombatMultiplier();
}