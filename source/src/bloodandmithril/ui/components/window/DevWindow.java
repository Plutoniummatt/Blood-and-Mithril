package bloodandmithril.ui.components.window;

import static bloodandmithril.character.individuals.Names.getRandomElfIdentifier;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;
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
import bloodandmithril.item.items.material.Brick;
import bloodandmithril.item.items.material.Rock;
import bloodandmithril.item.material.mineral.Coal;
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
				"Spawn Elf on cursor",
				new Button(
					"Spawn Elf on cursor",
					Fonts.defaultFont,
					0,
					0,
					130,
					16,
					() -> {
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

						for (int i = 10; i > 0; i--) {
							elf.giveItem(Rock.rock(Coal.class));
						}
						for (int i = 5; i > 0; i--) {
							elf.giveItem(new Brick());
						}

						Domain.addIndividual(elf);
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
								Domain.addProp(anvil);
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
									Pine.class
								);

								Domain.addProp(pineChest);
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

								Domain.addProp(carpenterWorkshop);
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
								Furnace furnace = new Furnace(individual.getState().position.x, individual.getState().position.y);
								furnace.setConstructionProgress(0f);
								Domain.addProp(furnace);
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
								Domain.addProp(carrot);
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