package bloodandmithril.ui.components.window;

import static bloodandmithril.character.individuals.Names.getRandomElfIdentifier;
import static bloodandmithril.core.BloodAndMithrilClient.HEIGHT;
import static bloodandmithril.core.BloodAndMithrilClient.WIDTH;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.util.Fonts.defaultFont;
import static bloodandmithril.util.Util.Colors.lightColor;
import static bloodandmithril.util.Util.Colors.lightSkinColor;

import java.util.Comparator;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;

import bloodandmithril.character.faction.Faction;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.character.individuals.IndividualState;
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
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
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
	private final HashMap<ListingMenuItem<Individual>, String> startingIndividuals = Maps.newHashMap();

	/**
	 * Constructor
	 */
	public NewGameWindow() {
		super(WIDTH/2 - 350, HEIGHT/2 + 250, 700, 500, "New game", true, 700, 500, false, true, false);

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
		if (currentPanel != null) {
			currentPanel.x = x;
			currentPanel.y = y;
			currentPanel.width = width;
			currentPanel.height = height;
			currentPanel.render();
		}

		if (panels.isEmpty()) {
			startGame.render(x + width / 2, y - height + 30, canNext(), getAlpha());
		} else {
			next.render(x + width / 2, y - height + 30, canNext(), getAlpha());
		}
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		currentPanel.leftClick(copy, windowsCopy);

		if (canNext()) {
			if (panels.isEmpty()) {
				startGame.click();
			} else {
				next.click();
			}
		}
	}


	private boolean canNext() {
		if (currentPanel instanceof ChooseRacePanel) {
			return selectedRace != null;
		} else if (currentPanel instanceof ChooseStartingIndividualsPanel) {
			return startingIndividuals.size() > 1;
		}
		
		throw new IllegalStateException("Unexpected panel");
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
		currentPanel.leftClickReleased();
	}



	public class ChooseStartingIndividualsPanel extends Panel {

		private ScrollableListingPanel<Individual, String> individuals;
		private ScrollableListingPanel<Skill, String> skills;
		private Individual selectedIndividual;
		private int assignablePoints = 150;

		/**
		 * Constructor
		 */
		public ChooseStartingIndividualsPanel(Component parent) {
			super(parent);

			if (!startingIndividuals.keySet().isEmpty()) {
				selectedIndividual = Iterables.get(startingIndividuals.keySet(), 0).t;
			}
			
			startingIndividuals.put(
				new ListingMenuItem<Individual>(
					null, 
					new Button(
						"Add new individual",
						defaultFont,
						0,
						0,
						180,
						16,
						() -> {
							if (assignablePoints < 10) {
								return;
							}
							
							addIndividual();
						},
						Color.GREEN,
						Color.WHITE,
						Color.GREEN,
						UIRef.BL
					), 
					null
				), 
				null
			);
			
			individuals = new ScrollableListingPanel<Individual, String>(
				NewGameWindow.this,
				new Comparator<Individual>() {
					@Override
					public int compare(Individual o1, Individual o2) {
						if (o1 == null) {
							return -1;
						} else if (o2 == null) {
							return 1;
						} else {
							return o1.getId().getSimpleName().compareTo(o2.getId().getSimpleName());
						}
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
					return 50;
				}


				@Override
				protected void populateListings(List<HashMap<ListingMenuItem<Skill>, String>> listings) {
					if (selectedIndividual == null) {
						listings.add(Maps.newHashMap());
					} else {
						refreshSkillListing();
					}
				}


				@Override
				public boolean keyPressed(int keyCode) {
					return false;
				}
			};
		}

		private void addIndividual() {
			Individual newIndividual = newIndividual(selectedRace);
			ListingMenuItem<Individual> listingItem = new ListingMenuItem<Individual>(
				newIndividual, 
				new Button(
					newIndividual.getId().getSimpleName(),
					defaultFont,
					0,
					0,
					newIndividual.getId().getSimpleName().length() * 10,
					16,
					() -> {
					},
					Color.ORANGE,
					Color.WHITE,
					Color.ORANGE,
					UIRef.BL
				), 
				null
			);
			
			listingItem.button.setTask(
				() -> {
					if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) {
						if (selectedIndividual == listingItem.t) {
							selectedIndividual = null;
						}
						startingIndividuals.remove(listingItem);
						refreshSkillListing();
						assignablePoints += 10;
					} else {
						selectedIndividual = newIndividual;
						refreshSkillListing();
						for (ListingMenuItem<Individual> item : startingIndividuals.keySet()) {
							if (item.t == null) {
								item.button.setIdleColor(Color.GREEN);
							} else if (item.t == selectedIndividual) {
								item.button.setIdleColor(Color.CYAN);
							} else {
								item.button.setIdleColor(Color.ORANGE);
							}
						}
					}
				}
			);
			
			startingIndividuals.put(
				listingItem, 
				null
			);
			
			assignablePoints -= 10;
		}
		
		
		private void refreshSkillListing() {
			ChooseStartingIndividualsPanel.this.skills.getListing().clear();
			HashMap<ListingMenuItem<Skill>, String> newHashMap = Maps.newHashMap();
			if (selectedIndividual != null) {
				for (Skill skill : selectedIndividual.getSkills().getAllSkills()) {
					final ListingMenuItem<Skill> item = new ListingMenuItem<Skill>(
						skill,
						new Button(
							skill.getName(),
							defaultFont,
							0,
							0,
							skill.getName().length() * 10,
							16,
							() -> {
								if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) {
									if (skill.getLevel() > 0) {
										skill.levelDown();
										assignablePoints++;
									}
								} else {
									if (skill.getLevel() < 25 && assignablePoints > 0) {
										skill.levelUp();
										assignablePoints--;
									}
								}
								refreshSkillListing();
							},
							Color.WHITE.cpy().sub(new Color(0, skill.getLevel() / 50f, 0, 0)),
							Color.GREEN,
							Color.WHITE,
							UIRef.BL
						),
						null
					);
					
					newHashMap.put(
						item,
						Integer.toString(skill.getLevel())
					);
				}
			}
			ChooseStartingIndividualsPanel.this.skills.getListing().add(newHashMap);
		}


		private Individual newIndividual(Class<? extends Individual> selectedRace) {
			if (selectedRace.equals(Elf.class)) {
				IndividualState state = new IndividualState(30f, 0.01f, 0.02f, 0f, 0f);
				state.position = new Vector2();
				state.velocity = new Vector2();
				state.acceleration = new Vector2();

				IndividualIdentifier id = getRandomElfIdentifier(true, 18 + Util.getRandom().nextInt(10));
				id.setNickName("");

				Elf elf = new Elf(
					id, state, Faction.NPC, true,
					20f,
					null,
					lightColor(),
					lightColor(),
					lightSkinColor()
				);

				return elf;
			}
			
			throw new RuntimeException("Expected to have a race selected");
		}


		@Override
		public boolean leftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
			if (individuals.leftClick(copy, windowsCopy)) {
				return true;
			} else if (skills.leftClick(copy, windowsCopy)) {
				return true;
			}
			return false;
		}


		@Override
		public void leftClickReleased() {
			individuals.leftClickReleased();
			skills.leftClickReleased();
		}


		@Override
		public void render() {
			individuals.x = x + 10;
			individuals.y = y - 220;
			individuals.width = width/2 - 10;
			individuals.height = height - 220;

			skills.x = x + width/2 + 10;
			skills.y = y - 220;
			skills.width = width/2 - 10;
			skills.height = height - 220;

			individuals.render();
			skills.render();
			
			spriteBatch.setShader(Shaders.text);
			defaultFont.setColor(Colors.modulateAlpha(Color.GREEN, parent.getAlpha() * (parent.isActive() ? 1.0f : 0.6f)));
			defaultFont.draw(spriteBatch, "Choose starting individuals and skills", x + width / 2 - 170, y - 40);
			defaultFont.draw(spriteBatch, "Hold LEFT CTRL and click to remove", x + width / 2 - 165, y - 60);
			
			defaultFont.setColor(Colors.modulateAlpha(Color.ORANGE, parent.getAlpha() * (parent.isActive() ? 1.0f : 0.6f)));
			defaultFont.draw(spriteBatch, "Points left: " + assignablePoints, x + 10, y - 100);
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