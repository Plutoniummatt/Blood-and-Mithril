package bloodandmithril.persistence;

import org.junit.Assert;
import org.junit.Test;

import bloodandmithril.server.BloodAndMithrilServer;

import com.badlogic.gdx.Input;

/**
 * Unit test to test game saving.
 *
 * @author Matt
 */
public class TestSaveAndLoadGame {

	@Test
	public void testSaveGame() throws Exception {
		// Launch the game.
		BloodAndMithrilServer.main(new String[0]);
		
		// Wait for main menu to load.
		Thread.sleep(5000);
		
		try {
			BloodAndMithrilServer.server.keyDown(Input.Keys.K);
			Thread.sleep(10000);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Got an exception during game save");
		}
	}
}