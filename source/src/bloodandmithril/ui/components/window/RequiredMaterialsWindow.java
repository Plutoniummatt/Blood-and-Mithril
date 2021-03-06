package bloodandmithril.ui.components.window;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

@Copyright("Matthew Peck 2014")
public class RequiredMaterialsWindow extends ScrollableListingWindow<Item, String> {

	private Container materialsContainer;
	private Map<Item, Integer> requiredMaterials;

	private static Comparator<Item> sortOrder = (o1, o2) -> {
		return o1.getSingular(false).compareTo(o2.getSingular(false));
	};

	/**
	 * Constructor
	 */
	public RequiredMaterialsWindow(
		int length,
		int height,
		String title,
		boolean active,
		int minLength,
		int minHeight,
		Container materialsContainer,
		Map<Item, Integer> requiredMaterials
	) {
		super(length, height, title, active, minLength, minHeight, true, true, getConstructionMaterialStatus(materialsContainer, requiredMaterials),
			item -> {
				return item.getSingular(true);
			},
			sortOrder
		);
		this.materialsContainer = materialsContainer;
		this.requiredMaterials = requiredMaterials;
	}


	@Override
	public void refresh() {
		buildListing(getConstructionMaterialStatus(materialsContainer, requiredMaterials), sortOrder);
	}


	/**
	 * @return the amount of materials currently assigned to this construction, as well as the total required.
	 */
	private static Map<Item, String> getConstructionMaterialStatus(Container materialsContainer, Map<Item, Integer> requiredMaterials) {
		Map<Item, String> map = newHashMap();

		for (final Entry<Item, Integer> entry : requiredMaterials.entrySet()) {
			Integer numberOfItemsInMaterialContainer = Iterables.find(
				materialsContainer.getInventory().entrySet(),
				new Predicate<Entry<Item, Integer>>() {
					@Override
					public boolean apply(Entry<Item, Integer> invEntry) {
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
					public Integer setValue(Integer arg0) {
						throw new UnsupportedOperationException();
					}
				}
			).getValue();
			map.put(entry.getKey(), (numberOfItemsInMaterialContainer == null ? "0" : numberOfItemsInMaterialContainer.toString()) + "/" + entry.getValue().toString());
		}

		return map;
	}


	@Override
	public Object getUniqueIdentifier() {
		return hashCode();
	}
}