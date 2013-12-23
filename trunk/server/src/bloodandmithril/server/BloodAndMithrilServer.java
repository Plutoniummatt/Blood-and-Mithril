package bloodandmithril.server;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.objenesis.strategy.StdInstantiatorStrategy;

import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.csi.Request;
import bloodandmithril.persistence.GameLoader;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.world.GameWorld;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

/**
 * Entry point class for the remote game server
 *
 * @author Matt
 */
public class BloodAndMithrilServer {

	private static GameWorld gameWorld;
	
	private static ExecutorService newCachedThreadPool;
	
	/**
	 * Entry point for the server
	 */
	public static void main(String[] args) {

		Server server = new Server(65536, 65536);
		server.start();

		try {
			server.bind(42685);
		} catch (IOException e) {
			Logger.networkDebug(e.getMessage(), LogLevel.WARN);
		}
		
		newCachedThreadPool = Executors.newCachedThreadPool();

		ClientServerInterface.registerClasses(server.getKryo());
		server.getKryo().setInstantiatorStrategy(new StdInstantiatorStrategy());

		server.addListener(new Listener() {
			@Override
			public void received(final Connection connection, final Object object) {
				if (object instanceof Request) {
					newCachedThreadPool.execute(new Runnable() {
						@Override
						public void run() {
							// Cast to Request
							Request request = (Request) object;
							
							// Send response
							connection.sendTCP(request.respond());
						}
					});
				}
			}
		});

		start();
	}

	
	private static void start() {

		// Configurations
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Blood and Mithril Server";
		cfg.width = 500;
		cfg.height = 250;
		cfg.resizable = true;

		new LwjglApplication(new GameServer(), cfg);
	}


	private static class GameServer implements ApplicationListener {

		@Override
		public void create() {
			gameWorld = new GameWorld(true);
			GameLoader.load();
		}


		@Override
		public void resize(int width, int height) {
		}


		@Override
		public void render() {
			GameSaver.update();

			// Do not update if game is paused
			// Do not update if FPS is lower than tolerance threshold, otherwise bad things can happen, like teleporting
			float delta = Gdx.graphics.getDeltaTime();
			if (delta < Float.parseFloat(System.getProperty("lagSpikeTolerance")) && !GameSaver.isSaving()) {
				gameWorld.update(delta, 0, 0); //TODO
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
	}
}