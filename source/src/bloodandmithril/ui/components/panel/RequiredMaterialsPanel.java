package bloodandmithril.ui.components.panel;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.graphics.Color;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.util.Fonts;

/**
 * Panel to display required materials
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class RequiredMaterialsPanel extends ScrollableListingPanel<Item, String> {

	@Inject private UserInterface userInterface;

	private Container materialsContainer;
	private Map<Item, Integer> requiredMaterials;

	/**
	 * Constructor
	 */
	public RequiredMaterialsPanel(final Component parent, final Container materialsContainer, final Map<Item, Integer> requiredMaterials) {
		super(
			parent,
			new Comparator<Item>() {
				@Override
				public int compare(final Item o1, final Item o2) {
					return o1.getSingular(false).compareTo(o2.getSingular(false));
				}
			},
			false,
			35,
			null
		);
		this.materialsContainer = materialsContainer;
		this.requiredMaterials = requiredMaterials;

		getListing().add(constructListing(getConstructionMaterialStatus(materialsContainer, requiredMaterials)));
	}


	/**
	 * @return the amount of materials currently assigned to this construction, as well as the total required.
	 */
	private static HashMap<Item, String> getConstructionMaterialStatus(final Container materialsContainer, final Map<Item, Integer> requiredMaterials) {
		final HashMap<Item, String> map = newHashMap();

		for (final Entry<Item, Integer> entry : requiredMaterials.entrySet()) {
			final Integer numberOfItemsInMaterialContainer = Iterables.find(
				materialsContainer.getInventory().entrySet(),
				new Predicate<Entry<Item, Integer>>() {
					@Override
					public boolean apply(final Entry<Item, Integer> invEntry) {
						return entry.getKey().sameAs(invEntry.getKey());
					}
				},
				new Entry<Item, Integer>() {
					@Override
					public Item getKey() {
						throw new UnsupportedOperationException();
					}
					@Override
					public Integer getValue() {
						return 0;
					}
					@Override
					public Integer setValue(final Integer arg0) {
						throw new UnsupportedOperationException();
					}
				}
			).getValue();
			map.put(entry.getKey(), (numberOfItemsInMaterialContainer == null ? "0" : numberOfItemsInMaterialContainer.toString()) + "/" + entry.getValue().toString());
		}

		return map;
	}


	@Override
	protected String getExtraString(final Entry<ListingMenuItem<Item>, String> item) {
		return item.getValue();
	}


	@Override
	protected int getExtraStringOffset() {
		return 100;
	}


	public void refresh() {
		getListing().clear();
		getListing().add(constructListing(getConstructionMaterialStatus(materialsContainer, getRequiredMaterials())));
	}


	@Override
	protected void populateListings(final List<HashMap<ListingMenuItem<Item>, String>> listings) {
	}


	private HashMap<ListingMenuItem<Item>, String> constructListing(final HashMap<Item, String> constructionMaterialStatus) {
		final HashMap <ListingMenuItem<Item>, String> map = Maps.newHashMap();

		constructionMaterialStatus.entrySet().stream().forEach(entry -> {
			map.put(
				new ListingMenuItem<Item>(
					entry.getKey(),
					new Button(
						entry.getKey().getSingular(true),
						Fonts.defaultFont,
						0,
						0,
						entry.getKey().getSingular(true).length() * 10,
						16,
						() -> {},
						Color.WHITE,
						Color.GREEN,
						Color.WHITE,
						UIRef.BL
					),
					() -> { return new ContextMenu(
						0, 0,
						true,
						new MenuItem(
							"Show info",
							() -> {
								userInterface.addLayeredComponentUnique(entry.getKey().getInfoWindow());
							},
							Color.WHITE,
							Color.GREEN,
							Color.WHITE,
							null
						)
					);}
				),
				entry.getValue()
			);
		});

		return map;
	}


	@Override
	public boolean keyPressed(final int keyCode) {
		return false;
	}


	public Map<Item, Integer> getRequiredMaterials() {
		return requiredMaterials;
	}
}