package bloodandmithril.ui.components.window;

import static bloodandmithril.core.BloodAndMithrilClient.HEIGHT;
import static bloodandmithril.core.BloodAndMithrilClient.WIDTH;
import static com.google.common.collect.Lists.newArrayList;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import bloodandmithril.core.Copyright;
import bloodandmithril.prop.Growable;
import bloodandmithril.prop.construction.farm.Farm;
import bloodandmithril.ui.Refreshable;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.util.Fonts;

import com.badlogic.gdx.graphics.Color;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

/**
 * Window to show the status of the current crop for a {@link Farm}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class CropWindow extends Window implements Refreshable {

	private final Farm farm;
	private ScrollableListingPanel<Growable, Boolean> cropListing;

	/**
	 * Constructor
	 */
	public CropWindow(Farm farm) {
		super(
			WIDTH/2 - 150,
			HEIGHT/2 + 200,
			300,
			400,
			farm.getTitle() + " - Current crop",
			true,
			300,
			400,
			true,
			true,
			true
		);
		this.farm = farm;
		this.cropListing = new ScrollableListingPanel<Growable, Boolean>(
			this,
			(h1, h2) -> {
				return h1.getClass().getSimpleName().compareTo(h2.getClass().getSimpleName());
			},
			false,
			35
		) {
			@Override
			protected String getExtraString(Entry<ScrollableListingPanel.ListingMenuItem<Growable>, Boolean> item) {
				return item.getValue() ? "Growing" : "Not Growing";
			}


			@Override
			protected int getExtraStringOffset() {
				return 135;
			}


			@Override
			protected void onSetup(List<HashMap<ScrollableListingPanel.ListingMenuItem<Growable>, Boolean>> listings) {
				listings.add(buildMap());
			}


			@Override
			public boolean keyPressed(int keyCode) {
				return false;
			}
		};
	}


	private HashMap<ScrollableListingPanel.ListingMenuItem<Growable>, Boolean> buildMap() {
		HashMap<ScrollableListingPanel.ListingMenuItem<Growable>, Boolean> map = Maps.newHashMap();

		farm.getGrowables().stream().forEach(growable -> {
			map.put(
				new ScrollableListingPanel.ListingMenuItem<>(
					growable,
					new Button(
						growable.harvest().getSingular(true),
						Fonts.defaultFont,
						0,
						0,
						growable.harvest().getSingular(true).length() * 10,
						16,
						() -> {
							farm.getCurrentCrop().add(growable);
							refresh();
						},
						Color.WHITE,
						Color.GREEN,
						Color.GRAY,
						UIRef.BL
					),
					null
				),
				Iterables.tryFind(farm.getCurrentCrop(), crop -> {
					return crop.getClass().equals(growable.getClass());
				}).isPresent()
			);
		});

		return map;
	}


	@Override
	protected void internalWindowRender() {
		cropListing.x = x;
		cropListing.y = y;
		cropListing.width = width;
		cropListing.height = height;

		cropListing.render();
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		cropListing.leftClick(copy, windowsCopy);
	}


	@Override
	protected void uponClose() {
	}


	@Override
	public Object getUniqueIdentifier() {
		return "CropWindow" + farm.id;
	}


	@Override
	public void leftClickReleased() {
		cropListing.leftClickReleased();
	}


	@Override
	@SuppressWarnings("unchecked")
	public void refresh() {
		cropListing.refresh(newArrayList(buildMap()));
	}
}