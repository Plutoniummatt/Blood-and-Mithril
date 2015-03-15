package bloodandmithril.ui.components.window;

import static bloodandmithril.character.individuals.Names.getRandomElfIdentifier;
import static bloodandmithril.core.BloodAndMithrilClient.HEIGHT;
import static bloodandmithril.core.BloodAndMithrilClient.WIDTH;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.util.Fonts.defaultFont;
import static bloodandmithril.util.Util.Colors.lightColor;
import static bloodandmithril.util.Util.Colors.lightSkinColor;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Iterables.transform;

import java.util.ArrayList;
import java.util.Collections;
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
import bloodandmithril.character.proficiency.Proficiency;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Description;
import bloodandmithril.core.ItemPackage;
import bloodandmithril.core.Name;
import bloodandmithril.generation.Structures;
import bloodandmithril.generation.superstructure.SuperStructure;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.persistence.GameLoader;
import bloodandmithril.persistence.GameSaver.PersistenceMetaData;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.persistence.world.ChunkLoader;
import bloodandmithril.ui.UserInterface;
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
import bloodandmithril.util.cursorboundtask.ChooseStartingLocationCursorBoundTask;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Window for selecting starting units/items
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class NewGameWindow extends Window {

	private Panel currentPanel;
	private Queue<Panel> panels = Lists.newLinkedList();

	private Class<? extends Individual> selectedRace;
	private final HashMap<ListingMenuItem<Individual>, String> startingIndividuals = Maps.newHashMap();
	private ItemPackage selectedItemPackage;

	private Button next;
	private Button startGame = new Button(
		"Start Game",
		defaultFont,
		0,
		0,
		100,
		16,
		() -> {
			startGame();
		},
		Color.WHITE,
		Color.GREEN,
		Color.WHITE,
		UIRef.BL
	);

	private void startGame() {
		setClosing(true);

		Faction nature = new Faction(
			"Nature",
			ParameterPersistenceService.getParameters().getNextFactionId(),
			false,
			"Mother nature"
		);

		Faction playerFaction = new Faction(
			selectedRace.getAnnotation(Name.class).name(),
			ParameterPersistenceService.getParameters().getNextFactionId(),
			true,
			selectedRace.getAnnotation(Description.class).description()
		);

		Domain.getFactions().put(nature.factionId, nature);
		Domain.getFactions().put(playerFaction.factionId, playerFaction);

		ClientServerInterface.setServer(true);
		BloodAndMithrilClient.clientCSIThread.execute(() -> {
			UserInterface.closeAllWindows();
			BloodAndMithrilClient.threadWait(1000);
			BloodAndMithrilClient.setLoading(true);
			
			GameLoader.load(new PersistenceMetaData("New game - " + new Date().toString()), true);
			BloodAndMithrilClient.domain = new Domain();
			BloodAndMithrilClient.setup();
			BloodAndMithrilClient.controlledFactions.add(playerFaction.factionId);

			Topography topography = Domain.getActiveWorld().getTopography();
			topography.loadOrGenerateChunk(0, 0, false);

			SuperStructure superStructure = null;
			while (superStructure == null || superStructure.getPossibleStartingLocations().isEmpty()) {
				BloodAndMithrilClient.threadWait(1000);

				superStructure = (SuperStructure) Iterables.tryFind(Structures.getStructures().values(), structure -> {
					return structure instanceof SuperStructure;
				}).orNull();
			}

			ArrayList<Vector2> startingLocations = Lists.newArrayList(superStructure.getPossibleStartingLocations());
			Collections.shuffle(startingLocations);
			Vector2 startingPosition = startingLocations.get(0);

			BloodAndMithrilClient.cam.position.x = startingPosition.x;
			BloodAndMithrilClient.cam.position.y = startingPosition.y;

			BloodAndMithrilClient.setCursorBoundTask(
				new ChooseStartingLocationCursorBoundTask(
					Sets.newHashSet(filter(Lists.newArrayList(transform(startingIndividuals.keySet(), listingMenuItem -> {
						return listingMenuItem.t;
					})), test -> {
						return test != null;
					})),
					selectedItemPackage,
					playerFaction.factionId
				)
			);
			
			while(!ChunkLoader.loaderTasks.isEmpty()) {
				BloodAndMithrilClient.threadWait(100);
			}
			
			BloodAndMithrilClient.setLoading(false);
		});
	}

	/**
	 * Constructor
	 */
	public NewGameWindow() {
		super(WIDTH/2 - 350, HEIGHT/2 + 250, 700, 500, "New game", true, 700, 500, false, true, false);
		panels.add(new ChooseRacePanel(this));
		panels.add(new ChooseStartingIndividualsPanel(this));
		panels.add(new ChooseStartingItemPackagePanel(this));

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
	public boolean scrolled(int amount) {
		return currentPanel.scrolled(amount);
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
			startGame.render(x + width / 2, y - height + 30, canNext() && isActive(), getAlpha());
		} else {
			next.render(x + width / 2, y - height + 30, canNext() && isActive(), getAlpha());
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
		} else if (currentPanel instanceof ChooseStartingItemPackagePanel) {
			return selectedItemPackage != null;
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


	public class ChooseStartingItemPackagePanel extends Panel {

		private ScrollableListingPanel<ItemPackage, String> itemPackages;

		public ChooseStartingItemPackagePanel(Component parent) {
			super(parent);

			itemPackages = new ScrollableListingPanel<ItemPackage, String>(
				NewGameWindow.this,
				new Comparator<ItemPackage>() {
					@Override
					public int compare(ItemPackage o1, ItemPackage o2) {
						return o1.getName().compareTo(o2.getName());
					}
				},
				false,
				0
			) {

				@Override
				protected String getExtraString(Entry<ListingMenuItem<ItemPackage>, String> item) {
					return "";
				}


				@Override
				protected int getExtraStringOffset() {
					return 0;

				}

				@Override
				protected void populateListings(List<HashMap<ListingMenuItem<ItemPackage>, String>> listings) {
					HashMap<ListingMenuItem<ItemPackage>, String> newHashMap = Maps.newHashMap();

					for (ItemPackage pack : ItemPackage.getAvailablePackages()) {
						ContextMenu.MenuItem select = new ContextMenu.MenuItem(
							"Select",
							() -> {
								NewGameWindow.this.selectedItemPackage = pack;
								for (HashMap<ListingMenuItem<ItemPackage>, String> item : itemPackages.getListing()) {
									for (ListingMenuItem<ItemPackage> listingItem : item.keySet()) {
										if (listingItem.t == selectedItemPackage) {
											listingItem.button.setIdleColor(Color.CYAN);
										} else {
											if (listingItem.t.isDefault()) {
												listingItem.button.setIdleColor(Color.PURPLE);
											} else {
												listingItem.button.setIdleColor(Color.ORANGE);
											}
										}
									}
								}
							},
							Color.ORANGE,
							Color.WHITE,
							Color.GREEN,
							null
						);

						ContextMenu.MenuItem inspect = new ContextMenu.MenuItem(
							"Inspect",
							() -> {
								UserInterface.addLayeredComponentUnique(
									new ContainerInspectionWindow(pack.getContainer(), pack.getName())
								);
							},
							Color.ORANGE,
							Color.WHITE,
							Color.GREEN,
							null
						);

						newHashMap.put(
							new ListingMenuItem<ItemPackage>(
								pack,
								new Button(
									pack.getName(),
									defaultFont,
									0,
									0,
									pack.getName().length() * 10,
									16,
									() -> {
									},
									pack.isDefault() ? Color.PURPLE : Color.ORANGE,
									Color.WHITE,
									Color.GREEN,
									UIRef.BL
								),
								new ContextMenu(
									getMouseScreenX(),
									getMouseScreenY(),
									true,
									select,
									inspect
								)
							),
							""
						);
					}

					listings.add(newHashMap);
				}


				@Override
				public boolean keyPressed(int keyCode) {
					return false;
				}
			};
		}


		@Override
		public boolean scrolled(int amount) {
			return itemPackages.scrolled(amount);
		}


		@Override
		public boolean leftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
			return itemPackages.leftClick(copy, windowsCopy);
		}


		@Override
		public void leftClickReleased() {
			itemPackages.leftClickReleased();
		}


		@Override
		public void render() {
			itemPackages.x = x + 10;
			itemPackages.y = y - 120;
			itemPackages.width = width - 10;
			itemPackages.height = height - 120;

			itemPackages.render();

			spriteBatch.setShader(Shaders.text);
			defaultFont.setColor(Colors.modulateAlpha(Color.GREEN, parent.getAlpha() * (parent.isActive() ? 1.0f : 0.6f)));
			defaultFont.draw(spriteBatch, "Choose starting item packages", x + width / 2 - 150, y - 40);
			defaultFont.drawWrapped(spriteBatch, "Item packages can be created in-game by placing desired items into a container, then shipping the container.", x + 10, y - 70, width - 20);
		}


		@Override
		public boolean keyPressed(int keyCode) {
			return false;
		}
	}


	public class ChooseStartingIndividualsPanel extends Panel {

		private ScrollableListingPanel<Individual, String> individuals;
		private ScrollableListingPanel<Proficiency, String> skills;
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


			skills = new ScrollableListingPanel<Proficiency, String>(
				NewGameWindow.this,
				new Comparator<Proficiency>() {
					@Override
					public int compare(Proficiency o1, Proficiency o2) {
						return o1.getName().compareTo(o2.getName());
					}
				},
				false,
				0
			) {
				@Override
				protected String getExtraString(Entry<ScrollableListingPanel.ListingMenuItem<Proficiency>, String> item) {
					return item.getValue();
				}


				@Override
				protected int getExtraStringOffset() {
					return 50;
				}


				@Override
				protected void populateListings(List<HashMap<ListingMenuItem<Proficiency>, String>> listings) {
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

		@Override
		public boolean scrolled(int amount) {
			return skills.scrolled(amount) || individuals.scrolled(amount);
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
						for (Proficiency skill : listingItem.t.getSkills().getAllProficiencies()) {
							assignablePoints += skill.getLevel();
						}
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
			HashMap<ListingMenuItem<Proficiency>, String> newHashMap = Maps.newHashMap();
			if (selectedIndividual != null) {
				for (Proficiency skill : selectedIndividual.getSkills().getAllProficiencies()) {
					final ListingMenuItem<Proficiency> item = new ListingMenuItem<Proficiency>(
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
			if (selectedRace != null) {
				return clazz.getAnnotation(Description.class).description();
			}

			return "Select a race...";
		}


		@Override
		public boolean keyPressed(int keyCode) {
			return false;
		}
	}
}
