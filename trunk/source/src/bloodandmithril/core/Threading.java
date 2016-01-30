package bloodandmithril.core;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.badlogic.gdx.Gdx;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.graphics.Graphics;
import bloodandmithril.graphics.particles.Particle;
import bloodandmithril.networking.ClientServerInterface;
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

	/**
	 * Constructor
	 */
	@Inject
	Threading(Graphics graphics) {
		if (ClientServerInterface.isClient()) {
			clientProcessingThreadPool = Executors.newCachedThreadPool();
		}

		updateThread = new Thread(() -> {
			long prevFrame = System.currentTimeMillis();

			while (true) {
				try {
					Thread.sleep(1);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

				if (System.currentTimeMillis() - prevFrame > Math.round(16f / BloodAndMithrilClient.getUpdateRate())) {
					prevFrame = System.currentTimeMillis();
					try {
						GameSaver.update();

						// Do not update if game is paused
						// Do not update if FPS is lower than tolerance threshold, otherwise bad things can happen, like teleporting
						if (!BloodAndMithrilClient.paused && !GameSaver.isSaving() && Domain.getActiveWorld() != null && !BloodAndMithrilClient.loading) {
							Domain.getActiveWorld().update();
						}
					} catch (Exception e) {
						e.printStackTrace();
						Gdx.app.exit();
					}
				}
			}
		});

		topographyQueryThread = new Thread(() -> {
			long prevFrame1 = System.currentTimeMillis();
			long prevFrame2 = System.currentTimeMillis();

			while (true) {
				try {
					Thread.sleep(2);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

				if (!BloodAndMithrilClient.isInGame()) {
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

		particleUpdateThread = new Thread(() -> {
			long prevFrame = System.currentTimeMillis();

			while (true) {
				try {
					Thread.sleep(1);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

				if (System.currentTimeMillis() - prevFrame > 16 && !BloodAndMithrilClient.rendering.get()) {
					prevFrame = System.currentTimeMillis();
					World world = Domain.getActiveWorld();
					if (world != null) {
						Collection<Particle> particles = world.getClientParticles();
						for (Particle p : particles) {
							if (p.getRemovalCondition().call()) {
								Domain.getActiveWorld().getClientParticles().remove(p);
							}
							try {
								p.update(0.012f);
							} catch (NoTileFoundException e) {}
						}
					}
				}
			}
		});


		updateThread.setPriority(Thread.MAX_PRIORITY);
		updateThread.setName("Update thread");
		updateThread.start();

		particleUpdateThread.setName("Particle thread");
		particleUpdateThread.start();

		topographyQueryThread.setName("Topography query thread");
		topographyQueryThread.start();
	}
}