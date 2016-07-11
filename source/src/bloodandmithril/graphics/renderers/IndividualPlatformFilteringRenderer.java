package bloodandmithril.graphics.renderers;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static com.google.common.collect.Iterables.filter;

import java.util.Comparator;

import com.google.common.base.Predicate;
import com.google.inject.Inject;

import bloodandmithril.character.faction.FactionControlService;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.graphics.Renderer;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.offhand.Torch;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * Class to encapsulate the rendering of {@link Individual}s
 *
 * @author Matt
 */
public class IndividualPlatformFilteringRenderer {

	private FactionControlService factionControlService;
	private Graphics graphics;

	private Comparator<Individual> renderPrioritySorter = (i1, i2) -> {
		return Integer.compare(getIndividualRenderPriority(i1), getIndividualRenderPriority(i2));
	};


	/** {@link Predicate} for filtering out those that are NOT on platforms */
	private  Predicate<Individual> onPlatform = new Predicate<Individual>() {
		@Override
		public boolean apply(final Individual individual) {
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
	private  Predicate<Individual> offPlatform = new Predicate<Individual>() {
		@Override
		public boolean apply(final Individual individual) {
			try {
				if (Domain.getWorld(individual.getWorldId()).getTopography().getTile(individual.getState().position.x, individual.getState().position.y - TILE_SIZE/2, true).isPlatformTile ||
					Domain.getWorld(individual.getWorldId()).getTopography().getTile(individual.getState().position.x, individual.getState().position.y - 3 * TILE_SIZE/2, true).isPlatformTile) {
					return false;
				} else {
					return true;
				}
			} catch (final NoTileFoundException e) {
				return false;
			}
		};
	};


	@Inject
	public IndividualPlatformFilteringRenderer(
		final FactionControlService factionControlService,
		final Graphics graphics
	) {
		this.factionControlService = factionControlService;
		this.graphics = graphics;
	}


	/** Renders all individuals, ones that are on platforms are rendered first */
	public void renderIndividuals(final int worldId) {
		try {
			for (final Individual indi : filter(Domain.getSortedIndividualsForWorld(renderPrioritySorter, worldId), offPlatform)) {
				Renderer.render(indi, graphics);
			}

			for (final Individual indi : filter(Domain.getSortedIndividualsForWorld(renderPrioritySorter, worldId), onPlatform)) {
				Renderer.render(indi, graphics);
			}
		} catch (final NullPointerException e) {
			Logger.generalDebug("Nullpointer whilst rendering individual", LogLevel.INFO, e);
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