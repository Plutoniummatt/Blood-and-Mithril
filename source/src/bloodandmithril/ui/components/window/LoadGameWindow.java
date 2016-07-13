package bloodandmithril.ui.components.window;

import static bloodandmithril.util.Util.threadWait;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.badlogic.gdx.graphics.Color;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import bloodandmithril.core.ClientModule;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.GameSetupService;
import bloodandmithril.core.Threading;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.persistence.GameLoader;
import bloodandmithril.persistence.GameSaver.PersistenceMetaData;
import bloodandmithril.persistence.world.ChunkLoader;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.util.Fonts;

/**
 * UI responsible for loading games.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class LoadGameWindow extends Window {

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private ScrollableListingPanel<PersistenceMetaData, Date> savedGames;

	@Inject	private Threading threading;
	@Inject	private Graphics graphics;
	@Inject	private GameLoader gameLoader;
	@Inject	private ChunkLoader chunkLoader;
	@Inject private GameSetupService gameSetupService;
	@Inject private GameClientStateTracker gameClientStateTracker;

	/**
	 * Constructor
	 */
	public LoadGameWindow() {
		super(
			400,
			200,
			"Load game",
			true,
			300,
			200,
			false,
			true,
			true
		);

		Wiring.injector().injectMembers(this);
		setupListing();
	}


	private void setupListing() {
		this.savedGames = new ScrollableListingPanel<PersistenceMetaData, Date>(
			this,
			(s1, s2) -> {
				return s1.date.compareTo(s2.date);
			},
			false,
			200,
			null
		) {
			@Override
			protected String getExtraString(final Entry<ScrollableListingPanel.ListingMenuItem<PersistenceMetaData>, Date> item) {
				return dateFormat.format(item.getValue());
			}


			@Override
			protected int getExtraStringOffset() {
				return 200;
			}


			@Override
			protected void populateListings(final List<HashMap<ScrollableListingPanel.ListingMenuItem<PersistenceMetaData>, Date>> listings) {
				populateList(listings);
			}


			@Override
			public boolean keyPressed(final int keyCode) {
				return false;
			}
		};
	}


	private void populateList(final List<HashMap<ScrollableListingPanel.ListingMenuItem<PersistenceMetaData>, Date>> listings) {
		final HashMap<ScrollableListingPanel.ListingMenuItem<PersistenceMetaData>, Date> mapToAdd = Maps.newHashMap();

		for (final PersistenceMetaData metadata : gameLoader.loadMetaData()) {
			mapToAdd.put(
				new ScrollableListingPanel.ListingMenuItem<PersistenceMetaData>(
					metadata,
					new Button(
						metadata.name,
						Fonts.defaultFont,
						0,
						0,
						metadata.name.length() * 9,
						16,
						() -> {
							threading.clientProcessingThreadPool.execute(
								() -> {
									setClosing(true);
									ClientServerInterface.setServer(true);
									Wiring.reconfigure(new ClientModule());
									graphics.setFading(true);
									MainMenuWindow.removeWindows();
									threadWait(2000);
									gameClientStateTracker.setLoading(true);
									gameLoader.load(metadata, false);
									gameClientStateTracker.setInGame(true);
									gameSetupService.setup();

									while(!chunkLoader.loaderTasks.isEmpty()) {
										threadWait(100);
									}

									gameClientStateTracker.setLoading(false);
									threadWait(2000);
									graphics.setFading(false);
								}
							);
						},
						Color.ORANGE,
						Color.GREEN,
						Color.ORANGE,
						UIRef.BL
					),
					null
				),
				metadata.date
			);
		}

		listings.add(mapToAdd);
	}


	@Override
	protected void internalWindowRender(final Graphics graphics) {
		savedGames.height = height;
		savedGames.width = width;
		savedGames.x = x;
		savedGames.y = y;

		savedGames.render(graphics);
	}


	@Override
	protected void internalLeftClick(final List<ContextMenu> copy, final Deque<Component> windowsCopy) {
		savedGames.leftClick(copy, windowsCopy);
	}


	@Override
	protected void uponClose() {
	}


	@Override
	public Object getUniqueIdentifier() {
		return LoadGameWindow.class;
	}


	@Override
	public void leftClickReleased() {
		savedGames.leftClickReleased();
	}
}