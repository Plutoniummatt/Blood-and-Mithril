package bloodandmithril.world;

import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Optional;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.world.fluids.FluidColumn;

/**
 * Fluids of the world
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class WorldFluids {

	/** Every {@link FluidColumn} that exists */
	private final ConcurrentHashMap<Integer, FluidColumn> fluids = new ConcurrentHashMap<>();

	/** World ID */
	public final int worldId;

	/**
	 * Constructor
	 */
	public WorldFluids(final int worldId) {
		this.worldId = worldId;
	}


	/**
	 * Adds a {@link FluidColumn} to the world
	 *
	 * @param tileX
	 * @param tileY
	 * @param volume
	 */
	public int addFluidColumn(final int tileX, final int tileY, final int volume, final int worldId) {
		final FluidColumn column = new FluidColumn(tileX, tileY, volume);
		column.setId(Wiring.injector().getInstance(ParameterPersistenceService.class).getParameters().getNextFluidColumnId());

		this.fluids.put(column.getId(), column);
		return column.getId();
	}


	/**
	 * @param id
	 * @return the {@link FluidColumn} with the matching ID
	 */
	public Optional<FluidColumn> getFluid(final int id) {
		if (fluids.containsKey(id)) {
			return Optional.of(fluids.get(id));
		}

		return Optional.absent();
	}


	/**
	 * @return an iterator over all {@link FluidColumn}s
	 */
	public Iterable<FluidColumn> getAllFluids() {
		return fluids.values();
	}
}