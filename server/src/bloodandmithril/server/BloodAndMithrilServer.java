package bloodandmithril.server;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.Executors;

import org.objenesis.strategy.StdInstantiatorStrategy;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Server;
import com.google.inject.Inject;

import bloodandmithril.character.faction.Faction;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.CommonModule;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.ServerModule;
import bloodandmithril.core.Wiring;
import bloodandmithril.generation.component.PrefabricatedComponent;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.persistence.GameLoader;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.persistence.GameSaver.PersistenceMetaData;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.prop.Prop;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.WorldUpdateService;

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
	public static void main(final String[] args) {

		ClientServerInterface.server = new Server(1048576, 1048576);
		final Server server = ClientServerInterface.server;
		ClientServerInterface.registerClasses(server.getKryo());
		server.start();

		try {
			server.bind(42685, 42686);
		} catch (final IOException e) {
			Logger.networkDebug(e.getMessage(), LogLevel.WARN);
		}

		ClientServerInterface.serverThread = Executors.newCachedThreadPool();

		((Kryo.DefaultInstantiatorStrategy) server.getKryo().getInstantiatorStrategy()).setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());
		server.addListener(new ServerListener(server));

		ClientServerInterface.syncThread = new Thread(
			new Runnable() {
				@Override
				public void run() {
					while (ClientServerInterface.server.getUpdateThread().isAlive()) {
						try {
							Thread.sleep(100);
							for (final Individual individual : Domain.getIndividuals()) {
								ClientServerInterface.SendNotification.notifyIndividualSync(individual.getId().getId());
							}

						} catch (final InterruptedException e) {
							Logger.generalDebug(e.getMessage(), LogLevel.WARN, e);
						}
						for (final Faction faction : Domain.getFactions().values()) {
							ClientServerInterface.SendNotification.notifySyncFaction(faction);
						}

						for (final int worldId : Domain.getAllWorldIds()) {
							ClientServerInterface.SendNotification.notifySyncItems(worldId);
							ClientServerInterface.SendNotification.notifySyncProjectiles(worldId);
							ClientServerInterface.SendNotification.notifySyncParticles(worldId);
							ClientServerInterface.SendNotification.notifySyncWorldState(worldId);

							for (final Prop prop : Domain.getWorld(worldId).props().getProps()) {
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
		final LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Blood and Mithril Server";
		cfg.width = 200;
		cfg.height = 20;
		cfg.resizable = true;

		new LwjglApplication(server, cfg);
	}

	public static class GameServer implements ApplicationListener {

		@Inject private GameSaver gameSaver;
		@Inject private GameLoader gameLoader;
		@Inject private ParameterPersistenceService parameterPersistenceService;
		@Inject private WorldUpdateService worldUpdateService;
		@Inject private GameClientStateTracker gameClientStateTracker;

		@Override
		public void create() {
			Wiring.setupInjector(new ServerModule(), new CommonModule());
			Wiring.injector().injectMembers(this);

			final Faction nature = new Faction("Nature", parameterPersistenceService.getParameters().getNextFactionId(), false, "");
			final Faction player = new Faction("Elves", parameterPersistenceService.getParameters().getNextFactionId(), true, "Elves are cool");
			Domain.addFaction(nature);
			Domain.addFaction(player);

			ClientServerInterface.setServer(true);
			gameLoader.load(new PersistenceMetaData("New game - " + new Date().toString()), true);
			gameClientStateTracker.setActiveWorldId(Domain.createWorld());

			PrefabricatedComponent.setup();
		}


		@Override
		public void resize(final int width, final int height) {
		}


		@Override
		public void render() {
			gameSaver.update();

			// Do not update if game is paused
			// Do not update if FPS is lower than tolerance threshold, otherwise
			// bad things can happen, like teleporting
			final float delta = Gdx.graphics.getDeltaTime();
			if (delta < 0.1f && !gameSaver.isSaving()) {
				final World activeWorld = gameClientStateTracker.getActiveWorld();
				if (activeWorld != null) {
					worldUpdateService.update(activeWorld);
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
	}
}