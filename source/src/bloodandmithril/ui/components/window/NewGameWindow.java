package bloodandmithril.ui.components.window;

import static bloodandmithril.character.individuals.Names.getRandomElfIdentifier;
import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.control.InputUtilities.isKeyPressed;
import static bloodandmithril.util.Fonts.defaultFont;
import static bloodandmithril.util.Util.Colors.lightColor;
import static bloodandmithril.util.Util.Colors.lightSkinColor;

import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import bloodandmithril.character.faction.Faction;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.character.individuals.IndividualState;
import bloodandmithril.character.individuals.characters.Elf;
import bloodandmithril.character.proficiency.Proficiency;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Description;
import bloodandmithril.core.ItemPackage;
import bloodandmithril.core.StartGameService;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
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
	private boolean enableTutorials = true;

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

	@Inject	private StartGameService startGameService;
	@Inject	private UserInterface userInterface;

	private void startGame() {
		setClosing(true);
		startGameService.start(selectedRace, selectedItemPackage, startingIndividuals);
	}

	/**
	 * Constructor
	 */
	public NewGameWindow() {
		super(700, 500, "New game", true, 700, 500, false, true, false);
		panels.add(new ChooseRacePanel(this));
		panels.add(new ChooseStartingIndividualsPanel(this));
		panels.add(new ChooseStartingItemPackagePanel(this));
		panels.add(new ChooseTutorialsPanel(this));

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

		Wiring.injector().injectMembers(this);
	}


	@Override
	public boolean scrolled(final int amount) {
		return currentPanel.scrolled(amount);
	}


	@Override
	protected void internalWindowRender(final Graphics graphics) {
		if (currentPanel != null) {
			currentPanel.x = x;
			currentPanel.y = y;
			currentPanel.width = width;
			currentPanel.height = height;
			currentPanel.render(graphics);
		}

		if (panels.isEmpty()) {
			startGame.render(x + width / 2, y - height + 30, canNext() && isActive(), getAlpha(), graphics);
		} else {
			next.render(x + width / 2, y - height + 30, canNext() && isActive(), getAlpha(), graphics);
		}
	}


	@Override
	protected void internalLeftClick(final List<ContextMenu> copy, final Deque<Component> windowsCopy) {
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
		} else if (currentPanel instanceof ChooseTutorialsPanel) {
			return true;
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

		public ChooseStartingItemPackagePanel(final Component parent) {
			super(parent);

			itemPackages = new ScrollableListingPanel<ItemPackage, String>(
				NewGameWindow.this,
				new Comparator<ItemPackage>() {
					@Override
					public int compare(final ItemPackage o1, final ItemPackage o2) {
						return o1.getName().compareTo(o2.getName());
					}
				},
				false,
				0,
				null
			) {

				@Override
				protected String getExtraString(final Entry<ListingMenuItem<ItemPackage>, String> item) {
					return "";
				}


				@Override
				protected int getExtraStringOffset() {
					return 0;

				}

				@Override
				protected void populateListings(final List<HashMap<ListingMenuItem<ItemPackage>, String>> listings) {
					final HashMap<ListingMenuItem<ItemPackage>, String> newHashMap = Maps.newHashMap();

					for (final ItemPackage pack : ItemPackage.getAvailablePackages()) {
						final ContextMenu.MenuItem select = new ContextMenu.MenuItem(
							"Select",
							() -> {
								NewGameWindow.this.selectedItemPackage = pack;
								for (final HashMap<ListingMenuItem<ItemPackage>, String> item : itemPackages.getListing()) {
									for (final ListingMenuItem<ItemPackage> listingItem : item.keySet()) {
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

						final ContextMenu.MenuItem inspect = new ContextMenu.MenuItem(
							"Inspect",
							() -> {
								userInterface.addLayeredComponentUnique(
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
								() -> { return new ContextMenu(
									getMouseScreenX(),
									getMouseScreenY(),
									true,
									select,
									inspect
								);}
							),
							""
						);
					}

					listings.add(newHashMap);
				}


				@Override
				public boolean keyPressed(final int keyCode) {
					return false;
				}
			};
		}


		@Override
		public boolean scrolled(final int amount) {
			return itemPackages.scrolled(amount);
		}


		@Override
		public boolean leftClick(final List<ContextMenu> copy, final Deque<Component> windowsCopy) {
			return itemPackages.leftClick(copy, windowsCopy);
		}


		@Override
		public void leftClickReleased() {
			itemPackages.leftClickReleased();
		}


		@Override
		public void render(final Graphics graphics) {
			itemPackages.x = x + 10;
			itemPackages.y = y - 120;
			itemPackages.width = width - 10;
			itemPackages.height = height - 120;

			itemPackages.render(graphics);

			graphics.getSpriteBatch().setShader(Shaders.text);
			defaultFont.setColor(Colors.modulateAlpha(Color.GREEN, parent.getAlpha() * (parent.isActive() ? 1.0f : 0.6f)));
			defaultFont.draw(graphics.getSpriteBatch(), "Choose starting item packages", x + width / 2 - 150, y - 40);
			defaultFont.drawWrapped(graphics.getSpriteBatch(), "Item packages can be created in-game by placing desired items into a container, then shipping the container.", x + 10, y - 70, width - 20);
		}


		@Override
		public boolean keyPressed(final int keyCode) {
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
		public ChooseStartingIndividualsPanel(final Component parent) {
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
					public int compare(final Individual o1, final Individual o2) {
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
				0,
				null
			) {
				@Override
				protected String getExtraString(final Entry<ListingMenuItem<Individual>, String> item) {
					return "";
				}


				@Override
				protected int getExtraStringOffset() {
					return 0;
				}


				@Override
				protected void populateListings(final List<HashMap<ListingMenuItem<Individual>, String>> listings) {
					listings.add(startingIndividuals);
				}


				@Override
				public boolean keyPressed(final int keyCode) {
					return false;
				}
			};


			skills = new ScrollableListingPanel<Proficiency, String>(
				NewGameWindow.this,
				new Comparator<Proficiency>() {
					@Override
					public int compare(final Proficiency o1, final Proficiency o2) {
						return o1.getName().compareTo(o2.getName());
					}
				},
				false,
				0,
				null
			) {
				@Override
				protected String getExtraString(final Entry<ScrollableListingPanel.ListingMenuItem<Proficiency>, String> item) {
					return item.getValue();
				}


				@Override
				protected int getExtraStringOffset() {
					return 50;
				}


				@Override
				protected void populateListings(final List<HashMap<ListingMenuItem<Proficiency>, String>> listings) {
					if (selectedIndividual == null) {
						listings.add(Maps.newHashMap());
					} else {
						refreshSkillListing();
					}
				}


				@Override
				public boolean keyPressed(final int keyCode) {
					return false;
				}
			};
		}

		@Override
		public boolean scrolled(final int amount) {
			return skills.scrolled(amount) || individuals.scrolled(amount);
		}

		private void addIndividual() {
			final Individual newIndividual = newIndividual(selectedRace);
			final ListingMenuItem<Individual> listingItem = new ListingMenuItem<Individual>(
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
					if (isKeyPressed(Keys.CONTROL_LEFT)) {
						if (selectedIndividual == listingItem.t) {
							selectedIndividual = null;
						}
						startingIndividuals.remove(listingItem);
						refreshSkillListing();
						assignablePoints += 10;
						for (final Proficiency skill : listingItem.t.getProficiencies().getAllProficiencies()) {
							assignablePoints += skill.getLevel();
						}
					} else {
						selectedIndividual = newIndividual;
						refreshSkillListing();
						for (final ListingMenuItem<Individual> item : startingIndividuals.keySet()) {
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
			final HashMap<ListingMenuItem<Proficiency>, String> newHashMap = Maps.newHashMap();
			if (selectedIndividual != null) {
				for (final Proficiency skill : selectedIndividual.getProficiencies().getAllProficiencies()) {
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
								if (isKeyPressed(Keys.CONTROL_LEFT)) {
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


		private Individual newIndividual(final Class<? extends Individual> selectedRace) {
			if (selectedRace.equals(Elf.class)) {
				final IndividualState state = new IndividualState.IndividualStateBuilder()
				.withMaxHealth(30f)
				.withHealthRegen(0.01f)
				.withStaminaRegen(0.02f)
				.withMaxMana(0f)
				.withManaRegen(0f).build();

				state.position = new Vector2();
				state.velocity = new Vector2();
				state.acceleration = new Vector2();

				final IndividualIdentifier id = getRandomElfIdentifier(true, 18 + Util.getRandom().nextInt(10));
				id.setNickName("");

				final Elf elf = new Elf(
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
		public boolean leftClick(final List<ContextMenu> copy, final Deque<Component> windowsCopy) {
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
		public void render(final Graphics graphics) {
			individuals.x = x + 10;
			individuals.y = y - 220;
			individuals.width = width/2 - 10;
			individuals.height = height - 220;

			skills.x = x + width/2 + 10;
			skills.y = y - 220;
			skills.width = width/2 - 10;
			skills.height = height - 220;

			individuals.render(graphics);
			skills.render(graphics);

			graphics.getSpriteBatch().setShader(Shaders.text);
			defaultFont.setColor(Colors.modulateAlpha(Color.GREEN, parent.getAlpha() * (parent.isActive() ? 1.0f : 0.6f)));
			defaultFont.draw(graphics.getSpriteBatch(), "Choose starting individuals and skills", x + width / 2 - 170, y - 40);
			defaultFont.draw(graphics.getSpriteBatch(), "Hold LEFT CTRL and click to remove", x + width / 2 - 165, y - 60);

			defaultFont.setColor(Colors.modulateAlpha(Color.ORANGE, parent.getAlpha() * (parent.isActive() ? 1.0f : 0.6f)));
			defaultFont.draw(graphics.getSpriteBatch(), "Points left: " + assignablePoints, x + 10, y - 100);
		}


		@Override
		public boolean keyPressed(final int keyCode) {
			return false;
		}
	}


	public class ChooseTutorialsPanel extends Panel {

		private Button tutorialsButton;

		/**
		 * Constructor
		 */
		public ChooseTutorialsPanel(final Component parent) {
			super(parent);

			tutorialsButton = new Button(
				() -> {
					if (NewGameWindow.this.enableTutorials) {
						return "Tutorials enabled";
					} else {
						return "Tutorials disabled";
					}
				},
				Fonts.defaultFont,
				0,
				0,
				180,
				16,
				() -> {
					NewGameWindow.this.enableTutorials = !NewGameWindow.this.enableTutorials;
				},
				Color.ORANGE,
				Color.WHITE,
				Color.GREEN,
				UIRef.BL
			);
		}


		@Override
		public boolean leftClick(final List<ContextMenu> copy, final Deque<Component> windowsCopy) {
			final boolean clicked = tutorialsButton.click();
			return clicked;
		}


		@Override
		public void leftClickReleased() {
		}


		@Override
		public void render(final Graphics graphics) {
			defaultFont.setColor(Colors.modulateAlpha(Color.GREEN, parent.getAlpha() * (parent.isActive() ? 1.0f : 0.6f)));
			defaultFont.draw(graphics.getSpriteBatch(), "Choose whether or not tutorials are enabled.", x + width / 2 - 220, y - 40);

			tutorialsButton.render(x + width / 2, y - 100, parent.isActive(), parent.getAlpha(), graphics);
		}


		@Override
		public boolean keyPressed(final int keyCode) {
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
		public ChooseRacePanel(final Component parent) {
			super(parent);

			for (final Class<? extends Individual> race : Lists.newArrayList(Elf.class)) {
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
		public boolean leftClick(final List<ContextMenu> copy, final Deque<Component> windowsCopy) {
			boolean clicked = false;
			for (final Button button : availableRaces) {
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
		public void render(final Graphics graphics) {
			defaultFont.setColor(Colors.modulateAlpha(Color.GREEN, parent.getAlpha() * (parent.isActive() ? 1.0f : 0.6f)));
			defaultFont.draw(graphics.getSpriteBatch(), "Choose starting race", x + width / 2 - 100, y - 40);

			int i = 0;
			for (final Button button : availableRaces) {
				button.render(x + width / 2, y - 75 - i * 18, parent.isActive(), parent.getAlpha(), graphics);
				i++;
			}

			defaultFont.setColor(Colors.modulateAlpha(Color.GREEN, parent.getAlpha() * (parent.isActive() ? 1.0f : 0.6f)));
			defaultFont.drawWrapped(graphics.getSpriteBatch(), deriveDescription(selectedRace), x + 10 , y - 120, width - 20);
		}


		private String deriveDescription(final Class<? extends Individual> clazz) {
			if (selectedRace != null) {
				return clazz.getAnnotation(Description.class).description();
			}

			return "Select a race...";
		}


		@Override
		public boolean keyPressed(final int keyCode) {
			return false;
		}
	}
}
