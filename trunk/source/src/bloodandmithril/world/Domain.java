package bloodandmithril.world;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import bloodandmithril.character.faction.Faction;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.generation.ChunkGenerator;
import bloodandmithril.generation.biome.DefaultBiomeDecider;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.window.UnitsWindow;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Class representing the entire domain governing the game.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Domain {

	/** The current active {@link World} */
	private static int activeWorldId;

	/** {@link World}s */
	private static HashMap<Integer, World> 						worlds 					= newHashMap();

	/** {@link Individual} that are selected for manual control */
	private static Set<Integer> 								selectedIndividuals 	= newHashSet();

	/** Every {@link Individual} that exists */
	private static ConcurrentHashMap<Integer, Individual> 		individuals 			= new ConcurrentHashMap<>();

	/** Every {@link Prop} that exists */
	private static ConcurrentHashMap<Integer, Faction> 			factions 				= new ConcurrentHashMap<>();


	public static int createWorld() {
		World world = new World(1200f, new Epoch(15.5f, 15, 4, 2015), new ChunkGenerator(new DefaultBiomeDecider()));
		getWorlds().put(world.getWorldId(), world);

		return world.getWorldId();
	}


	public synchronized static void addSelectedIndividual(Individual individual) {
		selectedIndividuals.add(individual.getId().getId());
	}


	public synchronized static boolean removeSelectedIndividual(Individual individual) {
		return selectedIndividuals.removeIf(id -> {
			return individual.getId().getId() == id;
		});
	}


	public synchronized static boolean removeSelectedIndividualIf(java.util.function.Predicate<Integer> predicate) {
		return selectedIndividuals.removeIf(predicate);
	}


	public synchronized static boolean isIndividualSelected(Individual individual) {
		return selectedIndividuals.contains(individual.getId().getId());
	}


	public synchronized static void clearSelectedIndividuals() {
		selectedIndividuals.clear();
	}


	public synchronized static Collection<Individual> getSelectedIndividuals() {
		return Lists.newLinkedList(Iterables.transform(selectedIndividuals, id -> {
			return getIndividual(id);
		}));
	}


	public static World getActiveWorld() {
		return getWorld(activeWorldId);
	}


	public static int getActiveWorldId() {
		return activeWorldId;
	}


	public static void setActiveWorld(int worldId) {
		activeWorldId = worldId;
	}


	public static World getWorld(int id) {
		return getWorlds().get(id);
	}


	public static HashMap<Integer, World> getWorlds() {
		return worlds;
	}


	public static void setWorlds(HashMap<Integer, World> worlds) {
		Domain.worlds = worlds;
	}


	public static ConcurrentHashMap<Integer, Individual> getIndividuals() {
		return individuals;
	}


	public static List<Individual> getSortedIndividuals(Comparator<Individual> sorter) {
		LinkedList<Individual> sorted = Lists.newLinkedList(individuals.values());
		Collections.sort(sorted, sorter);
		return sorted;
	}


	/**
	 * @return an {@link Individual} with the specified key
	 */
	public static Individual getIndividual(int key) {
		return individuals.get(key);
	}


	public static void addIndividual(Individual indi, int worldId) {
		indi.setWorldId(worldId);
		indi.getId().getBirthday().year = Domain.getWorld(worldId).getEpoch().year;
		individuals.put(indi.getId().getId(), indi);
		Domain.getWorld(worldId).getIndividuals().add(indi.getId().getId());
		if (ClientServerInterface.isClient()) {
			for (Component component : UserInterface.getLayeredComponents()) {
				if (component instanceof UnitsWindow) {
					((UnitsWindow) component).refresh();
				}
			}
		} else {
			ClientServerInterface.SendNotification.notifyRefreshWindows();
		}
	}


	public static void setIndividuals(ConcurrentHashMap<Integer, Individual> individuals) {
		Domain.individuals = individuals;
	}


	public static ConcurrentHashMap<Integer, Faction> getFactions() {
		return factions;
	}


	public static void setFactions(ConcurrentHashMap<Integer, Faction> factions) {
		Domain.factions = factions;
	}


	public static void setup() {
	}
}