package bloodandmithril.character.faction;

import java.util.HashSet;

import com.google.common.collect.Sets;
import com.google.inject.Singleton;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;

/**
 * Service related to the control of factions
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class FactionControlService {

	/**
	 * Indicates which factions are currently under control
	 */
	private final HashSet<Integer> controlledFactions = Sets.newHashSet();


	/**
	 * @param factionId
	 * @return whether or not the faction is under control
	 */
	public boolean isUnderControl(int factionId) {
		return controlledFactions.contains(factionId);
	}


	/**
	 * @param factionId to control
	 */
	public void control(int factionId) {
		controlledFactions.add(factionId);
	}


	/**
	 * @param individual to test
	 * @return whether the individual is currently under control
	 */
	public boolean isControllable(Individual individual) {
		return controlledFactions.contains(individual.getFactionId());
	}


	public HashSet<Integer> getControlledFactions() {
		return controlledFactions;
	}
}
