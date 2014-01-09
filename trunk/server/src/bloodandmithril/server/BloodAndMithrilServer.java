package bloodandmithril.server;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.objenesis.strategy.StdInstantiatorStrategy;

import bloodandmithril.character.Individual.IndividualIdentifier;
import bloodandmithril.character.Individual.IndividualState;
import bloodandmithril.character.individuals.Elf;
import bloodandmithril.character.individuals.Names;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.csi.Request;
import bloodandmithril.item.equipment.Broadsword;
import bloodandmithril.item.equipment.ButterflySword;
import bloodandmithril.item.material.animal.ChickenLeg;
import bloodandmithril.item.material.plant.Carrot;
import bloodandmithril.persistence.GameLoader;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.Util;
import bloodandmithril.world.GameWorld;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
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

	private static GameWorld gameWorld;

	private static ExecutorService newCachedThreadPool;

	/**
	 * Entry point for the server
	 */
	public static void main(String[] args) {

		Server server = new Server(1048576, 1048576);
		server.start();

		try {
			server.bind(42685, 42686);
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
							if (request.tcp()) {
								connection.sendTCP(request.respond());
							} else {
								connection.sendUDP(request.respond());
							}
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


	private static class GameServer implements ApplicationListener, InputProcessor {

		@Override
		public void create() {
			gameWorld = new GameWorld(false);
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

		@Override
		public boolean keyDown(int keycode) {
			if (keycode == Input.Keys.R) {
				IndividualState state = new IndividualState(10f, 10f);
				state.position = new Vector2(0, 1000);
				state.velocity = new Vector2(0, 0);
				state.acceleration = new Vector2(0, 0);

				IndividualIdentifier id = Names.getRandomElfIdentifier(true, Util.getRandom().nextInt(100) + 50);
				id.nickName = "Elfie";

				Elf elf = new Elf(
					id, state, true, true,
					new Color(0.5f + 0.5f*Util.getRandom().nextFloat(), 0.5f + 0.5f*Util.getRandom().nextFloat(), 0.5f + 0.5f*Util.getRandom().nextFloat(), 1),
					new Color(0.2f + 0.4f*Util.getRandom().nextFloat(), 0.2f + 0.3f*Util.getRandom().nextFloat(), 0.5f + 0.3f*Util.getRandom().nextFloat(), 1),
					Util.getRandom().nextInt(4),
					20f
				);

				elf.giveItem(new Carrot(), Util.getRandom().nextInt(50));
				elf.giveItem(new ChickenLeg(), Util.getRandom().nextInt(50));
				elf.giveItem(new ButterflySword(100), 1);
				elf.giveItem(new Broadsword(100), 1);

				GameWorld.individuals.put(elf.id.id, elf);
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
		public boolean touchDown(int screenX, int screenY, int pointer, int button) {
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