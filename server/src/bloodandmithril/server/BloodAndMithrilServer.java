package bloodandmithril.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.Function;

import org.objenesis.strategy.StdInstantiatorStrategy;

import bloodandmithril.character.conditions.Poison;
import bloodandmithril.character.faction.Faction;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.character.individuals.IndividualState;
import bloodandmithril.character.individuals.Names;
import bloodandmithril.character.individuals.characters.Elf;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.generation.component.PrefabricatedComponent;
import bloodandmithril.item.items.Item;
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
import bloodandmithril.item.items.material.Rock;
import bloodandmithril.item.items.misc.Currency;
import bloodandmithril.item.liquid.Blood;
import bloodandmithril.item.liquid.Liquid;
import bloodandmithril.item.liquid.Water;
import bloodandmithril.item.material.metal.Steel;
import bloodandmithril.item.material.mineral.Coal;
import bloodandmithril.item.material.wood.Pine;
import bloodandmithril.persistence.GameLoader;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.construction.craftingstation.Anvil;
import bloodandmithril.prop.construction.craftingstation.Furnace;
import bloodandmithril.prop.furniture.WoodenChest;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.fluid.Fluid;
import bloodandmithril.world.topography.fluid.FluidFraction;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

/**
 * Entry point class for the remote game server
 *
 * @author Matt
 */
public class BloodAndMithrilServer {

	public static final GameServer server = new GameServer();
	private static Domain gameWorld;

	/**
	 * Entry point for the server
	 */
	public static void main(String[] args) {

		ClientServerInterface.server = new Server(1048576, 1048576);
		final Server server = ClientServerInterface.server;
		ClientServerInterface.registerClasses(server.getKryo());
		server.start();

		try {
			server.bind(42685, 42686);
		} catch (IOException e) {
			Logger.networkDebug(e.getMessage(), LogLevel.WARN);
		}

		ClientServerInterface.serverThread = Executors.newCachedThreadPool();

		server.getKryo().setInstantiatorStrategy(new StdInstantiatorStrategy());

		server.addListener(new Listener() {

			@Override
			public void disconnected (Connection connection) {
				ClientServerInterface.SendNotification.notifySyncPlayerList();

				for (Individual indi : Domain.getIndividuals().values()) {
					if (indi.getSelectedByClient().remove(connection.getID())) {
						indi.deselect(false, connection.getID());
					}
				}
			}

			@Override
			public void received(final Connection connection, final Object object) {
				if (object instanceof Request) {
					ClientServerInterface.serverThread.execute(() -> {
						// Cast to Request
						Request request = (Request) object;

						// Send response
						if (request.tcp()) {
							Responses responseToSend = request.respond();
							for (Response response : responseToSend.getResponses()) {
								response.prepare();
							}
							if (request.notifyOthers()) {
								for (Connection c : server.getConnections()) {
									c.sendTCP(responseToSend);
								}
							} else {
								connection.sendTCP(request.respond());
							}
						} else {
							Responses responseToSend = request.respond();
							for (Response response : responseToSend.getResponses()) {
								response.prepare();
							}
							if (request.notifyOthers()) {
								for (Connection c : server.getConnections()) {
									c.sendUDP(responseToSend);
								}
							} else {
								connection.sendUDP(request.respond());
							}
							Logger.networkDebug("Responding to " + request.getClass().getSimpleName() + " from " + connection.getRemoteAddressTCP(), LogLevel.TRACE);
						}
					});
				}
			}
		});

		ClientServerInterface.syncThread = new Thread(
			new Runnable() {
				int counter = 0;

				@Override
				public void run() {
					while (ClientServerInterface.server.getUpdateThread().isAlive()) {
						counter++;
						try {
							Thread.sleep(100);
							for (Individual individual : Domain.getIndividuals().values()) {
								ClientServerInterface.SendNotification.notifyIndividualSync(individual.getId().getId());
							}

						} catch (InterruptedException e) {
							Logger.generalDebug(e.getMessage(), LogLevel.WARN, e);
						}
						if (counter % 2 == 0) {
							for (Prop prop : Domain.getProps().values()) {
								ClientServerInterface.SendNotification.notifySyncProp(prop);

							}

							for (Faction faction : Domain.getFactions().values()) {
								ClientServerInterface.SendNotification.notifySyncFaction(faction);
							}

							if (Domain.getActiveWorld() != null && Domain.getActiveWorld().getTopography() != null && Domain.getActiveWorld().getTopography().getFluids() != null) {
								ClientServerInterface.SendNotification.notifySyncFluids();
							}

							ClientServerInterface.SendNotification.notifySyncItems();
						}

						if (counter >= 100) {
							ClientServerInterface.SendNotification.notifySyncWorldState();
							counter = 0;
						}
					}
				}
			}
		);

		ClientServerInterface.syncThread.start();

		start();
	}

	private static void start() {

		// Configurations
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Blood and Mithril Server";
		cfg.width = 500;
		cfg.height = 250;
		cfg.resizable = true;

		new LwjglApplication(server, cfg);
	}

	public static class GameServer implements ApplicationListener, InputProcessor {

		@Override
		public void create() {
			Domain.getFactions().put(0, new Faction("NPC", 0, false));
			Domain.getFactions().put(1, new Faction("Elves", 1, true));

			ClientServerInterface.setServer(true);
			GameLoader.load();
			gameWorld = new Domain();

			Gdx.input.setInputProcessor(this);

			PrefabricatedComponent.setup();
		}


		@Override
		public void resize(int width, int height) {
		}


		@Override
		public void render() {
			GameSaver.update();

			// Do not update if game is paused
			// Do not update if FPS is lower than tolerance threshold, otherwise
			// bad things can happen, like teleporting
			float delta = Gdx.graphics.getDeltaTime();
			if (delta < 0.1f && !GameSaver.isSaving()) {
				gameWorld.update(0, 0);
			}
		}


		@Override
		public void pause() {
		}


		@Override
		public void resume() {
		}


		@Override
		public void dispose() {
		}


		@Override
		public boolean keyDown(int keycode) {

			if (keycode == Input.Keys.S) {
				GameSaver.save(false);
			}

			if (keycode == Input.Keys.J) {
				Domain.getActiveWorld().getTopography().getFluids().put(
					0,
					50,
					new Fluid(FluidFraction.fraction(new Water(), 16f))
				);
			}

			if (keycode == Input.Keys.K) {
				Domain.getActiveWorld().getTopography().getFluids().put(
					0,
					50,
					new Fluid(FluidFraction.fraction(new Blood(), 16f))
				);
			}

			if (keycode == Input.Keys.P) {
				Domain.getIndividuals().get(1).addCondition(new Poison(1f, 0.1f));
			}

			if (keycode == Input.Keys.T) {
				Individual individual = Domain.getIndividuals().get(1);
				if (individual != null) {
					WoodenChest pineChest = new WoodenChest(
						individual.getState().position.x,
						individual.getState().position.y, 100f, true,
						new Function<Item, Boolean>() {
							@Override
							public Boolean apply(Item t) {
								return true;
							}
						},
						Pine.class
					);
					Domain.getProps().put(pineChest.id, pineChest);
				}
			}

			if (keycode == Input.Keys.Z) {
				Individual individual = Domain.getIndividuals().get(1);
				if (individual != null) {
					Anvil anvil = new Anvil(
						individual.getState().position.x,
						individual.getState().position.y
					);
					Domain.getProps().put(anvil.id, anvil);
				}
			}

			if (keycode == Input.Keys.M) {
				Individual individual = Domain.getIndividuals().get(1);
				if (individual != null) {
					Furnace furnace = new Furnace(individual.getState().position.x, individual.getState().position.y);
					furnace.setConstructionProgress(0f);
					Domain.getProps().put(furnace.id, furnace);
				}
			}

			if (keycode == Input.Keys.N) {
				Individual individual = Domain.getIndividuals().get(1);
				if (individual != null) {
					bloodandmithril.prop.plant.Carrot carrot = new bloodandmithril.prop.plant.Carrot(individual.getState().position.x, individual.getState().position.y);
					Domain.getProps().put(carrot.id, carrot);
				}
			}

			if (keycode == Input.Keys.Y) {
				Individual individual = Domain.getIndividuals().get(1);
				if (individual != null) {
					Anvil anvil = new Anvil(individual.getState().position.x, individual.getState().position.y);
					Domain.getProps().put(anvil.id, anvil);
				}
			}

			if (keycode == Input.Keys.R) {
				IndividualState state = new IndividualState(10f, 0.01f, 0.02f, 0f, 0f);
				state.position = new Vector2(200, 700);
				state.velocity = new Vector2(0, 0);
				state.acceleration = new Vector2(0, 0);

				IndividualIdentifier id = Names.getRandomElfIdentifier(true, Util.getRandom().nextInt(100) + 50);
				id.setNickName("Elfie");

				Elf elf = new Elf(
					id,
					state,
					Gdx.input.isKeyPressed(Input.Keys.Q) ? Faction.NPC : 1,
					true,
					20f,
					Domain.getActiveWorld(),
					Colors.lightColor(),
					Colors.lightColor(),
					Colors.lightSkinColor()
				);

				elf.getSkills().setSmithing(50);

				for (int i = Util.getRandom().nextInt(50); i > 0; i--) {
					elf.giveItem(new Carrot());
				}
				for (int i = Util.getRandom().nextInt(50) + 40; i > 0; i--) {
					elf.giveItem(Ingot.ingot(Steel.class));
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
					Map<Class<? extends Liquid>, Float> liquids = new HashMap<>();
					liquids.put(Blood.class, 16f);
					elf.giveItem(new WoodenBucket(liquids, Pine.class));
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

				Domain.getIndividuals().put(elf.getId().getId(), elf);
			}
			return false;
		}


		@Override
		public boolean keyUp(int keycode) {
			return false;
		}


		@Override
		public boolean keyTyped(char character) {
			return false;
		}


		@Override
		public boolean touchDown(int screenX, int screenY, int pointer,
				int button) {
			return false;
		}


		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button) {
			return false;
		}


		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer) {
			return false;
		}


		@Override
		public boolean mouseMoved(int screenX, int screenY) {
			return false;
		}


		@Override
		public boolean scrolled(int amount) {
			return false;
		}
	}
}