package bloodandmithril.ui.components.window;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import bloodandmithril.item.Container;
import bloodandmithril.item.Item;

public class RequiredMaterialsWindow extends ScrollableListingWindow<Item, String> {

	private Container materialsContainer;
	private Map<Item, Integer> requiredMaterials;
	
	/**
	 * Constructor
	 */
	public RequiredMaterialsWindow(
		int x, 
		int y, 
		int length, 
		int height, 
		String title, 
		boolean active, 
		int minLength, 
		int minHeight, 
		Container materialsContainer, 
		Map<Item, Integer> requiredMaterials
	) {
		super(x, y, length, height, title, active, minLength, minHeight, true, true, getConstructionMaterialStatus(materialsContainer, requiredMaterials));
		this.materialsContainer = materialsContainer;
		this.requiredMaterials = requiredMaterials;
	}


	@Override
	public void refresh() {
		buildListing(getConstructionMaterialStatus(materialsContainer, requiredMaterials));
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
}