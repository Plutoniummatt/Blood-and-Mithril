package bloodandmithril.character.ai.implementations;

import bloodandmithril.character.ai.ArtificialIntelligence;
import bloodandmithril.character.ai.task.LightLightable;
import bloodandmithril.character.ai.task.Wait;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.prop.Lightable;
import bloodandmithril.prop.Prop;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * AI for elves
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class ElfAI extends ArtificialIntelligence {
	private static final long serialVersionUID = -6956695432238102289L;


	/**
	 * Constructor
	 */
	public ElfAI(Individual host) {
		super(host);
	}


	@Override
	protected void determineCurrentTask() {
		if (Util.roll(0.5f)) {
			wander(200f, false);
		} else if (!(getCurrentTask() instanceof Wait) && !(getCurrentTask() instanceof LightLightable)) {
			Individual host = getHost();
			for (Prop prop : Domain.getWorld(host.getWorldId()).getPositionalIndexMap().getNearbyEntities(Prop.class, host.getState().position)) {
				if (prop instanceof Lightable && !((Lightable) prop).isLit()) {
					try {
						setCurrentTask(new LightLightable(host, (Lightable) prop));
					} catch (NoTileFoundException e) {}
				}
			}
		}
	}
}