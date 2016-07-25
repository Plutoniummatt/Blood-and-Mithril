package bloodandmithril.graphics.background;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.Lists;

import bloodandmithril.core.Copyright;

/**
 * Class to manage background images
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class BackgroundImages implements Serializable {
	private static final long serialVersionUID = -649236314540206654L;

	List<Layer> layers = Lists.newArrayList();
	
	public BackgroundImages() {
		layers.add(new DayLightColorLayerWithFluidReflections());
	}
}