package bloodandmithril.world;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import bloodandmithril.core.Copyright;
import bloodandmithril.event.events.ConstructionFinished;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.construction.Construction;

/**
 * Class to encapsulate {@link Prop} logic on {@link World}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public final class WorldProps implements Serializable {
	private static final long serialVersionUID = -4455079330719120993L;

	/** Every {@link Prop} that exists */
	private final ConcurrentHashMap<Integer, Prop> props = new ConcurrentHashMap<>();
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
	public final boolean hasProp(int id) {
		return props.containsKey(id);
	}


	/**
	 * @return a prop by id.
	 */
	public final Prop getProp(int id) {
		return props.get(id);
	}


	/**
	 * @return a prop with the specified class, use with caution.
	 */
	public final Prop getAnyProp(Class<? extends Prop> propClass) {
		for (Prop p : props.values()) {
			if (p.getClass().equals(propClass)) {
				return p;
			}
		}

		return null;
	}


	/**
	 * @return all props
	 */
	public final Collection<Prop> getProps() {
		return props.values();
	}


	/**
	 * Adds a prop
	 */
	public final void addProp(Prop prop) {
		prop.setWorldId(worldId);
		props.put(prop.id, prop);
		Domain.getWorld(worldId).getPositionalIndexChunkMap().get(prop.position.x, prop.position.y).addProp(prop.id);

		if (prop instanceof Construction) {
			if (((Construction) prop).getConstructionProgress() == 1f) {
				Domain.getWorld(prop.getWorldId()).addEvent(new ConstructionFinished((Construction) prop));
			}
		}
	}


	/**
	 * Removes a prop
	 */
	public final Prop removeProp(int key) {
		Prop prop = props.get(key);
		Domain.getWorld(worldId).getPositionalIndexChunkMap().get(prop.position.x, prop.position.y).removeProp(prop.id);
		return props.remove(key);
	}
}
