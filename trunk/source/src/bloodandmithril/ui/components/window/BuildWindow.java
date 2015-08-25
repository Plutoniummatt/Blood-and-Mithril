package bloodandmithril.ui.components.window;

import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.util.cursorboundtask.PlaceCursorBoundTask;

import com.badlogic.gdx.graphics.Color;
import com.google.common.base.Function;
import com.google.common.collect.Maps;

/**
 * Listing window to display a lsit of {@link Construction}s to be built by some {@link Individual}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class BuildWindow extends ScrollableListingWindow<Construction, String> {

	private final Individual builder;

	/**
	 * Constructor
	 */
	public BuildWindow(
			Individual builder,
			Function<Construction, String> displayFunction,
			Comparator<Construction> sortingOrder) {
		super(
			300,
			200,
			"Build",
			true,
			300,
			200,
			true,
			true,
			buildMap(builder),
			construction -> construction.getTitle(),
			sortingOrder
		);

		this.builder = builder;
	}


	private static Map<Construction, String> buildMap(Individual builder) {
		Map<Construction, String> listing = Maps.newHashMap();

		for (Construction construction : builder.getConstructables()) {
			float time = construction.constructionRate == 0f ? 0f : 1f/construction.constructionRate;
			listing.put(construction, String.format("%.1f", time) + "s");
		}
		return listing;
	}


	@Override
	public void refresh() {
		buildListing(
			buildMap(builder),
			(a1, a2) -> {
				return a1.getTitle().compareTo(a2.getTitle());
			}
		);
	}


	@Override
	protected ContextMenu buttonContextMenu(Entry<Construction, String> tEntry) {
		return new ContextMenu(
			getMouseScreenX(),
			getMouseScreenY(),
			true,
			new ContextMenu.MenuItem(
				"Show info",
				() -> {
					UserInterface.addLayeredComponentUnique(
						tEntry.getKey().getInfoWindow()
					);
				},
				Color.WHITE,
				Color.GREEN,
				Color.GRAY,
				null
			),
			new ContextMenu.MenuItem(
				"Construct",
				() -> {
					Construction toConstruct = tEntry.getKey();
					toConstruct.setWorldId(builder.getWorldId());
					BloodAndMithrilClient.setCursorBoundTask(
						new PlaceCursorBoundTask(toConstruct, null, null)
					);
					this.setClosing(true);
				},
				Color.WHITE,
				Color.GREEN,
				Color.GRAY,
				null
			)
		);
	}


	@Override
	public Object getUniqueIdentifier() {
		return builder.getId().getId() + "BuildMenu";
	}
}