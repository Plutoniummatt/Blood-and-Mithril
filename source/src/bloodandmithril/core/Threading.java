package bloodandmithril.core;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.badlogic.gdx.Gdx;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.event.Event;
import bloodandmithril.event.EventListener;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.graphics.particles.Particle;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.objectives.Mission;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.WorldUpdateService;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * Threading class
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2015")
public class Threading {

	/** Client-side {@link ExecutorService} */
	public ExecutorService clientProcessingThreadPool;

	/** The main game loop thread, handles logic only, no graphics related processing */
	public Thread updateThread;

	/** This thread is responsible for adding chunks that are on screen to the topography loading thread */
	public Thread topographyQueryThread;

	/** Handles the update of particles */
	public Thread particleUpdateThread;

	/** Thread used for saving/loading */
	public Thread persistenceThread;

	/** Thread responsible for processing events */
	private Thread eventsProcessingThread;

	/** Update rate multiplier */
	private float updateRate = 1.0f;

	private final Graphics graphics;
	private final GameSaver gameSaver;
	private final GameClientStateTracker gameClientStateTracker;
	private final MissionTracker missionTracker;
	private final WorldUpdateService worldUpdateService;

	/**
	 * Constructor
	 */
	@Inject
	Threading(
		final Graphics graphics,
		final GameSaver gameSaver,
		final GameClientStateTracker gameClientStateTracker,
		final MissionTracker missionTracker,
		final WorldUpdateService worldUpdateService
	) {
		this.graphics = graphics;
		this.gameSaver = gameSaver;
		this.gameClientStateTracker = gameClientStateTracker;
		this.missionTracker = missionTracker;
		this.worldUpdateService = worldUpdateService;

		setupEventProcessingThread();
		setupUpdateThread();
		setupTopographyQueryThread();
		setupParticleUpdateThread();
	}


	private void setupParticleUpdateThread() {
		particleUpdateThread = new Thread(() -> {
			long prevFrame = System.currentTimeMillis();

			while (true) {
				try {
					Thread.sleep(1);
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}

				if (System.currentTimeMillis() - prevFrame > 16 && !gameClientStateTracker.isRendering()) {
					prevFrame = System.currentTimeMillis();
					final World world = gameClientStateTracker.getActiveWorld();
					if (world != null) {
						final Collection<Particle> particles = world.getClientParticles();
						for (final Particle p : particles) {
							if (p.getRemovalCondition().call()) {
								gameClientStateTracker.getActiveWorld().getClientParticles().remove(p);
							}
							try {
								p.update(0.012f);
							} catch (final NoTileFoundException e) {}
						}
					}
				}
			}
		});

		particleUpdateThread.start();
	}


	private void setupTopographyQueryThread() {
		topographyQueryThread = new Thread(() -> {
			long prevFrame1 = System.currentTimeMillis();
			long prevFrame2 = System.currentTimeMillis();

			while (true) {
				try {
					Thread.sleep(2);
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}

				if (!gameClientStateTracker.isInGame()) {
					continue;
				}

				if (System.currentTimeMillis() - prevFrame1 > 16) {
					if (gameClientStateTracker.getActiveWorld() != null) {
						gameClientStateTracker.getActiveWorld().getTopography().loadOrGenerateNullChunksAccordingToPosition(
							(int) graphics.getCam().position.x,
							(int) graphics.getCam().position.y
						);
					}
					prevFrame1 = System.currentTimeMillis();
				}

				if (System.currentTimeMillis() - prevFrame2 > 200) {
					if (gameClientStateTracker.getActiveWorld() != null) {
						Domain.getIndividuals().stream().forEach(individual -> {
							gameClientStateTracker.getActiveWorld().getTopography().loadOrGenerateNullChunksAccordingToPosition(
								(int) individual.getState().position.x,
								(int) individual.getState().position.y
							);
						});
					}
					prevFrame2 = System.currentTimeMillis();
				}
			}
		});

		topographyQueryThread.setName("Topography query thread");
		topographyQueryThread.start();
	}


	private void setupUpdateThread() {
		updateThread = new Thread(() -> {
			long prevFrame = System.currentTimeMillis();

			while (true) {
				try {
					Thread.sleep(1);
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}

				if (System.currentTimeMillis() - prevFrame > Math.round(16f / updateRate)) {
					prevFrame = System.currentTimeMillis();
					try {
						gameSaver.update();

						// Do not update if game is paused
						// Do not update if FPS is lower than tolerance threshold, otherwise bad things can happen, like teleporting
						if (!gameClientStateTracker.isPaused() && !gameSaver.isSaving() && gameClientStateTracker.getActiveWorld() != null && !gameClientStateTracker.isLoading()) {
							worldUpdateService.update(gameClientStateTracker.getActiveWorld());
						}
					} catch (final Exception e) {
						e.printStackTrace();
						Gdx.app.exit();
					}
				}
			}
		});

		updateThread.setPriority(Thread.MAX_PRIORITY);
		updateThread.setName("Update thread");
		updateThread.start();
	}


	private void setupEventProcessingThread() {
		eventsProcessingThread = new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(250);
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}

				for (final World world : Domain.getAllWorlds()) {
					while (!world.getEvents().isEmpty()) {
						final Event polled = world.getEvents().poll();
						for (final EventListener listener : missionTracker.getMissions()) {
							listener.listen(polled);
						}
					}
				}

				for (final Mission m : missionTracker.getMissions()) {
					m.update();
				}
			}
		});

		eventsProcessingThread.setDaemon(false);
		eventsProcessingThread.setName("Events");
		eventsProcessingThread.start();

		if (ClientServerInterface.isClient()) {
			clientProcessingThreadPool = Executors.newCachedThreadPool();
		}
	}


	public float getUpdateRate() {
		return updateRate;
	}


	public void setUpdateRate(final float updateRate) {
		this.updateRate = updateRate;
	}
}