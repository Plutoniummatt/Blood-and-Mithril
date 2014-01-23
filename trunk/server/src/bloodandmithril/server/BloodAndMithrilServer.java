package bloodandmithril.server;

import java.io.IOException;
import java.util.concurrent.Executors;

import org.objenesis.strategy.StdInstantiatorStrategy;

import bloodandmithril.character.Individual;
import bloodandmithril.character.Individual.IndividualIdentifier;
import bloodandmithril.character.Individual.IndividualState;
import bloodandmithril.character.conditions.Poison;
import bloodandmithril.character.individuals.Boar;
import bloodandmithril.character.individuals.Elf;
import bloodandmithril.character.individuals.Names;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.item.equipment.Broadsword;
import bloodandmithril.item.equipment.ButterflySword;
import bloodandmithril.item.material.animal.ChickenLeg;
import bloodandmithril.item.material.container.GlassBottle;
import bloodandmithril.item.material.liquid.Liquid.Water;
import bloodandmithril.item.material.plant.Carrot;
import bloodandmithril.item.material.plant.DeathCap;
import bloodandmithril.item.misc.Currency;
import bloodandmithril.persistence.GameLoader;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.building.PineChest;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.Util;
import bloodandmithril.world.Epoch;
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

	/**0
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
									for (Connection c : server.getConnections()) {
										c.sendTCP(responseToSend);
									}
								} else {
									connection.sendTCP(request.respond());
								}
							} else {
								if (request.notifyOthers()) {
									Responses responseToSend = request.respond();
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
								ClientServerInterface.SendNotification.notifyIndividualSync(individual.id.id);
							}
						} catch (InterruptedException e) {
						}

						if (counter % 10 == 0) {
						  for (Prop prop : GameWorld.props.values()) {
						    ClientServerInterface.SendNotification.notifySyncProp(prop);
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

		new LwjglApplication(new GameServer(), cfg);
	}


	private static class GameServer implements ApplicationListener, InputProcessor {

		@Override
		public void create() {
			gameWorld = new GameWorld(false);
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
			// Do not update if FPS is lower than tolerance threshold, otherwise bad things can happen, like teleporting
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

	    if (keycode == Input.Keys.P) {
	      GameWorld.individuals.get(1).addCondition(new Poison(1f, 0.1f));
	    }

	    if (keycode == Input.Keys.T) {
	      Individual individual = GameWorld.individuals.get(1);
	      if (individual != null) {
	        PineChest pineChest = new PineChest(individual.state.position.x, individual.state.position.y, true, 100f);
	        GameWorld.props.put(pineChest.id, pineChest);
	      }
	    }

	    if (keycode == Input.Keys.U) {
	      IndividualState state = new IndividualState(10f, 10f);
	      state.position = new Vector2(0, 500);
	      state.velocity = new Vector2(0, 0);
	      state.acceleration = new Vector2(0, 0);

	      IndividualIdentifier id = new IndividualIdentifier("Unknown", "", new Epoch(10f, 12, 12, 2012));
	      id.nickName = "Unknown";

	      Boar boar = new Boar(id, state);

	      GameWorld.individuals.put(boar.id.id, boar);
	    }

			if (keycode == Input.Keys.R) {
				IndividualState state = new IndividualState(10f, 10f);
	      state.stamina = 1f;
	      state.staminaRegen = 1f;
	      state.hunger = 1f;
	      state.thirst = 1f;
	      state.healthRegen = 0.01f;
				state.position = new Vector2(0, 500);
				state.velocity = new Vector2(0, 0);
				state.acceleration = new Vector2(0, 0);

				IndividualIdentifier id = Names.getRandomElfIdentifier(true, Util.getRandom().nextInt(100) + 50);
				id.nickName = "Elfie";

				Elf elf = new Elf(
					id, state, Gdx.input.isKeyPressed(Input.Keys.Q), true,
					new Color(0.5f + 0.5f*Util.getRandom().nextFloat(), 0.5f + 0.5f*Util.getRandom().nextFloat(), 0.5f + 0.5f*Util.getRandom().nextFloat(), 1),
					new Color(0.2f + 0.4f*Util.getRandom().nextFloat(), 0.2f + 0.3f*Util.getRandom().nextFloat(), 0.5f + 0.3f*Util.getRandom().nextFloat(), 1),
					Util.getRandom().nextInt(4),
					20f
				);

	      for (int i = Util.getRandom().nextInt(50); i > 0; i--) {
	        elf.giveItem(new Carrot());
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