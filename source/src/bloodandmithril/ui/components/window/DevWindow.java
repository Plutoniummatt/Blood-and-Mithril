package bloodandmithril.ui.components.window;

import static bloodandmithril.character.individuals.Names.getRandomElfIdentifier;
import static bloodandmithril.character.individuals.Names.getUnknownNatureIdentifier;
import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.control.InputUtilities.getMouseWorldX;
import static bloodandmithril.control.InputUtilities.getMouseWorldY;
import static bloodandmithril.control.InputUtilities.isKeyPressed;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseWorldCoords;
import static bloodandmithril.networking.ClientServerInterface.isServer;
import static bloodandmithril.util.Util.Colors.lightColor;
import static bloodandmithril.util.Util.Colors.lightSkinColor;

import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import bloodandmithril.character.AddIndividualService;
import bloodandmithril.character.faction.Faction;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.character.individuals.IndividualState;
import bloodandmithril.character.individuals.characters.Elf;
import bloodandmithril.character.individuals.characters.Hare;
import bloodandmithril.character.individuals.characters.Wolf;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.MissionTracker;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.GaussianLightingRenderer;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.graphics.particles.DiminishingColorChangingParticle;
import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.graphics.particles.RandomParticle;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.food.plant.CarrotItem;
import bloodandmithril.item.items.material.RockItem;
import bloodandmithril.item.material.mineral.Coal;
import bloodandmithril.item.material.mineral.SandStone;
import bloodandmithril.item.material.wood.StandardWood;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.objectives.tutorial.Tutorial;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.prop.construction.craftingstation.BlacksmithWorkshop;
import bloodandmithril.prop.construction.craftingstation.Campfire;
import bloodandmithril.prop.construction.craftingstation.Furnace;
import bloodandmithril.prop.construction.craftingstation.WorkBench;
import bloodandmithril.prop.furniture.MedievalWallTorchProp;
import bloodandmithril.prop.furniture.RottenWoodenChest;
import bloodandmithril.prop.furniture.WoodenChestProp;
import bloodandmithril.prop.plant.DryGrass;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.RepeatingCountdown;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.tile.tiles.brick.YellowBrickPlatform;
import bloodandmithril.world.topography.tile.tiles.brick.YellowBrickTile;

/**
 * Window containing dev features
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class DevWindow extends Window {

	private final ScrollableListingPanel<String, Object> panel;

	@Inject private GameSaver gameSaver;
	@Inject private MissionTracker missionTracker;
	@Inject private GameClientStateTracker gameClientStateTracker;
	@Inject private AddIndividualService addIndividualService;
	@Inject private UserInterface userInterface;

	/**
	 * Constructor
	 */
	public DevWindow(final int x, final int y, final int length, final int height, final boolean active) {
		super(length, height, "Developer", active, 500, 300, false, true, true);
		Wiring.injector().injectMembers(this);

		panel = new ScrollableListingPanel<String, Object>(this, Comparator.<String>naturalOrder(), false, 35, null) {
			@Override
			protected String getExtraString(final Entry<ListingMenuItem<String>, Object> item) {
				return "";
			}

			@Override
			protected int getExtraStringOffset() {
				return 0;
			}

			@Override
			protected void populateListings(final List<HashMap<ListingMenuItem<String>, Object>> listings) {
				final HashMap<ListingMenuItem<String>, Object> newHashMap = buildMap();

				listings.add(newHashMap);
			}

			@Override
			public boolean keyPressed(final int keyCode) {
				return false;
			}
		};

		panel.setScrollWheelActive(true);
	}


	@Override
	protected void internalWindowRender(final Graphics graphics) {
		panel.x = x;
		panel.y = y;
		panel.width = width;
		panel.height = height;

		panel.render(graphics);

		if (isKeyPressed(Keys.W)) {
			for (int i = 0; i < 2; i++) {
				final long lifetime = Util.getRandom().nextInt(10000);
				final Color randomOneOf = Util.randomOneOf(new Color(0.4f, 0.224f, 0.76f, 1.0f), new Color(0.1f, 0.763f, 0.324f, 1.0f));
				final Vector2 rotate = new Vector2(Util.getRandom().nextFloat() * 200f, 0f).rotate(Util.getRandom().nextFloat() * 360f).add(1000f, 0f);

				gameClientStateTracker.getActiveWorld().getClientParticles().add(new DiminishingColorChangingParticle(
					getMouseWorldCoords(),
					rotate,
					Color.WHITE,
					randomOneOf,
					randomOneOf,
					Util.getRandom().nextFloat() * 3.4f,
					gameClientStateTracker.getActiveWorld().getWorldId(),
					Util.getRandom().nextFloat() * 20f + 10,
					MovementMode.GRAVITY,
					Util.getRandom().nextBoolean() ? Depth.FOREGROUND : Depth.MIDDLEGROUND,
					lifetime,
					true
				).bounce());
			}
		}
	}


	@Override
	protected void internalLeftClick(final List<ContextMenu> copy, final Deque<Component> windowsCopy) {
		panel.leftClick(copy, windowsCopy);
	}


	@Override
	protected void uponClose() {
	}


	@Override
	public boolean scrolled(final int amount) {
		return panel.scrolled(amount);
	}


	@Override
	public boolean keyPressed(final int keyCode) {
		if (super.keyPressed(keyCode)) {
			return true;
		}

		if (keyCode == Keys.I) {
			gameClientStateTracker.getActiveWorld().items().addItem(new CarrotItem(), new Vector2(getMouseWorldX(), getMouseWorldY()), new Vector2());
		}

		if (keyCode == Keys.J) {
			final Set<Integer> keySet = Sets.newHashSet(Domain.getAllWorldIds());
			keySet.remove(gameClientStateTracker.getActiveWorldId());
			gameClientStateTracker.setActiveWorldId(keySet.iterator().next());
		}

		if (keyCode == Keys.B) {
			missionTracker.addMission(new Tutorial(gameClientStateTracker.getActiveWorldId()));
		}

		if (keyCode == Keys.H) {
			for (int i = 0; i < 20; i++) {
				gameClientStateTracker.getActiveWorld().getClientParticles().add(
					new RandomParticle(
						BloodAndMithrilClient.getMouseWorldCoords(),
						new Vector2(),
						Color.WHITE,
						Color.CYAN,
						2f,
						gameClientStateTracker.getActiveWorldId(),
						4f,
						MovementMode.EMBER,
						Depth.FOREGROUND,
						1000 + Util.getRandom().nextInt(2000),
						() -> {
							return new Vector2(150, 0).rotate(Util.getRandom().nextFloat() * 360f);
						},
						new RepeatingCountdown(10)
					)
				);
			}
		}

		if (keyCode == Keys.T) {
			gameClientStateTracker.getActiveWorld().getTopography().changeTile(
				getMouseWorldX(),
				getMouseWorldY(),
				true,
				YellowBrickTile.class
			);
		}

		if (keyCode == Keys.P) {
			gameClientStateTracker.getActiveWorld().getTopography().changeTile(
				getMouseWorldX(),
				getMouseWorldY(),
				true,
				YellowBrickPlatform.class
			);
		}

		if (keyCode == Keys.D) {
			gameClientStateTracker.getActiveWorld().getTopography().deleteTile(
				getMouseWorldX(),
				getMouseWorldY(),
				true,
				false
			);
		}

		if (keyCode == Keys.E) {
			final IndividualState state = new IndividualState.IndividualStateBuilder()
			.withMaxHealth(30f)
			.withHealthRegen(0.01f)
			.withStaminaRegen(0.02f)
			.withMaxMana(0f)
			.withManaRegen(0f).build();


			state.position = new Vector2(getMouseWorldX(), getMouseWorldY());
			state.velocity = new Vector2(0, 0);
			state.acceleration = new Vector2(0, 0);

			final IndividualIdentifier id = getRandomElfIdentifier(true, Util.getRandom().nextInt(100) + 50);
			id.setNickName("Elfie");

			final Elf elf = new Elf(
				id, state, isKeyPressed(Input.Keys.Q) ? Faction.NPC : 2, true,
				gameClientStateTracker.getActiveWorld(),
				lightColor(),
				lightColor(),
				lightSkinColor()
			);

			if (isServer()) {
				addIndividualService.addIndividual(elf, gameClientStateTracker.getActiveWorld().getWorldId());
			} else {
				ClientServerInterface.SendRequest.sendSpawnIndividualRequest(elf);
			}
		}

		if (keyCode == Keys.R) {
			final IndividualState state = new IndividualState.IndividualStateBuilder()
			.withMaxHealth(1000f)
			.withHealthRegen(0.01f)
			.withMaxMana(0.02f)
			.withMaxMana(0f)
			.withManaRegen(0f).build();

			state.position = new Vector2(getMouseWorldX(), getMouseWorldY());
			state.velocity = new Vector2(0, 0);
			state.acceleration = new Vector2(0, 0);

			final IndividualIdentifier id = getUnknownNatureIdentifier(Util.getRandom().nextBoolean(), Util.getRandom().nextInt(5));
			id.setNickName("Rabbit");

			final Hare hare = new Hare(id, state, Faction.NPC, gameClientStateTracker.getActiveWorld().getWorldId());

			if (isServer()) {
				addIndividualService.addIndividual(hare, gameClientStateTracker.getActiveWorld().getWorldId());
			} else {
				ClientServerInterface.SendRequest.sendSpawnIndividualRequest(hare);
			}
		}

		if (keyCode == Keys.W) {
			final IndividualState state = new IndividualState.IndividualStateBuilder()
			.withMaxHealth(1000f)
			.withHealthRegen(0.01f)
			.withMaxMana(0.02f)
			.withMaxMana(0f)
			.withManaRegen(0f).build();

			state.position = new Vector2(getMouseWorldX(), getMouseWorldY());
			state.velocity = new Vector2(0, 0);
			state.acceleration = new Vector2(0, 0);

			final IndividualIdentifier id = getUnknownNatureIdentifier(Util.getRandom().nextBoolean(), Util.getRandom().nextInt(5));
			id.setNickName("Wolf");

			final Wolf wolf= new Wolf(id, state, 2, gameClientStateTracker.getActiveWorld().getWorldId());

			if (isServer()) {
				addIndividualService.addIndividual(wolf, gameClientStateTracker.getActiveWorld().getWorldId());
			} else {
				ClientServerInterface.SendRequest.sendSpawnIndividualRequest(wolf);
			}
		}

		return false;
	}


	@Override
	public void leftClickReleased() {
		panel.leftClickReleased();
		panel.getListing().clear();
		panel.getListing().add(buildMap());
	}


	private HashMap<ListingMenuItem<String>, Object> buildMap() {
		final HashMap<ListingMenuItem<String>, Object> newHashMap = Maps.newHashMap();

		newHashMap.put(
			new ListingMenuItem<String>(
				"Spawn Prop on first individual",
				new Button(
					"Spawn Prop on first individual",
					Fonts.defaultFont,
					0,
					0,
					310,
					16,
					() -> {},
					Color.GREEN,
					Color.WHITE,
					Color.GREEN,
					UIRef.BL
				),
				() -> { return new ContextMenu(
					getMouseScreenX(),
					getMouseScreenY(),
					true,
					new ContextMenu.MenuItem(
						"Dry grass",
						() -> {
							final Individual individual = Domain.getIndividualsMap().get(1);
							if (individual != null) {
								final DryGrass grass = new DryGrass(individual.getState().position.x, individual.getState().position.y);
								Domain.getWorld(individual.getWorldId()).props().addProp(grass);
							}
						},
						Color.GREEN,
						Color.WHITE,
						Color.GREEN,
						null
					),
					new ContextMenu.MenuItem(
						"Anvil",
						() -> {
							final Individual individual = Domain.getIndividualsMap().get(1);
							if (individual != null) {
								final BlacksmithWorkshop anvil = new BlacksmithWorkshop(individual.getState().position.x, individual.getState().position.y);
								Domain.getWorld(individual.getWorldId()).props().addProp(anvil);
							}
						},
						Color.GREEN,
						Color.WHITE,
						Color.GREEN,
						null
					),
					new ContextMenu.MenuItem(
						"Wooden Chest",
						() -> {
							final Individual individual = Domain.getIndividualsMap().get(1);
							if (individual != null) {
								final WoodenChestProp pineChest = new WoodenChestProp(
									individual.getState().position.x,
									individual.getState().position.y,
									100f,
									200,
									true,
									new Function<Item, Boolean>() {
										@Override
										public Boolean apply(final Item item) {
											return true;
										}
									},
									StandardWood.class
								);

								Domain.getWorld(individual.getWorldId()).props().addProp(pineChest);
							}
						},
						Color.GREEN,
						Color.WHITE,
						Color.GREEN,
						null
					),
					new ContextMenu.MenuItem(
						"Rotten Chest",
						() -> {
							final Individual individual = Domain.getIndividualsMap().get(1);
							if (individual != null) {
								final WoodenChestProp rottenChest = new RottenWoodenChest(
									individual.getState().position.x,
									individual.getState().position.y,
									100f,
									200
								);

								Domain.getWorld(individual.getWorldId()).props().addProp(rottenChest);
							}
						},
						Color.GREEN,
						Color.WHITE,
						Color.GREEN,
						null
					),
					new ContextMenu.MenuItem(
						"Workbench",
						() -> {
							final Individual individual = Domain.getIndividualsMap().get(1);
							if (individual != null) {
								final WorkBench carpenterWorkshop = new WorkBench(
									individual.getState().position.x,
									individual.getState().position.y
								);

								Domain.getWorld(individual.getWorldId()).props().addProp(carpenterWorkshop);
							}
						},
						Color.GREEN,
						Color.WHITE,
						Color.GREEN,
						null
					),
					new ContextMenu.MenuItem(
						"Furnace",
						() -> {
							final Individual individual = Domain.getIndividualsMap().get(1);
							if (individual != null) {
								final Furnace furnace = new Furnace(SandStone.class, individual.getState().position.x, individual.getState().position.y);
								furnace.setConstructionProgress(1f);
								for (int i = 0; i < 0; i++) {
									furnace.giveItem(RockItem.rock(Coal.class));
								}
								Domain.getWorld(individual.getWorldId()).props().addProp(furnace);
							}
						},
						Color.GREEN,
						Color.WHITE,
						Color.GREEN,
						null
					),
					new ContextMenu.MenuItem(
						"Campfire",
						() -> {
							final Individual individual = Domain.getIndividualsMap().get(1);
							if (individual != null) {
								final Campfire campfire = new Campfire(individual.getState().position.x, individual.getState().position.y);
								Domain.getWorld(individual.getWorldId()).props().addProp(campfire);
							}
						},
						Color.GREEN,
						Color.WHITE,
						Color.GREEN,
						null
					),
					new ContextMenu.MenuItem(
						"Carrot",
						() -> {
							final Individual individual = Domain.getIndividualsMap().get(1);
							if (individual != null) {
								final bloodandmithril.prop.plant.CarrotProp carrot = new bloodandmithril.prop.plant.CarrotProp(individual.getState().position.x, individual.getState().position.y);
								Domain.getWorld(individual.getWorldId()).props().addProp(carrot);
							}
						},
						Color.GREEN,
						Color.WHITE,
						Color.GREEN,
						null
					),
					new ContextMenu.MenuItem(
						"Torch",
						() -> {
							final Individual individual = Domain.getIndividualsMap().get(1);
							if (individual != null) {
								final MedievalWallTorchProp torch = new MedievalWallTorchProp(individual.getState().position.x, individual.getState().position.y + 100);
								Domain.getWorld(individual.getWorldId()).props().addProp(torch);
							}
						},
						Color.GREEN,
						Color.WHITE,
						Color.GREEN,
						null
					)
				);}
			),
			0
		);

		newHashMap.put(
			new ListingMenuItem<String>(
				"Render component boundaries",
				new Button(
					"Render component boundaries",
					Fonts.defaultFont,
					0,
					0,
					310,
					16,
					() -> {
						userInterface.renderComponentBoundaries = !userInterface.renderComponentBoundaries;
					},
					userInterface.renderComponentBoundaries ? Color.GREEN : Color.RED,
					Color.WHITE,
					Color.GREEN,
					UIRef.BL
				),
				null
			),
			0
		);

		newHashMap.put(
			new ListingMenuItem<String>(
				"Render component interfaces",
				new Button(
					"Render component interfaces",
					Fonts.defaultFont,
					0,
					0,
					310,
					16,
					() -> {
						userInterface.renderAvailableInterfaces = !userInterface.renderAvailableInterfaces;
					},
					userInterface.renderAvailableInterfaces ? Color.GREEN : Color.RED,
					Color.WHITE,
					Color.GREEN,
					UIRef.BL
				),
				null
			),
			0
		);

		newHashMap.put(
			new ListingMenuItem<String>(
				"Change time of day",
				new Button(
					"Change time of day",
					Fonts.defaultFont,
					0,
					0,
					310,
					16,
					() -> {
						userInterface.addLayeredComponent(
							new TextInputWindow(
								250,
								100,
								"Change time of day",
								250,
								100,
								args -> {
									if (isServer()) {
										try {
											gameClientStateTracker.getActiveWorld().getEpoch().setTimeOfDay(Float.parseFloat((String) args[0]));
										} catch (final Exception e) {
										}
									}
								},
								"Confirm",
								true,
								""
							)
						);
					},
					Color.GREEN,
					Color.WHITE,
					Color.GREEN,
					UIRef.BL
				),
				null
			),
			0
		);

		newHashMap.put(
			new ListingMenuItem<String>(
				"See All",
				new Button(
					"See All",
					Fonts.defaultFont,
					0,
					0,
					310,
					16,
					() -> {
						GaussianLightingRenderer.SEE_ALL = !GaussianLightingRenderer.SEE_ALL;
					},
					GaussianLightingRenderer.SEE_ALL ? Color.GREEN : Color.RED,
					Color.WHITE,
					Color.GREEN,
					UIRef.BL
				),
				null
			),
			0
		);

		newHashMap.put(
			new ListingMenuItem<String>(
				"Debug",
				new Button(
					"Debug",
					Fonts.defaultFont,
					0,
					0,
					310,
					16,
					() -> {
						userInterface.DEBUG = !userInterface.DEBUG;
					},
					userInterface.DEBUG ? Color.GREEN : Color.RED,
					Color.WHITE,
					Color.GREEN,
					UIRef.BL
				),
				null
			),
			0
		);

		newHashMap.put(
			new ListingMenuItem<String>(
				"Render Topography",
				new Button(
					"Render Topography",
					Fonts.defaultFont,
					0,
					0,
					310,
					16,
					() -> {
						userInterface.RENDER_TOPOGRAPHY = !userInterface.RENDER_TOPOGRAPHY;
					},
					userInterface.RENDER_TOPOGRAPHY ? Color.GREEN : Color.RED,
					Color.WHITE,
					Color.GREEN,
					UIRef.BL
				),
				null
			),
			0
		);

		newHashMap.put(
			new ListingMenuItem<String>(
				"Save game",
				new Button(
					"Save game",
					Fonts.defaultFont,
					0,
					0,
					310,
					16,
					() -> {
						userInterface.addLayeredComponent(
							new TextInputWindow(
								250,
								100,
								"Enter name",
								250,
								100,
								args -> {
									final String input = (String)args[0];
									input.replace(" ", "");

									if (StringUtils.isBlank(input)) {
										userInterface.addGlobalMessage("Invalid name", "Please enter a valid name.");
										return;
									}

									gameSaver.save(input, false);
								},
								"Save",
								true,
								""
							)
						);
					},
					Color.GREEN,
					Color.WHITE,
					Color.GREEN,
					UIRef.BL
				),
				null
			),
			0
		);

		return newHashMap;
	}


	@Override
	public Object getUniqueIdentifier() {
		return getClass();
	}
}