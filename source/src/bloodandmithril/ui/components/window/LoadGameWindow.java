package bloodandmithril.ui.components.window;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.badlogic.gdx.graphics.Color;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
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
			protected String getExtraString(Entry<ScrollableListingPanel.ListingMenuItem<PersistenceMetaData>, Date> item) {
				return dateFormat.format(item.getValue());
			}


			@Override
			protected int getExtraStringOffset() {
				return 200;
			}


			@Override
			protected void populateListings(List<HashMap<ScrollableListingPanel.ListingMenuItem<PersistenceMetaData>, Date>> listings) {
				populateList(listings);
			}


			@Override
			public boolean keyPressed(int keyCode) {
				return false;
			}
		};
	}


	private void populateList(List<HashMap<ScrollableListingPanel.ListingMenuItem<PersistenceMetaData>, Date>> listings) {
		HashMap<ScrollableListingPanel.ListingMenuItem<PersistenceMetaData>, Date> mapToAdd = Maps.newHashMap();

		for (PersistenceMetaData metadata : gameLoader.loadMetaData()) {
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
									graphics.setFading(true);
									MainMenuWindow.removeWindows();
									BloodAndMithrilClient.threadWait(2000);
									BloodAndMithrilClient.setLoading(true);
									gameLoader.load(metadata, false);
									BloodAndMithrilClient.setInGame(true);
									BloodAndMithrilClient.setup();

									while(!chunkLoader.loaderTasks.isEmpty()) {
										BloodAndMithrilClient.threadWait(100);
									}

									BloodAndMithrilClient.setLoading(false);
									BloodAndMithrilClient.threadWait(2000);
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
	protected void internalWindowRender(Graphics graphics) {
		savedGames.height = height;
		savedGames.width = width;
		savedGames.x = x;
		savedGames.y = y;

		savedGames.render(graphics);
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
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