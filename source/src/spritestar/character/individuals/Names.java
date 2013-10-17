package spritestar.character.individuals;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import com.badlogic.gdx.Gdx;

import spritestar.character.Individual.IndividualIdentifier;
import spritestar.world.Epoch;
import spritestar.world.WorldState;

/**
 * Giant list of names to use.
 *
 * @author Matt
 */
public class Names {
	
	private static ArrayList<String> elfMale = new ArrayList<String>();
	private static ArrayList<String> elfFemale = new ArrayList<String>();
	private static ArrayList<String> elfLast = new ArrayList<String>();
	
	private static ArrayList<String> humanMale = new ArrayList<String>();
	private static ArrayList<String> humanFemale = new ArrayList<String>();
	private static ArrayList<String> humanLast = new ArrayList<String>();
	
	private static ArrayList<String> dwarfMale = new ArrayList<String>();
	private static ArrayList<String> dwarfFemale = new ArrayList<String>();
	private static ArrayList<String> dwarfLast = new ArrayList<String>();
	
	private static Random random = new Random();
	
	private static boolean setup = false;
	
	public static IndividualIdentifier getRandomElfIdentifier(boolean female, int age) {
		setup();
		String first;
		String last;
		
		if (female) {
			Collections.shuffle(elfFemale);
			first = elfFemale.get(0);
		} else {
			Collections.shuffle(elfMale);
			first = elfMale.get(0);
		}
		Collections.shuffle(elfLast);
		last = elfLast.get(0);
		
		return new IndividualIdentifier(first, last, new Epoch(24f * random.nextFloat(), random.nextInt(31), random.nextInt(13), WorldState.currentEpoch.year - age));
	}
	
	
	public static IndividualIdentifier getRandomDwarfIdentifier(boolean female, int age) {
		setup();
		String first;
		String last;
		
		if (female) {
			Collections.shuffle(dwarfFemale);
			first = dwarfFemale.get(0);
		} else {
			Collections.shuffle(dwarfMale);
			first = dwarfMale.get(0);
		}
		Collections.shuffle(dwarfLast);
		last = dwarfLast.get(0);
		
		return new IndividualIdentifier(first, last, new Epoch(24f * random.nextFloat(), random.nextInt(31), random.nextInt(13), WorldState.currentEpoch.year - age));
	}
	

	/** Populates the lists */
	private static void setup() {
		if (setup) {
			return;
		}
		
		loadList("elfMale", elfMale);
		loadList("elfFemale", elfFemale);
		loadList("elfLast", elfLast);
		
		loadList("dwarfMale", dwarfMale);
		loadList("dwarfFemale", dwarfFemale);
		loadList("dwarfLast", dwarfLast);
		
		loadList("humanMale", humanMale);
		loadList("humanFemale", humanFemale);
		loadList("humanLast", humanLast);
		
		setup = true;
	}
	
	
	/** Loads a single list of names from disk */
	private static void loadList(String name, ArrayList<String> list) {
		BufferedReader br = new BufferedReader(Gdx.files.internal("data/general/names/" + name + ".txt").reader());
		String next;
		
		try {
			next = br.readLine();
		} catch (IOException e) {
			throw new RuntimeException("Got an exception here");
		}
		
		while(next != null) {
			list.add(next);
			try {
				next = br.readLine();
			} catch (IOException e) {
				throw new RuntimeException("Got an exception here");
			}
		}
	}
}
