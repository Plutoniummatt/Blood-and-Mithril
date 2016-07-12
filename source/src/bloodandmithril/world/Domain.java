package bloodandmithril.world;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

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
		final World world = new World(1200f, new Epoch(15.5f, 15, 4, 2015), new ChunkGenerator(new DefaultBiomeDecider()));
		getWorlds().put(world.getWorldId(), world);

		final World world2 = new World(1200f, new Epoch(15.5f, 15, 4, 2015), new ChunkGenerator(new DefaultBiomeDecider()));
		getWorlds().put(world2.getWorldId(), world2);

		return world.getWorldId();
	}


	public static World getWorld(final int id) {
		return getWorlds().get(id);
	}


	public static HashMap<Integer, World> getWorlds() {
		return worlds;
	}


	public static void setWorlds(final HashMap<Integer, World> worlds) {
		Domain.worlds = worlds;
	}


	public static ConcurrentHashMap<Integer, Individual> getIndividuals() {
		return individuals;
	}


	public static List<Individual> getSortedIndividualsForWorld(final Comparator<Individual> sorter, final int worldId) {
		final LinkedList<Individual> sorted = Lists.newLinkedList(Collections2.filter(individuals.values(), indi -> {return indi.getWorldId() == worldId;}));
		Collections.sort(sorted, sorter);
		return sorted;
	}


	/**
	 * @return an {@link Individual} with the specified key
	 */
	public static Individual getIndividual(final int key) {
		final Individual individual = individuals.get(key);
		return individual;
	}


	public static void addIndividual(final Individual indi, final int worldId) {
		indi.setWorldId(worldId);
		indi.getId().getBirthday().year = Domain.getWorld(worldId).getEpoch().year;
		individuals.put(indi.getId().getId(), indi);
		Domain.getWorld(worldId).getIndividuals().add(indi.getId().getId());
		if (ClientServerInterface.isClient()) {
			for (final Component component : UserInterface.getLayeredComponents()) {
				if (component instanceof UnitsWindow) {
					((UnitsWindow) component).refresh();
				}
			}
		} else {
			ClientServerInterface.SendNotification.notifyRefreshWindows();
		}
	}


	public static void setIndividuals(final ConcurrentHashMap<Integer, Individual> individuals) {
		Domain.individuals = individuals;
	}


	public static ConcurrentHashMap<Integer, Faction> getFactions() {
		return factions;
	}


	public static void setFactions(final ConcurrentHashMap<Integer, Faction> factions) {
		Domain.factions = factions;
	}


	public static void setup() {
	}
}