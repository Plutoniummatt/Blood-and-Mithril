package bloodandmithril.core;

import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.individuals.Individual;
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

	/** {@link Individual} that are selected for manual control */
	private static Set<Integer> selectedIndividuals = newHashSet();


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


	public synchronized void addSelectedIndividual(final Individual individual) {
		selectedIndividuals.add(individual.getId().getId());
	}


	public synchronized boolean removeSelectedIndividual(final Individual individual) {
		return selectedIndividuals.removeIf(id -> {
			return individual.getId().getId() == id;
		});
	}


	public synchronized boolean removeSelectedIndividualIf(final java.util.function.Predicate<Integer> predicate) {
		return selectedIndividuals.removeIf(predicate);
	}


	public synchronized boolean isIndividualSelected(final Individual individual) {
		return selectedIndividuals.contains(individual.getId().getId());
	}


	public synchronized void clearSelectedIndividuals() {
		selectedIndividuals.clear();
	}


	public synchronized Collection<Individual> getSelectedIndividuals() {
		return Lists.newLinkedList(Iterables.transform(selectedIndividuals, id -> {
			return Domain.getIndividual(id);
		}));
	}
}
