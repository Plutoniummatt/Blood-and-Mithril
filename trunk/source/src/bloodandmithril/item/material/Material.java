package bloodandmithril.item.material;

import java.util.Map;

import bloodandmithril.item.material.metal.Metal;

import com.google.common.collect.Maps;

/**
 * Material interface
 *
 * @author Matt
 */
public abstract class Material {

	/**
	 * Map from a class of material to a singleton instance of that material
	 */
	private static final Map<Class<? extends Material>, Material> materials = Maps.newHashMap();

	static {
		Metal.metals(materials);
	}


	@SuppressWarnings("unchecked")
	public static <T extends Material> T getMaterial(Class<T> clazz) {
		return (T) materials.get(clazz);
	}


	/**
	 * @return the Name of this {@link Material}
	 */
	public abstract String getName();
}