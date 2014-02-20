package bloodandmithril;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.concurrent.Executors;

import org.objenesis.strategy.StdInstantiatorStrategy;

import bloodandmithril.character.Individual;
import bloodandmithril.character.faction.Faction;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.persistence.GameLoader;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.prop.Prop;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.world.GameWorld;
import bloodandmithril.world.GameWorld.Light;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

/**
 * Server class, with modifications used for testing
 *
 * @author Matt
 */
public class BloodAndMithrilServerForTesting {
	public static final GameServer server = new GameServer();
	public static GameWorld gameWorld;

	public static final int SAVE_GAME = Input.Keys.S;

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

				for (Individual indi : GameWorld.individuals.values()) {
					if (indi.getSelectedByClient().remove(connection.getID())) {
						indi.deselect(false, connection.getID());
					}
				}
			}

			@Override
			public void received(final Connection connection, final Object object) {
				if (object instanceof Request) {
					ClientServerInterface.serverThread.execute(new Runnable() {
						@Override
						public void run() {
							// Cast to Request
							Request request = (Request) object;

							// Send response
							if (request.tcp()) {
								if (request.notifyOthers()) {
									Responses responseToSend = request.respond();
									for (Response response : responseToSend.responses) {
										response.prepare();
									}
									for (Connection c : server.getConnections()) {
										c.sendTCP(responseToSend);
									}
								} else {
									connection.sendTCP(request.respond());
								}
							} else {
								if (request.notifyOthers()) {
									Responses responseToSend = request.respond();
									for (Response response : responseToSend.responses) {
										response.prepare();
									}
									for (Connection c : server.getConnections()) {
										c.sendUDP(responseToSend);
									}
								} else {
									connection.sendUDP(request.respond());
								}
								Logger.networkDebug("Responding to " + request.getClass().getSimpleName() + " from " + connection.getRemoteAddressTCP(), LogLevel.TRACE);
							}
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
							for (Individual individual : GameWorld.individuals.values()) {
								ClientServerInterface.SendNotification.notifyIndividualSync(individual.getId().getId());
							}
						} catch (InterruptedException e) {
							Logger.generalDebug(e.getMessage(), LogLevel.WARN, e);
						}
						if (counter % 2 == 0) {
							for (Prop prop : GameWorld.props.values()) {
								ClientServerInterface.SendNotification.notifySyncProp(prop);
							}

							for (Faction faction : GameWorld.factions.values()) {
								ClientServerInterface.SendNotification.notifySyncFaction(faction);
							}

							for (Entry<Integer, Light> entry : GameWorld.lights.entrySet()) {
								ClientServerInterface.SendNotification.notifySyncLight(entry.getKey(), entry.getValue());
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
			gameWorld = new GameWorld(false);

			GameWorld.factions.put(0, new Faction("NPC", 0, false));
			GameWorld.factions.put(1, new Faction("Elves", 1, true));

			ClientServerInterface.setServer(true);
			GameLoader.load();

			Gdx.input.setInputProcessor(this);
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
			if (keycode == SAVE_GAME) {
				GameSaver.save(false);
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