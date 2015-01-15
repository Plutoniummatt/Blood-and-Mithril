package bloodandmithril.character.ai.implementations;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import bloodandmithril.character.Speech;
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

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

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
			List<Prop> nearbyEntities = Lists.newLinkedList(Domain.getWorld(host.getWorldId()).getPositionalIndexMap().getNearbyEntities(Prop.class, host.getState().position));
			Collections.shuffle(nearbyEntities);
			LinkedList<Prop> lightables = Lists.newLinkedList(Collections2.filter(nearbyEntities, prop -> {
				return prop instanceof Lightable && !((Lightable) prop).isLit();
			}));
			if (!lightables.isEmpty()) {
				try {
					setCurrentTask(new LightLightable(host, (Lightable) lightables.get(0), true));
				} catch (NoTileFoundException e) {}
			}
		}

		if (Util.roll(0.0005f)) {
			getHost().speak(Speech.getRandomIdleSpeech(), 2500);
		}
	}
}