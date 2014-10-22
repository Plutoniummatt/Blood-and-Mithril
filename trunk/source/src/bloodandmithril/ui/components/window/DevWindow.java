package bloodandmithril.ui.components.window;

import static bloodandmithril.character.individuals.Names.getRandomElfIdentifier;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseWorldX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseWorldY;
import static bloodandmithril.util.Util.Colors.lightColor;
import static bloodandmithril.util.Util.Colors.randomColor;
import static bloodandmithril.world.Domain.getActiveWorld;
import static com.badlogic.gdx.Gdx.input;

import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import bloodandmithril.character.faction.Faction;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.character.individuals.IndividualState;
import bloodandmithril.character.individuals.characters.Elf;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.GaussianLightingRenderer;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.GlassBottle;
import bloodandmithril.item.items.container.WoodenBucket;
import bloodandmithril.item.items.equipment.weapon.dagger.BushKnife;
import bloodandmithril.item.items.equipment.weapon.dagger.CombatKnife;
import bloodandmithril.item.items.equipment.weapon.onehandedsword.Broadsword;
import bloodandmithril.item.items.equipment.weapon.onehandedsword.Machette;
import bloodandmithril.item.items.food.animal.ChickenLeg;
import bloodandmithril.item.items.food.plant.Carrot;
import bloodandmithril.item.items.food.plant.DeathCap;
import bloodandmithril.item.items.material.Brick;
import bloodandmithril.item.items.material.Ingot;
import bloodandmithril.item.items.material.Log;
import bloodandmithril.item.items.material.Rock;
import bloodandmithril.item.items.misc.Currency;
import bloodandmithril.item.liquid.Blood;
import bloodandmithril.item.liquid.Liquid;
import bloodandmithril.item.material.metal.Copper;
import bloodandmithril.item.material.metal.Gold;
import bloodandmithril.item.material.metal.Iron;
import bloodandmithril.item.material.metal.Silver;
import bloodandmithril.item.material.metal.Steel;
import bloodandmithril.item.material.mineral.Coal;
import bloodandmithril.item.material.mineral.Hematite;
import bloodandmithril.item.material.wood.Pine;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.prop.construction.craftingstation.Anvil;
import bloodandmithril.prop.construction.craftingstation.Furnace;
import bloodandmithril.prop.construction.craftingstation.WorkBench;
import bloodandmithril.prop.furniture.WoodenChest;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.tile.tiles.brick.YellowBrickTile;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
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

		if (keyCode == Input.Keys.E) {
			IndividualState state = new IndividualState(1000f, 0.01f, 0.02f, 0f, 0f);
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
				randomColor(),
				randomColor()
			);

			elf.getSkills().setObservation(55);
			elf.getSkills().setSmithing(55);

			for (int i = Util.getRandom().nextInt(50) + 40; i > 0; i--) {
				elf.giveItem(Ingot.ingot(Iron.class));
			}
			for (int i = Util.getRandom().nextInt(50) + 40; i > 0; i--) {
				elf.giveItem(new Carrot());
			}
			for (int i = Util.getRandom().nextInt(50) + 40; i > 0; i--) {
				elf.giveItem(Ingot.ingot(Gold.class));
			}
			for (int i = Util.getRandom().nextInt(50) + 40; i > 0; i--) {
				elf.giveItem(Ingot.ingot(Silver.class));
			}
			for (int i = Util.getRandom().nextInt(50) + 40; i > 0; i--) {
				elf.giveItem(Ingot.ingot(Copper.class));
			}
			for (int i = Util.getRandom().nextInt(50) + 40; i > 0; i--) {
				elf.giveItem(new Carrot.CarrotSeed());
			}
			for (int i = Util.getRandom().nextInt(50) + 40; i > 0; i--) {
				elf.giveItem(Log.log(Pine.class));
			}
			for (int i = 40; i > 0; i--) {
				elf.giveItem(Ingot.ingot(Steel.class));
			}
			for (int i = Util.getRandom().nextInt(50) + 40; i > 0; i--) {
				elf.giveItem(Rock.rock(Hematite.class));
			}
			for (int i = Util.getRandom().nextInt(50); i > 0; i--) {
				elf.giveItem(Rock.rock(Coal.class));
			}
			for (int i = Util.getRandom().nextInt(50); i > 0; i--) {
				elf.giveItem(new DeathCap(false));
			}
			for (int i = Util.getRandom().nextInt(50); i > 0; i--) {
				elf.giveItem(new ChickenLeg());
			}
			for (int i = Util.getRandom().nextInt(50); i > 0; i--) {
				elf.giveItem(new WoodenBucket(Pine.class));
			}
			for (int i = Util.getRandom().nextInt(50) + 10; i > 0; i--) {
				Map<Class<? extends Liquid>, Float> liquids = new HashMap<>();
				if (Util.getRandom().nextBoolean()) {
					liquids.put(Blood.class, 2f);
				}
				elf.giveItem(new GlassBottle(liquids));
			}
			for (int i = Util.getRandom().nextInt(1000); i > 0; i--) {
				elf.giveItem(new Currency());
			}
			for (int i = Util.getRandom().nextInt(1000); i > 0; i--) {
				elf.giveItem(new Brick());
			}

			elf.giveItem(new BushKnife());
			elf.giveItem(new CombatKnife());
			elf.giveItem(new Machette());
			elf.giveItem(new Broadsword());

			Domain.addIndividual(elf);
			return true;
		}

		if (keyCode == Input.Keys.O) {
			Domain.getActiveWorld().getTopography().deleteTile(getMouseWorldX(), getMouseWorldY(), false);
		}

		if (keyCode == Input.Keys.K) {
			Domain.getActiveWorld().getTopography().deleteTile(getMouseWorldX(), getMouseWorldY(), true);
		}

		if (keyCode == Input.Keys.L) {
			Domain.getActiveWorld().getTopography().changeTile(getMouseWorldX(), getMouseWorldY(), true, YellowBrickTile.class);
		}

		if (keyCode == Input.Keys.P) {
			Domain.getActiveWorld().getTopography().changeTile(getMouseWorldX(), getMouseWorldY(), false, YellowBrickTile.class);
		}

		if (keyCode == Input.Keys.I) {
			Domain.getActiveWorld().getTopography().getTile(getMouseWorldX(), getMouseWorldY(), true).changeToSmoothCeiling();
		}

		if (keyCode == Input.Keys.H) {
			Domain.addItem(
				new CombatKnife(),
				new Vector2(BloodAndMithrilClient.getMouseWorldX(), BloodAndMithrilClient.getMouseWorldY()),
				new Vector2(new Vector2(800f, 0f).rotate(Util.getRandom().nextFloat() * 360)),
				Domain.getActiveWorld()
			);
		}

		if (keyCode == Input.Keys.B) {
			Domain.addItem(
				Ingot.ingot(Steel.class),
				new Vector2(BloodAndMithrilClient.getMouseWorldX(), BloodAndMithrilClient.getMouseWorldY()),
				new Vector2(new Vector2(500f, 0f).rotate(Util.getRandom().nextFloat() * 360)),
				Domain.getActiveWorld()
			);
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
				"Spawn Elf - E",
				new Button(
					"Spawn Elf - E",
					Fonts.defaultFont,
					0,
					0,
					130,
					16,
					() -> {},
					Color.CYAN,
					Color.CYAN,
					Color.CYAN,
					UIRef.BL
				),
				null
			),
			0
		);

		newHashMap.put(
			new ListingMenuItem<String>(
				"Spawn Anvil on first individual",
				new Button(
					"Spawn Anvil on first individual",
					Fonts.defaultFont,
					0,
					0,
					310,
					16,
					() -> {
						Individual individual = Domain.getIndividuals().get(1);
						if (individual != null) {
							Anvil anvil = new Anvil(individual.getState().position.x, individual.getState().position.y);
							Domain.getProps().put(anvil.id, anvil);
						}
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
				"Spawn Chest on first individual",
				new Button(
					"Spawn Chest on first individual",
					Fonts.defaultFont,
					0,
					0,
					310,
					16,
					() -> {
						Individual individual = Domain.getIndividuals().get(1);
						if (individual != null) {
							WoodenChest pineChest = new WoodenChest(
								individual.getState().position.x,
								individual.getState().position.y,
								100f,
								true,
								new Function<Item, Boolean>() {
									@Override
									public Boolean apply(Item item) {
										return true;
									}
								},
								Pine.class
							);

							Domain.getProps().put(pineChest.id, pineChest);
						}
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
				"Spawn work bench on first individual",
				new Button(
					"Spawn work bench on first individual",
					Fonts.defaultFont,
					0,
					0,
					360,
					16,
					() -> {
						Individual individual = Domain.getIndividuals().get(1);
						if (individual != null) {
							WorkBench carpenterWorkshop = new WorkBench(
								individual.getState().position.x,
								individual.getState().position.y
							);

							Domain.getProps().put(carpenterWorkshop.id, carpenterWorkshop);
						}
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
				"Spawn Furnace on first individual",
				new Button(
					"Spawn Furnace on first individual",
					Fonts.defaultFont,
					0,
					0,
					310,
					16,
					() -> {
						Individual individual = Domain.getIndividuals().get(1);
						if (individual != null) {
							Furnace furnace = new Furnace(individual.getState().position.x, individual.getState().position.y);
							furnace.setConstructionProgress(0f);
							Domain.getProps().put(furnace.id, furnace);
						}
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
				"Spawn Carrot on first individual",
				new Button(
					"Spawn Carrot on first individual",
					Fonts.defaultFont,
					0,
					0,
					310,
					16,
					() -> {
						Individual individual = Domain.getIndividuals().get(1);
						if (individual != null) {
							bloodandmithril.prop.plant.CarrotProp carrot = new bloodandmithril.prop.plant.CarrotProp(individual.getState().position.x, individual.getState().position.y);
							Domain.getProps().put(carrot.id, carrot);
						}
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