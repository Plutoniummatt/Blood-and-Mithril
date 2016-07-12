package bloodandmithril.core;

import java.util.concurrent.atomic.AtomicBoolean;

import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.control.CameraTracker;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;

/**
 * Tracks game client state
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class GameClientStateTracker {

	@Inject private Graphics graphics;
	@Inject private CameraTracker cameraTracker;

	/** The game world */
	private AtomicBoolean inGame = new AtomicBoolean(false);

	/** True if game is paused */
	private AtomicBoolean paused = new AtomicBoolean(false);

	/** True if the world is currently being rendered */
	private AtomicBoolean rendering = new AtomicBoolean(false);

	/** True if game is loading */
	private AtomicBoolean loading = new AtomicBoolean(false);

	/** The world that's currently selected */
	private Integer selectedActiveWorldId;


	public boolean isLoading() {
		return loading.get();
	}


	public boolean isInGame() {
		return inGame.get();
	}


	public boolean isPaused() {
		return paused.get();
	}


	public boolean isRendering() {
		return rendering.get();
	}


	public void setPaused(final boolean paused) {
		this.paused.set(paused);
	}


	public void setLoading(final boolean loading) {
		this.loading.set(loading);
	}


	public void setInGame(final boolean inGame) {
		this.inGame.set(inGame);
	}


	public void setRendering(final boolean rendering) {
		this.rendering.set(rendering);
	}


	public Integer getSelectedActiveWorldId() {
		return selectedActiveWorldId;
	}


	public void setSelectedActiveWorldId(final int selectedActiveWorldId) {
		if (ClientServerInterface.isClient()) {
			if (getActiveWorld() != null) {
				cameraTracker.getWorldcamcoordinates().put(selectedActiveWorldId, new Vector2(graphics.getCam().position.x, graphics.getCam().position.y));
			}

			if (cameraTracker.getWorldcamcoordinates().containsKey(selectedActiveWorldId)) {
				final Vector2 camPosition = cameraTracker.getWorldcamcoordinates().get(selectedActiveWorldId);

				graphics.getCam().position.x = camPosition.x;
				graphics.getCam().position.y = camPosition.y;
			} else {
				cameraTracker.getWorldcamcoordinates().put(selectedActiveWorldId, new Vector2());

				graphics.getCam().position.x = 0;
				graphics.getCam().position.y = 0;
			}
		}
		this.selectedActiveWorldId = selectedActiveWorldId;
	}


	public World getActiveWorld() {
		return Domain.getWorld(selectedActiveWorldId);
	}
}
