package bloodandmithril.character.ai.task;

import static bloodandmithril.networking.ClientServerInterface.isClient;
import static bloodandmithril.networking.ClientServerInterface.isServer;

import java.util.Map;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.LiquidContainer;
import bloodandmithril.item.liquid.Liquid;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.fluid.Fluid;

import com.badlogic.gdx.math.Vector2;

@Copyright("Matthew Peck 2014")
public class DiscardLiquid extends CompositeAITask {
	private static final long serialVersionUID = -7380303651201784970L;
	private LiquidContainer container;
	private float amount;
	private Vector2 location;

	/**
	 * Constructor
	 */
	public DiscardLiquid(Individual host, int worldX, int worldY, LiquidContainer container, float amount) {
		super(
			host.getId(),
			"Discarding liquid",
			new GoToLocation(
				host,
				new WayPoint(new Vector2(worldX, worldY), 32),
				false,
				32f,
				true
			)
		);
		this.location = new Vector2(worldX, worldY);
		this.container = container.clone();
		this.amount = amount;

		appendTask(new Discard(hostId));
	}


	public class Discard extends AITask {
		private static final long serialVersionUID = -6979881655486422216L;
		private boolean emptied = false;

		/**
		 * Constructor
		 */
		public Discard(IndividualIdentifier hostId) {
			super(hostId);
		}


		@Override
		public String getDescription() {
			return "Discarding liquid";
		}


		@Override
		public boolean isComplete() {
			return emptied;
		}


		@Override
		public boolean uponCompletion() {
			return false;
		}


		@Override
		public void execute(float delta) {
			Individual individual = Domain.getIndividuals().get(hostId.getId());
			if (individual.getInteractionBox().isWithinBox(location) && !emptied) {
				for (Item item : individual.getInventory().keySet()) {
					if (item.sameAs(container)) {
						individual.takeItem(container);
						LiquidContainer newBottle = container.clone();
						Map<Class<? extends Liquid>, Float> subtracted = newBottle.subtract(amount);
						individual.giveItem(newBottle);
						Domain.getWorld(individual.getWorldId()).getTopography().getFluids().put(
							Topography.convertToWorldTileCoord(location.x),
							Topography.convertToWorldTileCoord(location.y),
							new Fluid(subtracted)
						);

						if (isServer()) {
							if (isClient()) {
								UserInterface.refreshRefreshableWindows();
							} else {
								ClientServerInterface.SendNotification.notifyRefreshWindows();
							}
						}

						emptied = true;
						break;
					}
				}
			}
		}
	}
}