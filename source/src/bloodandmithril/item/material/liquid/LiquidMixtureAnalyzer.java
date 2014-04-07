package bloodandmithril.item.material.liquid;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

/**
 * Analysis of a mixture of {@link Liquid}s
 *
 * @author Matt
 */
public class LiquidMixtureAnalyzer {

	
	/**
	 * @return The description of this fluid.
	 */
	public static String getDescription(Map<Class<? extends Liquid>, Float> liquids, float total) {
		List<Entry<Class<? extends Liquid>, Float>> list = Lists.newArrayList(liquids.entrySet());
		Collections.sort(list, (e1, e2) -> {
			return e1.getValue().compareTo(e1.getValue());
		});
		
		if (list.get(0).getValue() > 0.75f * total) {
			try {
				return "mostly " + list.get(0).getKey().getSimpleName() + "; " + list.get(0).getKey().newInstance().getDescription();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			return "a mixture of different liquids... Impossible to tell without proper analysis.";
		}
	}
	
	
	/**
	 * @return The title of this fluid.
	 */
	public static String getTitle(Map<Class<? extends Liquid>, Float> liquids, float total) {
		List<Entry<Class<? extends Liquid>, Float>> list = Lists.newArrayList(liquids.entrySet());
		Collections.sort(list, (e1, e2) -> {
			return e1.getValue().compareTo(e1.getValue());
		});
		
		if (list.get(0).getValue() > 0.75f * total) {
			return list.get(0).getKey().getSimpleName();
		} else {
			return "different liquids";
		}
	}
}