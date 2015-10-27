package bloodandmithril.server;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.Executors;

import org.objenesis.strategy.StdInstantiatorStrategy;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import bloodandmithril.character.faction.Faction;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.generation.component.PrefabricatedComponent;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.persistence.GameLoader;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.persistence.GameSaver.PersistenceMetaData;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.prop.Prop;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;

/**
 * Entry point class for the remote game server
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class BloodAndMithrilServer {

	public static final GameServer server = new GameServer();

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
				@Override
				public void run() {
					while (ClientServerInterface.server.getUpdateThread().isAlive()) {
						try {
							Thread.sleep(100);
							for (Individual individual : Domain.getIndividuals().values()) {
								ClientServerInterface.SendNotification.notifyIndividualSync(individual.getId().getId());
							}

						} catch (InterruptedException e) {
							Logger.generalDebug(e.getMessage(), LogLevel.WARN, e);
						}
						for (Faction faction : Domain.getFactions().values()) {
							ClientServerInterface.SendNotification.notifySyncFaction(faction);
						}

						for (int worldId : Domain.getWorlds().keySet()) {
							ClientServerInterface.SendNotification.notifySyncItems(worldId);
							ClientServerInterface.SendNotification.notifySyncProjectiles(worldId);
							ClientServerInterface.SendNotification.notifySyncParticles(worldId);
							ClientServerInterface.SendNotification.notifySyncWorldState(worldId);

							for (Prop prop : Domain.getWorld(worldId).props().getProps()) {
								ClientServerInterface.SendNotification.notifySyncProp(prop);
							}
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
		cfg.width = 200;
		cfg.height = 20;
		cfg.resizable = true;

		new LwjglApplication(server, cfg);
	}

	public static class GameServer implements ApplicationListener, InputProcessor {

		@Override
		public void create() {
			Faction nature = new Faction("Nature", ParameterPersistenceService.getParameters().getNextFactionId(), false, "");
			Faction player = new Faction("Elves", ParameterPersistenceService.getParameters().getNextFactionId(), true, "Elves are cool");
			Domain.getFactions().put(nature.factionId, nature);
			Domain.getFactions().put(player.factionId, player);

			ClientServerInterface.setServer(true);
			GameLoader.load(new PersistenceMetaData("New game - " + new Date().toString()), true);
			Domain.setActiveWorld(Domain.createWorld());
			Domain.setup();

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
				World activeWorld = Domain.getActiveWorld();
				if (activeWorld != null) {
					activeWorld.update();
				}
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