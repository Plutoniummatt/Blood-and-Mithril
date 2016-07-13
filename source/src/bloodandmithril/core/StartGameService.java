package bloodandmithril.core;

import static bloodandmithril.util.Util.threadWait;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Iterables.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.faction.Faction;
import bloodandmithril.character.faction.FactionControlService;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.control.BloodAndMithrilClientInputProcessor;
import bloodandmithril.generation.Structures;
import bloodandmithril.generation.superstructure.SuperStructure;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.persistence.GameLoader;
import bloodandmithril.persistence.GameSaver.PersistenceMetaData;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.persistence.world.ChunkLoader;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.util.cursorboundtask.ChooseStartingLocationCursorBoundTask;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;

/**
 * Service to start a new single player game
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class StartGameService {

	@Inject	private Threading threading;
	@Inject	private Graphics graphics;
	@Inject	private ParameterPersistenceService parameterPersistenceService;
	@Inject	private FactionControlService factionControlService;
	@Inject	private GameLoader gameLoader;
	@Inject	private ChunkLoader chunkLoader;
	@Inject private GameSetupService gameSetupService;
	@Inject private GameClientStateTracker gameClientStateTracker;

	/**
	 * Starts the game
	 */
	public void start(
		final Class<? extends Individual> selectedRace,
		final ItemPackage selectedItemPackage,
		final HashMap<ListingMenuItem<Individual>, String> startingIndividuals
	) {
		final Faction playerFaction = setupFactions(selectedRace);
		reconfigureGameClient();

		threading.clientProcessingThreadPool.execute(() -> {
			closeWindowsAndFadeToBlack();
			setup();
			setStartingLocation();
			setCursorBoundTask(selectedItemPackage, startingIndividuals, playerFaction);
			waitForFinish();
			finish();
		});
	}


	private void waitForFinish() {
		while(!chunkLoader.loaderTasks.isEmpty()) {
			threadWait(100);
		}
	}


	private void finish() {
		gameClientStateTracker.setLoading(false);
		graphics.setFading(false);
	}


	private void setCursorBoundTask(final ItemPackage selectedItemPackage,
			final HashMap<ListingMenuItem<Individual>, String> startingIndividuals, final Faction playerFaction) {
		Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).setCursorBoundTask(
			new ChooseStartingLocationCursorBoundTask(
				Sets.newHashSet(filter(Lists.newArrayList(transform(startingIndividuals.keySet(), listingMenuItem -> {
					return listingMenuItem.t;
				})), test -> {
					return test != null;
				})),
				selectedItemPackage,
				playerFaction.factionId,
				gameClientStateTracker.getActiveWorld().getWorldId()
			)
		);
	}


	private void setStartingLocation() {
		SuperStructure superStructure = null;
		while (superStructure == null || superStructure.getPossibleStartingLocations().isEmpty()) {
			threadWait(1000);

			superStructure = (SuperStructure) Iterables.tryFind(Structures.getStructures().values(), structure -> {
				return structure instanceof SuperStructure;
			}).orNull();
		}

		final ArrayList<Vector2> startingLocations = Lists.newArrayList(superStructure.getPossibleStartingLocations());
		Collections.shuffle(startingLocations);
		final Vector2 startingPosition = startingLocations.get(0);

		graphics.getCam().position.x = startingPosition.x;
		graphics.getCam().position.y = startingPosition.y;
	}


	private void setup() {
		// Wire up the game
		gameClientStateTracker.setLoading(true);
		gameLoader.load(new PersistenceMetaData("New game - " + new Date().toString()), true);
		gameClientStateTracker.setActiveWorldId(Domain.createWorld());
		gameClientStateTracker.setInGame(true);
		gameSetupService.setup();

		// Generate first chunk
		final Topography topography = gameClientStateTracker.getActiveWorld().getTopography();
		topography.loadOrGenerateChunk(0, 0, false);
	}


	private void closeWindowsAndFadeToBlack() {
		UserInterface.closeAllWindows();
		graphics.setFading(true);
		threadWait(1500);
	}


	private void reconfigureGameClient() {
		// Single player game, we're effectively the "server"
		ClientServerInterface.setServer(true);

		// "Install" the ClientModule
		Wiring.reconfigure(new ClientModule());
	}


	private Faction setupFactions(final Class<? extends Individual> selectedRace) {
		final Faction nature = new Faction(
			"Nature",
			parameterPersistenceService.getParameters().getNextFactionId(),
			false,
			"Mother nature"
		);

		final Faction playerFaction = new Faction(
			selectedRace.getAnnotation(Name.class).name(),
			parameterPersistenceService.getParameters().getNextFactionId(),
			true,
			selectedRace.getAnnotation(Description.class).description()
		);

		Domain.addFaction(nature);
		Domain.addFaction(playerFaction);
		factionControlService.control(playerFaction.factionId);

		return playerFaction;
	}
}