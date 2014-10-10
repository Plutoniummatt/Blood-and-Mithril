package bloodandmithril.networking.requests;

import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Response;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.math.Vector2;

/**
 * A {@link Response} to notifiy clients that a prop has been removed
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class DestroyPropNotification implements Response {

	private final int propId;
	private Vector2 position;

	/** Constructor */
	public DestroyPropNotification(int propId, Vector2 position) {
		this.propId = propId;
		this.position = position;
	}


	@Override
	public void acknowledge() {
		Domain.getProps().remove(propId);
		Domain.getActiveWorld().getPositionalIndexMap().getNearbyEntities(Prop.class, position.x, position.y).remove(propId);
	}


	@Override
	public int forClient() {
		return -1;
	}


	@Override
	public void prepare() {
	}
}