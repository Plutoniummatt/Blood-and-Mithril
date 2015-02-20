package bloodandmithril.ui.components.window;

import static bloodandmithril.core.BloodAndMithrilClient.HEIGHT;
import static bloodandmithril.core.BloodAndMithrilClient.WIDTH;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Comparator;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;

import bloodandmithril.character.faction.Faction;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.characters.Elf;
import bloodandmithril.character.skill.Skill;
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
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Window for selecting starting units/items
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class NewGameWindow extends Window {

	private Button next;
	private Button startGame = new Button(
		"Start Game",
		defaultFont,
		0,
		0,
		100,
		16,
		() -> {
			setClosing(true);
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

	private Queue<Panel> panels = Lists.newLinkedList();

	private Class<? extends Individual> selectedRace;
	private HashMap<ListingMenuItem<Individual>, String> startingIndividuals = Maps.newHashMap();

	/**
	 * Constructor
	 */
	public NewGameWindow() {
		super(WIDTH/2 - 200, HEIGHT/2 + 150, 400, 300, "New game", true, 400, 300, false, true, false);

		panels.add(new ChooseRacePanel(this));
		panels.add(new ChooseStartingIndividualsPanel(this));

		currentPanel = panels.poll();
		next = new Button(
			"Next",
			defaultFont,
			0,
			0,
			50,
			16,
			() -> {
				currentPanel = panels.poll();
			},
			Color.WHITE,
			Color.GREEN,
			Color.WHITE,
			UIRef.BL
		);
	}


	@Override
	protected void internalWindowRender() {
		currentPanel.x = x;
		currentPanel.y = y;
		currentPanel.width = width;
		currentPanel.height = height;
		currentPanel.render();

		if (selectedRace != null && !startingIndividuals.entrySet().isEmpty()) {
			startGame.render(x + width / 2, y - height + 30, isActive(), getAlpha());
		}

		next.render(x + width / 2, y - height + 30, isActive(), getAlpha());
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		currentPanel.leftClick(copy, windowsCopy);

		if (selectedRace != null && !startingIndividuals.entrySet().isEmpty()) {
			startGame.click();
		}

		next.click();
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



	public class ChooseStartingIndividualsPanel extends Panel {

		private ScrollableListingPanel<Individual, String> individuals;
		private ScrollableListingPanel<Skill, String> skills;
		private Individual selectedIndividual;

		/**
		 * Constructor
		 */
		public ChooseStartingIndividualsPanel(Component parent) {
			super(parent);

			if (!startingIndividuals.keySet().isEmpty()) {
				selectedIndividual = Iterables.get(startingIndividuals.keySet(), 0).t;
			}

			individuals = new ScrollableListingPanel<Individual, String>(
				NewGameWindow.this,
				new Comparator<Individual>() {
					@Override
					public int compare(Individual o1, Individual o2) {
						return o1.getId().getSimpleName().compareTo(o2.getId().getSimpleName());
					}
				},
				false,
				0
			) {
				@Override
				protected String getExtraString(Entry<ListingMenuItem<Individual>, String> item) {
					return "";
				}


				@Override
				protected int getExtraStringOffset() {
					return 0;
				}


				@Override
				protected void populateListings(List<HashMap<ListingMenuItem<Individual>, String>> listings) {
					listings.add(startingIndividuals);
				}


				@Override
				public boolean keyPressed(int keyCode) {
					return false;
				}
			};


			skills = new ScrollableListingPanel<Skill, String>(
				NewGameWindow.this,
				new Comparator<Skill>() {
					@Override
					public int compare(Skill o1, Skill o2) {
						return o1.getName().compareTo(o2.getName());
					}
				},
				false,
				0
			) {
				@Override
				protected String getExtraString(Entry<ScrollableListingPanel.ListingMenuItem<Skill>, String> item) {
					return item.getValue();
				}


				@Override
				protected int getExtraStringOffset() {
					return 0;
				}


				@Override
				protected void populateListings(List<HashMap<ListingMenuItem<Skill>, String>> listings) {
					if (selectedIndividual == null) {
						listings.add(Maps.newHashMap());
					} else {
						HashMap<ListingMenuItem<Skill>, String> newHashMap = Maps.newHashMap();
						for (Skill skill : selectedIndividual.getSkills().getAllSkills()) {
							newHashMap.put(
								new ListingMenuItem<Skill>(
									skill,
									new Button(
										skill.getName(),
										defaultFont,
										0,
										0,
										skill.getName().length() * 10,
										16,
										() -> {
										},
										Color.WHITE,
										Color.GREEN,
										Color.WHITE,
										UIRef.BL
									),
									null
								),
								Integer.toString(skill.getLevel())
							);
						}
						listings.add(newHashMap);
					}
				}


				@Override
				public boolean keyPressed(int keyCode) {
					return false;
				}
			};
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
			individuals.x = x;
			individuals.y = y;
			individuals.width = width/2;
			individuals.height = height;;

			skills.x = x + width/2;
			skills.y = y;
			skills.width = width/2;
			skills.height = height;;

			individuals.render();
			skills.render();
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