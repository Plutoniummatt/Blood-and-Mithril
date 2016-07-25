package bloodandmithril.prop;

import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.prop.renderservice.PropRenderingService;

/**
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class DummyPropRenderingService implements PropRenderingService {

	@Override
	public void render(Prop p) {
		throw new RuntimeException();
	}
}