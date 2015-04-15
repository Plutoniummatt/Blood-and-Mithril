package bloodandmithril.character.proficiency;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

import bloodandmithril.character.proficiency.proficiencies.Smithing;
import bloodandmithril.core.Copyright;


/**
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
@SuppressWarnings("deprecation")
public class TestProficiency {

	/**
	 * Tests that proficiency experience is correctly recalculated when levelling up and down manually
	 */
	@Test
	public void testChangingLevelNormlizesExperience() {
		// Create new proficiency
		Proficiency testProficiency = new Smithing(0);

		// Level up
		// Level should be 1
		// Experience should be 1000
		testProficiency.levelUp();
		assertEquals(1, testProficiency.getLevel());
		assertEquals(1000, testProficiency.getExperience(), 0.001f);

		// Increase experience by 500
		// Level should be 1
		// Experience should be 1500
		testProficiency.increaseExperience(500);
		assertEquals(1, testProficiency.getLevel());
		assertEquals(1500, testProficiency.getExperience(), 0.001f);

		// Level up
		// Level should be 2
		// Experience should be 2414
		testProficiency.levelUp();
		assertEquals(2, testProficiency.getLevel());
		assertEquals(2414, testProficiency.getExperience(), 0.001f);

		// Increase experience by 500
		// Level should be 2
		// Experience should be 2914
		testProficiency.increaseExperience(500);
		assertEquals(2, testProficiency.getLevel());
		assertEquals(2914, testProficiency.getExperience(), 0.001f);

		// Level down
		// Level should be 1
		// Experience should be 2413 (Max XP at given level)
		testProficiency.levelDown();
		assertEquals(1, testProficiency.getLevel());
		assertEquals(2413, testProficiency.getExperience(), 0.001f);
	}


	/**
	 * Tests that proficiencies level up as expected when experience increases
	 */
	@Test
	public void testIncreasingExperience() {
		// Create new proficiency
		Proficiency testProficiency = new Smithing(0);

		// Assert experience is zero
		assertEquals(0, testProficiency.getExperience(), 0.001f);

		// Increase experience by 500
		// Assert level is still 0 (Required to level-up to 1 is 1000)
		// Experience should be 500
		testProficiency.increaseExperience(500);
		assertEquals(500, testProficiency.getExperience(), 0.001f);
		assertEquals(0, testProficiency.getLevel());

		// Increase experience by 500
		// Assert level is 1 (Required to level-up to 1 is 1000)
		// Experience should be 1000
		testProficiency.increaseExperience(500);
		assertEquals(1000, testProficiency.getExperience(), 0.001f);
		assertEquals(1, testProficiency.getLevel());

		// Increase experience by 1413
		// Assert level is 1 (Required to level-up to 2 is 1414)
		// Experience should be 2413
		testProficiency.increaseExperience(1413);
		assertEquals(2413, testProficiency.getExperience(), 0.001f);
		assertEquals(1, testProficiency.getLevel());

		// Increase experience by 1
		// Assert level is 1 (Required to level-up to 2 is 1414)
		// Experience should be 2414
		testProficiency.increaseExperience(1);
		assertEquals(2414, testProficiency.getExperience(), 0.001f);
		assertEquals(2, testProficiency.getLevel());

		// Increase experience by a lot, enough to reach max level
		// Assert level is 100 (Max)
		// Experience should be a lot
		testProficiency.increaseExperience(10000000);
		assertEquals(10002414, testProficiency.getExperience(), 0.001f);
		assertEquals(100, testProficiency.getLevel());
	}
}