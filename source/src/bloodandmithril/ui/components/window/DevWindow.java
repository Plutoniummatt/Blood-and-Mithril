package bloodandmithril.ui.components.window;

import static bloodandmithril.character.individuals.Names.getRandomElfIdentifier;
import static bloodandmithril.character.individuals.Names.getUnknownNatureIdentifier;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseWorldX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseWorldY;
import static bloodandmithril.util.Util.Colors.lightColor;
import static bloodandmithril.util.Util.Colors.lightSkinColor;
import static bloodandmithril.world.Domain.getActiveWorld;
import static com.badlogic.gdx.Gdx.input;

import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import bloodandmithril.character.faction.Faction;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.character.individuals.IndividualState;
import bloodandmithril.character.individuals.characters.Elf;
import bloodandmithril.character.individuals.characters.Hare;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.GaussianLightingRenderer;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.misc.FlintAndFiresteel;
import bloodandmithril.item.items.equipment.weapon.dagger.BushKnife;
import bloodandmithril.item.items.equipment.weapon.onehandedsword.Broadsword;
import bloodandmithril.item.items.equipment.weapon.ranged.LongBow;
import bloodandmithril.item.items.equipment.weapon.ranged.projectile.Arrow;
import bloodandmithril.item.items.equipment.weapon.ranged.projectile.FireArrow;
import bloodandmithril.item.items.equipment.weapon.ranged.projectile.GlowStickArrow;
import bloodandmithril.item.items.food.animal.ChickenLeg;
import bloodandmithril.item.items.food.plant.Carrot;
import bloodandmithril.item.items.food.plant.Carrot.CarrotSeed;
import bloodandmithril.item.items.material.Bricks;
import bloodandmithril.item.items.material.Ingot;
import bloodandmithril.item.items.material.Rock;
import bloodandmithril.item.liquid.Water;
import bloodandmithril.item.material.metal.Iron;
import bloodandmithril.item.material.metal.Steel;
import bloodandmithril.item.material.mineral.Coal;
import bloodandmithril.item.material.mineral.SandStone;
import bloodandmithril.item.material.wood.StandardWood;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.prop.construction.craftingstation.Anvil;
import bloodandmithril.prop.construction.craftingstation.Campfire;
import bloodandmithril.prop.construction.craftingstation.Furnace;
import bloodandmithril.prop.construction.craftingstation.WorkBench;
import bloodandmithril.prop.furniture.MedievalWallTorch;
import bloodandmithril.prop.furniture.WoodenChest;
import bloodandmithril.prop.plant.tree.Tree;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Util;
import bloodandmithril.util.datastructure.TwoInts;
import bloodandmithril.world.Domain;
import bloodandmithril.world.fluids.FluidBody;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.tiles.brick.YellowBrickTile;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Window containing dev features
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class DevWindow extends Window {

	private final ScrollableListingPanel<String, Object> panel;

	/**
	 * Constructor
	 */
	public DevWindow(int x, int y, int length, int height, boolean active) {
		super(x, y, length, height, "Developer", active, 500, 300, false, true, true);

		panel = new ScrollableListingPanel<String, Object>(this, Comparator.<String>naturalOrder(), false, 35) {
			@Override
			protected String getExtraString(Entry<ListingMenuItem<String>, Object> item) {
				return "";
			}

			@Override
			protected int getExtraStringOffset() {
				return 0;
			}

			@Override
			protected void onSetup(List<HashMap<ListingMenuItem<String>, Object>> listings) {
				HashMap<ListingMenuItem<String>, Object> newHashMap = buildMap();

				listings.add(newHashMap);
			}

			@Override
			public boolean keyPressed(int keyCode) {
				return false;
			}
		};

		panel.setScrollWheelActive(true);
	}


	@Override
	protected void internalWindowRender() {
		panel.x = x;
		panel.y = y;
		panel.width = width;
		panel.height = height;

		panel.render();
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		panel.leftClick(copy, windowsCopy);
	}


	@Override
	protected void uponClose() {
	}


	@Override
	public boolean scrolled(int amount) {
		return panel.scrolled(amount);
	}


	@Override
	public boolean keyPressed(int keyCode) {
		if (super.keyPressed(keyCode)) {
			return true;
		}

		if (keyCode == Keys.T) {
			Domain.getActiveWorld().getTopography().changeTile(
				BloodAndMithrilClient.getMouseWorldX(),
				BloodAndMithrilClient.getMouseWorldY(),
				true,
				YellowBrickTile.class
			);
		}

		if (keyCode == Keys.D) {
			Domain.getActiveWorld().getTopography().deleteTile(
				BloodAndMithrilClient.getMouseWorldX(),
				BloodAndMithrilClient.getMouseWorldY(),
				true
			);
		}

		if (keyCode == Keys.F) {
			int x = Topography.convertToWorldTileCoord(BloodAndMithrilClient.getMouseWorldX());
			int y = Topography.convertToWorldTileCoord(BloodAndMithrilClient.getMouseWorldY());

			List<TwoInts> coords = Lists.newArrayList();
			coords.add(new TwoInts(x + 1, y));
			coords.add(new TwoInts(x + 2, y));
			coords.add(new TwoInts(x + 3, y));
			coords.add(new TwoInts(x + 4, y));
			coords.add(new TwoInts(x + 1, y + 1));
			coords.add(new TwoInts(x + 2, y + 1));
			coords.add(new TwoInts(x + 3, y + 1));
			coords.add(new TwoInts(x + 4, y + 1));
			coords.add(new TwoInts(x + 1, y + 2));
			coords.add(new TwoInts(x + 2, y + 2));
			coords.add(new TwoInts(x + 3, y + 2));
			coords.add(new TwoInts(x + 4, y + 2));
			coords.add(new TwoInts(x + 1, y + 3));
			coords.add(new TwoInts(x + 2, y + 3));
			coords.add(new TwoInts(x + 3, y + 3));
			coords.add(new TwoInts(x + 4, y + 3));

			FluidBody fluid = FluidBody.createForTileCoordinates(coords, 16f, Water.class, Domain.getActiveWorld().getWorldId());
			Domain.getActiveWorld().addFluid(fluid);
		}

		if (keyCode == Keys.E) {
			IndividualState state = new IndividualState(30f, 0.01f, 0.02f, 0f, 0f);
			state.position = new Vector2(getMouseWorldX(), getMouseWorldY());
			state.velocity = new Vector2(0, 0);
			state.acceleration = new Vector2(0, 0);

			IndividualIdentifier id = getRandomElfIdentifier(true, Util.getRandom().nextInt(100) + 50);
			id.setNickName("Elfie");

			Elf elf = new Elf(
				id, state, input.isKeyPressed(Input.Keys.Q) ? Faction.NPC : 1, true,
				20f,
				getActiveWorld(),
				lightColor(),
				lightColor(),
				lightSkinColor()
			);

			elf.getSkills().setObservation(55);
			elf.getSkills().setSmithing(55);

			elf.giveItem(new bloodandmithril.item.items.furniture.WoodenChest(StandardWood.class));
			for (int i = 100; i > 0; i--) {
				elf.giveItem(new bloodandmithril.item.items.furniture.MedievalWallTorch());
				elf.giveItem(new Carrot());
				elf.giveItem(Arrow.ArrowItem.arrowItem(Steel.class));
				elf.giveItem(new FireArrow.FireArrowItem<>(Iron.class, 10));
				elf.giveItem(new GlowStickArrow.GlowStickArrowItem<>(Iron.class, 10));
			}
			for (int i = 10; i > 0; i--) {
				elf.giveItem(Ingot.ingot(Steel.class));
				elf.giveItem(new FlintAndFiresteel());
				elf.giveItem(Rock.rock(Coal.class));
			}
			for (int i = 5; i > 0; i--) {
				elf.giveItem(Bricks.bricks(SandStone.class));
			}
			for (int i = 5; i > 0; i--) {
				elf.giveItem(Rock.rock(SandStone.class));
			}
			for (int i = 5; i > 0; i--) {
				elf.giveItem(new ChickenLeg(false));
			}
			for (int i = 1; i > 0; i--) {
				Broadsword item = new Broadsword();
				elf.giveItem(item);
			}
			for (int i = 1; i > 0; i--) {
				LongBow<StandardWood> bow = new LongBow<>(10f, 5, true, 10, StandardWood.class);
				elf.giveItem(bow);
			}
			for (int i = 1; i > 0; i--) {
				BushKnife item = new BushKnife();
				elf.giveItem(item);
			}
			for (int i = 100; i > 0; i--) {
				elf.giveItem(new CarrotSeed());
			}

			Domain.addIndividual(elf, Domain.getActiveWorld().getWorldId());
		}

		if (keyCode == Keys.R) {
			IndividualState state = new IndividualState(1000f, 0.01f, 0.02f, 0f, 0f);
			state.position = new Vector2(getMouseWorldX(), getMouseWorldY());
			state.velocity = new Vector2(0, 0);
			state.acceleration = new Vector2(0, 0);

			IndividualIdentifier id = getUnknownNatureIdentifier(Util.getRandom().nextBoolean(), Util.getRandom().nextInt(5));
			id.setNickName("Rabbit");

			Hare hare = new Hare(id, state, Faction.NPC, getActiveWorld().getWorldId());
			Domain.addIndividual(hare, Domain.getActiveWorld().getWorldId());
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
		HashMap<ListingMenuItem<String>, Object> newHashMap = Maps.newHashMap();

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
				new ContextMenu(
					getMouseScreenX(),
					getMouseScreenY(),
					true,
					new ContextMenu.MenuItem(
						"Anvil",
						() -> {
							Individual individual = Domain.getIndividuals().get(1);
							if (individual != null) {
								Anvil anvil = new Anvil(individual.getState().position.x, individual.getState().position.y);
								Domain.getWorld(individual.getWorldId()).props().addProp(anvil);
							}
						},
						Color.GREEN,
						Color.WHITE,
						Color.GREEN,
						null
					),
					new ContextMenu.MenuItem(
						"Pine Chest",
						() -> {
							Individual individual = Domain.getIndividuals().get(1);
							if (individual != null) {
								WoodenChest pineChest = new WoodenChest(
									individual.getState().position.x,
									individual.getState().position.y,
									100f,
									200,
									true,
									new Function<Item, Boolean>() {
										@Override
										public Boolean apply(Item item) {
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
						"Workbench",
						() -> {
							Individual individual = Domain.getIndividuals().get(1);
							if (individual != null) {
								WorkBench carpenterWorkshop = new WorkBench(
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
							Individual individual = Domain.getIndividuals().get(1);
							if (individual != null) {
								Furnace furnace = new Furnace(SandStone.class, individual.getState().position.x, individual.getState().position.y);
								furnace.setConstructionProgress(1f);
								for (int i = 0; i < 200; i++) {
									furnace.giveItem(Rock.rock(Coal.class));
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
							Individual individual = Domain.getIndividuals().get(1);
							if (individual != null) {
								Campfire campfire = new Campfire(individual.getState().position.x, individual.getState().position.y);
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
							Individual individual = Domain.getIndividuals().get(1);
							if (individual != null) {
								bloodandmithril.prop.plant.CarrotProp carrot = new bloodandmithril.prop.plant.CarrotProp(individual.getState().position.x, individual.getState().position.y);
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
							Individual individual = Domain.getIndividuals().get(1);
							if (individual != null) {
								MedievalWallTorch torch = new MedievalWallTorch(individual.getState().position.x, individual.getState().position.y + 100);
								Domain.getWorld(individual.getWorldId()).props().addProp(torch);
							}
						},
						Color.GREEN,
						Color.WHITE,
						Color.GREEN,
						null
					),
					new ContextMenu.MenuItem(
						"Tree",
						() -> {
							Individual individual = Domain.getIndividuals().get(1);
							if (individual != null) {
								Tree tree = new Tree(individual.getState().position.x, individual.getState().position.y, 100, 100);
								Domain.getWorld(individual.getWorldId()).props().addProp(tree);
							}
						},
						Color.GREEN,
						Color.WHITE,
						Color.GREEN,
						null
					)
				)
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
						UserInterface.renderComponentBoundaries = !UserInterface.renderComponentBoundaries;
					},
					UserInterface.renderComponentBoundaries ? Color.GREEN : Color.RED,
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
						UserInterface.renderAvailableInterfaces = !UserInterface.renderAvailableInterfaces;
					},
					UserInterface.renderAvailableInterfaces ? Color.GREEN : Color.RED,
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
						UserInterface.DEBUG = !UserInterface.DEBUG;
					},
					UserInterface.DEBUG ? Color.GREEN : Color.RED,
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
						UserInterface.RENDER_TOPOGRAPHY = !UserInterface.RENDER_TOPOGRAPHY;
					},
					UserInterface.RENDER_TOPOGRAPHY ? Color.GREEN : Color.RED,
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
						UserInterface.addLayeredComponent(
							new TextInputWindow(
								BloodAndMithrilClient.WIDTH / 2 - 125,
								BloodAndMithrilClient.HEIGHT/2 + 50,
								250,
								100,
								"Enter name",
								250,
								100,
								args -> {
									String input = (String)args[0];
									input.replace(" ", "");

									if (StringUtils.isBlank(input)) {
										UserInterface.addMessage("Invalid name", "Please enter a valid name.");
										return;
									}

									GameSaver.save(input, false);
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