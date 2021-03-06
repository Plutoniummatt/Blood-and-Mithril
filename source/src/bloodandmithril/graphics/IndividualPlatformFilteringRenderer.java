package bloodandmithril.graphics;

import static bloodandmithril.util.Logger.generalDebug;
import static bloodandmithril.world.topography.Topography.TILE_SIZE;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.Predicate;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.faction.FactionControlService;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.offhand.Torch;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * Class to encapsulate the rendering of {@link Individual}s
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class IndividualPlatformFilteringRenderer {

	@Inject private FactionControlService factionControlService;
	@Inject private Graphics graphics;

	/** {@link Predicate} for filtering out those that are NOT on platforms */
	private Predicate<Individual> onPlatform = new Predicate<Individual>() {
		@Override
		public boolean test(final Individual individual) {
			try {
				if (Domain.getWorld(individual.getWorldId()).getTopography().getTile(individual.getState().position.x, individual.getState().position.y - TILE_SIZE/2, true).isPlatformTile ||
					Domain.getWorld(individual.getWorldId()).getTopography().getTile(individual.getState().position.x, individual.getState().position.y - 3 * TILE_SIZE/2, true).isPlatformTile) {
					return true;
				} else {
					return false;
				}
			} catch (final NoTileFoundException e) {
				return false;
			}

		};
	};

	/** {@link Predicate} for filtering out those that ARE on platforms */
	private Predicate<Individual> offPlatform = onPlatform.negate();

	private Comparator<Individual> renderPrioritySorter = (i1, i2) -> {
		return Integer.compare(getIndividualRenderPriority(i1), getIndividualRenderPriority(i2));
	};


	/** Renders all individuals, ones that are on platforms are rendered first */
	public void renderIndividuals(final int worldId) {
		try {
			Collection<Integer> onScreenEntities = Domain.getWorld(worldId).getPositionalIndexMap().getOnScreenEntities(Individual.class, graphics);
			
			onScreenEntities
			.stream()
			.map(id -> Domain.getIndividual(id))
			.sorted(renderPrioritySorter)
			.filter(offPlatform)
			.forEach(individual -> {
				IndividualRenderer.render(individual, graphics);
			});
			
			onScreenEntities
			.stream()
			.map(id -> Domain.getIndividual(id))
			.sorted(renderPrioritySorter)
			.filter(onPlatform)
			.forEach(individual -> {
				IndividualRenderer.render(individual, graphics);
			});
		} catch (final NullPointerException e) {
			generalDebug("Nullpointer whilst rendering individual", LogLevel.OVERRIDE, e);
		}
	}


	private int getIndividualRenderPriority(final Individual individual) {
		for (final Item equipped : individual.getEquipped().keySet()) {
			if (equipped instanceof Torch) {
				return 2;
			}
		}

		return factionControlService.isControllable(individual) ? 1 : 0;
	}
}