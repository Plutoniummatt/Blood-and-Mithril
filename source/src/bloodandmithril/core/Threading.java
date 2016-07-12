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

	@Inject private GameSaver gameSaver;
	@Inject private GameClientStateTracker gameClientStateTracker;

	/**
	 * Constructor
	 */
	@Inject
	Threading(final Graphics graphics) {
		setupEventProcessingThread();
		setupUpdateThread();
		setupTopographyQueryThread(graphics);
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
					final World world = Domain.getActiveWorld();
					if (world != null) {
						final Collection<Particle> particles = world.getClientParticles();
						for (final Particle p : particles) {
							if (p.getRemovalCondition().call()) {
								Domain.getActiveWorld().getClientParticles().remove(p);
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


	private void setupTopographyQueryThread(final Graphics graphics) {
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
					if (Domain.getActiveWorld() != null) {
						Domain.getActiveWorld().getTopography().loadOrGenerateNullChunksAccordingToPosition(
							(int) graphics.getCam().position.x,
							(int) graphics.getCam().position.y
						);
					}
					prevFrame1 = System.currentTimeMillis();
				}

				if (System.currentTimeMillis() - prevFrame2 > 200) {
					if (Domain.getActiveWorld() != null) {
						Domain.getIndividuals().values().stream().forEach(individual -> {
							Domain.getActiveWorld().getTopography().loadOrGenerateNullChunksAccordingToPosition(
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

				if (System.currentTimeMillis() - prevFrame > Math.round(16f / BloodAndMithrilClient.getUpdateRate())) {
					prevFrame = System.currentTimeMillis();
					try {
						gameSaver.update();

						// Do not update if game is paused
						// Do not update if FPS is lower than tolerance threshold, otherwise bad things can happen, like teleporting
						if (!gameClientStateTracker.isPaused() && !gameSaver.isSaving() && Domain.getActiveWorld() != null && !gameClientStateTracker.isLoading()) {
							Domain.getActiveWorld().update();
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

				for (final World world : Domain.getWorlds().values()) {
					while (!world.getEvents().isEmpty()) {
						final Event polled = world.getEvents().poll();
						for (final EventListener listener : BloodAndMithrilClient.getMissions()) {
							listener.listen(polled);
						}
					}
				}

				for (final Mission m : BloodAndMithrilClient.getMissions()) {
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
}