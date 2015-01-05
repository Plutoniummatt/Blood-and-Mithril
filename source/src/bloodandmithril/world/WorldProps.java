package bloodandmithril.world;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import bloodandmithril.core.Copyright;
import bloodandmithril.prop.Prop;

/**
 * Class to encapsulate {@link Prop} logic on {@link World}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class WorldProps implements Serializable {
	private static final long serialVersionUID = -4455079330719120993L;

	/** Every {@link Prop} that exists */
	private ConcurrentHashMap<Integer, Prop> props = new ConcurrentHashMap<>();
	private final int worldId;

	/**
	 * Constructor
	 */
	public WorldProps(int worldId) {
		this.worldId = worldId;
	}


	/**
	 * @return whether a prop exists by id
	 */
	public boolean hasProp(int id) {
		return props.containsKey(id);
	}


	/**
	 * @return a prop by id.
	 */
	public Prop getProp(int id) {
		return props.get(id);
	}


	/**
	 * @return all props
	 */
	public Collection<Prop> getProps() {
		return props.values();
	}


	/**
	 * Adds a prop
	 */
	public void addProp(Prop prop) {
		prop.setWorldId(worldId);
		props.put(prop.id, prop);
		Domain.getWorld(worldId).getPositionalIndexMap().get(prop.position.x, prop.position.y).addProp(prop.id);
	}


	/**
	 * Removes a prop
	 */
	public Prop removeProp(int key) {
		Prop prop = props.get(key);
		Domain.getWorld(worldId).getPositionalIndexMap().get(prop.position.x, prop.position.y).removeProp(prop.id);
		return props.remove(key);
	}
}
