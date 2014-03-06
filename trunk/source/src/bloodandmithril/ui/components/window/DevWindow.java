package bloodandmithril.ui.components.window;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.character.Individual;
import bloodandmithril.character.Individual.IndividualIdentifier;
import bloodandmithril.character.Individual.IndividualState;
import bloodandmithril.character.faction.Faction;
import bloodandmithril.character.individuals.Boar;
import bloodandmithril.character.individuals.Elf;
import bloodandmithril.character.individuals.Names;
import bloodandmithril.item.equipment.Broadsword;
import bloodandmithril.item.equipment.ButterflySword;
import bloodandmithril.item.material.animal.ChickenLeg;
import bloodandmithril.item.material.container.GlassBottle;
import bloodandmithril.item.material.fuel.Coal;
import bloodandmithril.item.material.liquid.Liquid.Water;
import bloodandmithril.item.material.plant.Carrot;
import bloodandmithril.item.material.plant.DeathCap;
import bloodandmithril.item.misc.Currency;
import bloodandmithril.prop.building.Furnace;
import bloodandmithril.prop.building.PineChest;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Task;
import bloodandmithril.util.Util;
import bloodandmithril.world.Epoch;
import bloodandmithril.world.GameWorld;
import bloodandmithril.world.GameWorld.DynamicLightingPostRenderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Maps;

/**
 * Window containing dev features
 *
 * @author Matt
 */
public class DevWindow extends Window {

	ScrollableListingPanel<String> panel;

	/**
	 * Constructor
	 */
	public DevWindow(int x, int y, int length, int height, boolean active) {
		super(x, y, length, height, "Developer", active, 500, 300, false, true);

		panel = new ScrollableListingPanel<String>(this) {
			@Override
			protected String getExtraString(Entry<ListingMenuItem<String>, Integer> item) {
				return "";
			}

			@Override
			protected int getExtraStringOffset() {
				return 0;
			}

			@Override
			protected void onSetup(List<HashMap<ListingMenuItem<String>, Integer>> listings) {
				HashMap<ListingMenuItem<String>, Integer> newHashMap = buildMap();

				listings.add(newHashMap);
			}

			@Override
			public boolean keyPressed(int keyCode) {
				// TODO Auto-generated method stub
				return false;
			}
		};
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
	public boolean keyPressed(int keyCode) {
		if (keyCode == Input.Keys.E) {
			IndividualState state = new IndividualState(10f, 10f, 0.01f, 1f, 1f, 1f, 1f);
			state.position = new Vector2(BloodAndMithrilClient.getMouseWorldX(), BloodAndMithrilClient.getMouseWorldY());
			state.velocity = new Vector2(0, 0);
			state.acceleration = new Vector2(0, 0);

			IndividualIdentifier id = Names.getRandomElfIdentifier(true, Util.getRandom().nextInt(100) + 50);
			id.setNickName("Elfie");

			Elf elf = new Elf(
				id, state, Gdx.input.isKeyPressed(Input.Keys.Q) ? Faction.NPC : 1, true,
				new Color(0.5f + 0.5f*Util.getRandom().nextFloat(), 0.5f + 0.5f*Util.getRandom().nextFloat(), 0.5f + 0.5f*Util.getRandom().nextFloat(), 1),
				new Color(0.2f + 0.4f*Util.getRandom().nextFloat(), 0.2f + 0.3f*Util.getRandom().nextFloat(), 0.5f + 0.3f*Util.getRandom().nextFloat(), 1),
				Util.getRandom().nextInt(4),
				20f
			);
			
			elf.getSkills().setObservation(55);

			for (int i = Util.getRandom().nextInt(50); i > 0; i--) {
				elf.giveItem(new Carrot());
			}
			for (int i = Util.getRandom().nextInt(50); i > 0; i--) {
				elf.giveItem(new Coal());
			}
			for (int i = Util.getRandom().nextInt(50); i > 0; i--) {
				elf.giveItem(new DeathCap(false));
			}
			for (int i = Util.getRandom().nextInt(50); i > 0; i--) {
				elf.giveItem(new ChickenLeg());
			}
			for (int i = Util.getRandom().nextInt(50); i > 0; i--) {
				elf.giveItem(new GlassBottle(Water.class, 1f));
			}
			for (int i = Util.getRandom().nextInt(1000); i > 0; i--) {
				elf.giveItem(new Currency());
			}
			elf.giveItem(new ButterflySword(100));
			elf.giveItem(new Broadsword(100));

			GameWorld.individuals.put(elf.getId().getId(), elf);
			return true;
		}

		if (keyCode == Input.Keys.U) {
			IndividualState state = new IndividualState(10f, 10f, 0.01f, 1f, 1f, 1f, 1f);
			state.position = new Vector2(BloodAndMithrilClient.getMouseWorldX(), BloodAndMithrilClient.getMouseWorldY());
			state.velocity = new Vector2(0, 0);
			state.acceleration = new Vector2(0, 0);

			IndividualIdentifier id = new IndividualIdentifier("Unknown", "", new Epoch(10f, 12, 12, 2012));
			id.setNickName("Unknown");

			Boar boar = new Boar(id, state);

			GameWorld.individuals.put(boar.getId().getId(), boar);
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


	private HashMap<ListingMenuItem<String>, Integer> buildMap() {
		HashMap<ListingMenuItem<String>, Integer> newHashMap = Maps.newHashMap();

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
					new Task() {
						@Override
						public void execute() {
						}
					},
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
				"Spawn Boar - U",
				new Button(
					"Spawn Boar - U",
					Fonts.defaultFont,
					0,
					0,
					130,
					16,
					new Task() {
						@Override
						public void execute() {
						}
					},
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
				"Spawn Chest on first individual",
				new Button(
					"Spawn Chest on first individual",
					Fonts.defaultFont,
					0,
					0,
					310,
					16,
					new Task() {
						@Override
						public void execute() {
							Individual individual = GameWorld.individuals.get(1);
							if (individual != null) {
								PineChest pineChest = new PineChest(individual.getState().position.x, individual.getState().position.y, true, 100f);
								GameWorld.props.put(pineChest.id, pineChest);
							}
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
					new Task() {
						@Override
						public void execute() {
							Individual individual = GameWorld.individuals.get(1);
							if (individual != null) {
								Furnace furnace = new Furnace(individual.getState().position.x, individual.getState().position.y);
								GameWorld.props.put(furnace.id, furnace);
							}
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
					new Task() {
						@Override
						public void execute() {
							Individual individual = GameWorld.individuals.get(1);
							if (individual != null) {
								bloodandmithril.prop.plant.Carrot carrot = new bloodandmithril.prop.plant.Carrot(individual.getState().position.x, individual.getState().position.y);
								GameWorld.props.put(carrot.id, carrot);
							}
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
					new Task() {
						@Override
						public void execute() {
							UserInterface.renderComponentBoundaries = !UserInterface.renderComponentBoundaries;
						}
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
					new Task() {
						@Override
						public void execute() {
							UserInterface.renderAvailableInterfaces = !UserInterface.renderAvailableInterfaces;
						}
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
					new Task() {
						@Override
						public void execute() {
							DynamicLightingPostRenderer.SEE_ALL = !DynamicLightingPostRenderer.SEE_ALL;
						}
					},
					DynamicLightingPostRenderer.SEE_ALL ? Color.GREEN : Color.RED,
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
					new Task() {
						@Override
						public void execute() {
							UserInterface.DEBUG = !UserInterface.DEBUG;
						}
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

		return newHashMap;
	}
}