package bloodandmithril.core;

import java.util.concurrent.atomic.AtomicBoolean;

import com.google.inject.Singleton;

/**
 * Tracks game client state
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class GameClientStateTracker {

	/** The game world */
	private AtomicBoolean inGame = new AtomicBoolean(false);

	/** True if game is paused */
	private AtomicBoolean paused = new AtomicBoolean(false);

	/** True if the world is currently being rendered */
	private AtomicBoolean rendering = new AtomicBoolean(false);

	/** True if game is loading */
	private AtomicBoolean loading = new AtomicBoolean(false);


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
}
