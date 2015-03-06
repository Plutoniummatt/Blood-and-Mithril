package bloodandmithril.server;

import static bloodandmithril.util.Util.Colors.lightColor;
import static bloodandmithril.util.Util.Colors.lightSkinColor;
import static bloodandmithril.world.Domain.getActiveWorld;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.Executors;

import org.objenesis.strategy.StdInstantiatorStrategy;

import bloodandmithril.character.faction.Faction;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.character.individuals.IndividualState;
import bloodandmithril.character.individuals.Names;
import bloodandmithril.character.individuals.characters.Elf;
import bloodandmithril.core.Copyright;
import bloodandmithril.generation.component.PrefabricatedComponent;
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
import bloodandmithril.item.material.metal.Iron;
import bloodandmithril.item.material.metal.Steel;
import bloodandmithril.item.material.mineral.Coal;
import bloodandmithril.item.material.mineral.SandStone;
import bloodandmithril.item.material.wood.StandardWood;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.persistence.GameLoader;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.persistence.GameSaver.PersistenceMetaData;
import bloodandmithril.prop.Prop;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
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
@Copyright("Matthew Peck 2014")
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
								connection.sendTCP(responseToSend);
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
								connection.sendUDP(responseToSend);
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
							for (Faction faction : Domain.getFactions().values()) {
								ClientServerInterface.SendNotification.notifySyncFaction(faction);
							}

							for (int worldId : Domain.getWorlds().keySet()) {
								ClientServerInterface.SendNotification.notifySyncItems(worldId);
								ClientServerInterface.SendNotification.notifySyncProjectiles(worldId);
								ClientServerInterface.SendNotification.notifySyncParticles(worldId);
								for (Prop prop : Domain.getWorld(worldId).props().getProps()) {
									ClientServerInterface.SendNotification.notifySyncProp(prop);
								}

							}
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
			Domain.getFactions().put(0, new Faction("Nature", 0, false, ""));
			Domain.getFactions().put(1, new Faction("Elves", 1, true, "Elves are cool"));

			ClientServerInterface.setServer(true);
			GameLoader.load(new PersistenceMetaData("New game - " + new Date().toString()), true);
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

			if (keycode == Keys.I) {
				IndividualState state = new IndividualState(30f, 0.01f, 0.02f, 0f, 0f);
				state.position = new Vector2(0, 2500);
				state.velocity = new Vector2(0, 0);
				state.acceleration = new Vector2(0, 0);

				IndividualIdentifier id = Names.getRandomElfIdentifier(true, Util.getRandom().nextInt(100) + 50);
				id.setNickName("Elfie");

				Elf elf = new Elf(
					id, state, Gdx.input.isKeyPressed(Input.Keys.Q) ? Faction.NPC : 1, true,
					20f,
					getActiveWorld(),
					lightColor(),
					lightColor(),
					lightSkinColor()
				);

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