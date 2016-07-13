package bloodandmithril.world;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import bloodandmithril.character.faction.Faction;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.generation.biome.DefaultBiomeDecider;
import bloodandmithril.prop.Prop;

/**
 * Class representing the entire domain governing the game.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Domain {

	/** {@link World}s */
	private static HashMap<Integer, World> 								worlds 					= newHashMap();

	/** Every {@link Individual} that exists */
	private static ConcurrentHashMap<Integer, Individual> 				individuals 			= new ConcurrentHashMap<>();

	/** Every {@link Prop} that exists */
	private static ConcurrentHashMap<Integer, Faction> 					factions 				= new ConcurrentHashMap<>();


	public static int createWorld() {
		final World world = new World(1200f, new Epoch(15.5f, 15, 4, 2015), DefaultBiomeDecider.class);
		worlds.put(world.getWorldId(), world);
		return world.getWorldId();
	}


	public static World getWorld(final int id) {
		return worlds.get(id);
	}


	/**
	 * @return all global {@link World} IDs
	 */
	public static Set<Integer> getAllWorldIds() {
		return worlds.keySet();
	}


	/**
	 * @return all global {@link World}s
	 */
	public static Collection<World> getAllWorlds() {
		return worlds.values();
	}


	/**
	 * @return the global world map
	 */
	public static HashMap<Integer, World> getWorldMap() {
		return worlds;
	}


	/**
	 * Adds a world to the global world map
	 */
	public static void addWorld(final World world) {
		worlds.put(world.getWorldId(), world);
	}


	/**
	 * @return The global individual map
	 */
	public static ConcurrentHashMap<Integer, Individual> getIndividualsMap() {
		return individuals;
	}


	/**
	 * @return All global individuals
	 */
	public static Collection<Individual> getIndividuals() {
		return individuals.values();
	}


	/**
	 * @return All global individual IDs
	 */
	public static Set<Integer> getIndividualIds() {
		return individuals.keySet();
	}


	/**
	 * @param comparator used to sort the individuals
	 * @param worldId
	 *
	 * @return List of sorted {@link Individual}s given a comparator
	 */
	public static List<Individual> getSortedIndividualsForWorld(final Comparator<Individual> comparator, final int worldId) {
		final LinkedList<Individual> sorted = Lists.newLinkedList(Collections2.filter(individuals.values(), indi -> {return indi.getWorldId() == worldId;}));
		Collections.sort(sorted, comparator);
		return sorted;
	}


	/**
	 * @return an {@link Individual} with the specified key
	 */
	public static Individual getIndividual(final int key) {
		final Individual individual = individuals.get(key);
		return individual;
	}


	/**
	 * Adds an individual to the global individual map
	 */
	public static void addIndividual(final Individual indi) {
		Domain.individuals.put(indi.getId().getId(), indi);
	}


	/**
	 * @return the faction map
	 */
	public static ConcurrentHashMap<Integer, Faction> getFactions() {
		return factions;
	}


	/**
	 * Adds a {@link Faction} to the global faction map
	 */
	public static void addFaction(final Faction faction) {
		factions.put(faction.factionId, faction);
	}


	/**
	 * Gets a {@link Faction} given a faction ID
	 */
	public static Faction getFaction(final int factionId) {
		return factions.get(factionId);
	}
}