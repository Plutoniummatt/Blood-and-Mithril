package bloodandmithril.ui.components.window;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.graphics.Color;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.control.BloodAndMithrilClientInputProcessor;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.util.cursorboundtask.PlaceCursorBoundTask;

/**
 * Listing window to display a lsit of {@link Construction}s to be built by some {@link Individual}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class BuildWindow extends ScrollableListingWindow<Construction, String> {

	@Inject private UserInterface userInterface;

	private final Individual builder;

	/**
	 * Constructor
	 */
	public BuildWindow(
			final Individual builder,
			final Function<Construction, String> displayFunction,
			final Comparator<Construction> sortingOrder) {
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


	private static Map<Construction, String> buildMap(final Individual builder) {
		final Map<Construction, String> listing = Maps.newHashMap();

		for (final Construction construction : builder.getConstructables()) {
			final float time = construction.constructionRate == 0f ? 0f : 1f/construction.constructionRate;
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
	protected ContextMenu buttonContextMenu(final Entry<Construction, String> tEntry) {
		return new ContextMenu(
			getMouseScreenX(),
			getMouseScreenY(),
			true,
			new ContextMenu.MenuItem(
				"Show info",
				() -> {
					userInterface.addLayeredComponentUnique(
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
					final Construction toConstruct = tEntry.getKey();
					toConstruct.setWorldId(builder.getWorldId());
					Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).setCursorBoundTask(
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
	protected void internalWindowRender(final Graphics graphics) {
		if (!builder.isAlive()) {
			setClosing(true);
		}
		super.internalWindowRender(graphics);
	};


	@Override
	public Object getUniqueIdentifier() {
		return builder.getId().getId() + "BuildMenu";
	}
}