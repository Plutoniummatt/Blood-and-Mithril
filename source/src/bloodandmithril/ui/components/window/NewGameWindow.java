package bloodandmithril.ui.components.window;

import static bloodandmithril.core.BloodAndMithrilClient.HEIGHT;
import static bloodandmithril.core.BloodAndMithrilClient.WIDTH;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Date;
import java.util.Deque;
import java.util.List;

import bloodandmithril.character.faction.Faction;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.characters.Elf;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.persistence.GameLoader;
import bloodandmithril.persistence.GameSaver.PersistenceMetaData;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.Panel;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
import com.google.common.collect.Lists;

/**
 * Window for selecting starting units/items
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class NewGameWindow extends Window {

	private Button startGame = new Button(
		"Start Game",
		defaultFont,
		0,
		0,
		100,
		16,
		() -> {
			Domain.getFactions().put(0, new Faction("Nature", 0, false, ""));
			Domain.getFactions().put(1, new Faction("Elves", 1, true, "Elves are cool"));
			ClientServerInterface.setServer(true);
			BloodAndMithrilClient.clientCSIThread.execute(() -> {
				MainMenuWindow.removeWindows();
				try {
					Thread.sleep(1000);
				} catch (Exception e) {}
				GameLoader.load(new PersistenceMetaData("New game - " + new Date().toString()), true);
				BloodAndMithrilClient.domain = new Domain();
				MainMenuWindow.connected();
			});
		},
		Color.WHITE,
		Color.GREEN,
		Color.WHITE,
		UIRef.BL
	);

	private Panel currentPanel;

	private ChooseRacePanel chooseRacePanel;
	private Class<? extends Individual> selectedRace;

	/**
	 * Constructor
	 */
	public NewGameWindow() {
		super(WIDTH/2 - 200, HEIGHT/2 + 150, 400, 300, "New game", true, 400, 300, false, true, false);

		chooseRacePanel = new ChooseRacePanel(this);

		currentPanel = chooseRacePanel;
	}


	@Override
	protected void internalWindowRender() {
		chooseRacePanel.x = x;
		chooseRacePanel.y = y;
		chooseRacePanel.width = width;
		chooseRacePanel.height = height;

		currentPanel.render();

		startGame.render(x + width / 2, y - height + 30, isActive(), getAlpha());
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		currentPanel.leftClick(copy, windowsCopy);
		startGame.click();
	}


	@Override
	protected void uponClose() {
	}


	@Override
	public Object getUniqueIdentifier() {
		return hashCode();
	}


	@Override
	public void leftClickReleased() {
	}



	public class ChooseStartingEntitiesPanel extends Panel {

		/**
		 * Constructor
		 */
		public ChooseStartingEntitiesPanel(Component parent) {
			super(parent);
		}


		@Override
		public boolean leftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
			return false;
		}


		@Override
		public void leftClickReleased() {
		}


		@Override
		public void render() {
		}


		@Override
		public boolean keyPressed(int keyCode) {
			return false;
		}
	}


	/**
	 * Panel used to choose starting race
	 */
	public class ChooseRacePanel extends Panel {
		private List<Button> availableRaces = Lists.newLinkedList();

		/**
		 * Constructor
		 */
		@SuppressWarnings("unchecked")
		public ChooseRacePanel(Component parent) {
			super(parent);

			for (Class<? extends Individual> race : Lists.newArrayList(Elf.class)) {
				availableRaces.add(
					new Button(
						race.getSimpleName(),
						Fonts.defaultFont,
						0,
						0,
						race.getSimpleName().length() * 10,
						16,
						() -> {
							selectedRace = race;
						},
						Color.ORANGE,
						Color.WHITE,
						Color.GREEN,
						UIRef.BL
					)
				);
			}
		}


		@Override
		public boolean leftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
			boolean clicked = false;
			for (Button button : availableRaces) {
				clicked = button.click();
				if (clicked) {
					break;
				}
			}

			return clicked;
		}


		@Override
		public void leftClickReleased() {
		}


		@Override
		public void render() {
			defaultFont.setColor(Colors.modulateAlpha(Color.GREEN, parent.getAlpha() * (parent.isActive() ? 1.0f : 0.6f)));
			defaultFont.draw(spriteBatch, "Choose starting race", x + width / 2 - 100, y - 40);

			int i = 0;
			for (Button button : availableRaces) {
				button.render(x + width / 2, y - 75 - i * 18, parent.isActive(), parent.getAlpha());
				i++;
			}

			defaultFont.setColor(Colors.modulateAlpha(Color.GREEN, parent.getAlpha() * (parent.isActive() ? 1.0f : 0.6f)));
			defaultFont.drawWrapped(spriteBatch, deriveDescription(selectedRace), x + 10 , y - 120, width - 20);
		}


		private String deriveDescription(Class<? extends Individual> clazz) {
			if (Elf.class.equals(clazz)) {
				return "Elves are children of nature, they are nimble creatures with a good grip on magic and excel at archery.";
			}

			return "Select a race...";
		}


		@Override
		public boolean keyPressed(int keyCode) {
			return false;
		}
	}
}