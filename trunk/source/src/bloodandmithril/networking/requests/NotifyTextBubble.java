package bloodandmithril.networking.requests;

import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Response;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.TextBubble;
import bloodandmithril.util.SerializableFunction;

import com.badlogic.gdx.math.Vector2;

/**
 * {@link Response} to notify clients to add {@link TextBubble}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class NotifyTextBubble implements Response {
	private SerializableFunction<Vector2> position;
	private int xOffset;
	private int yOffset;
	private String text;
	private long duration;


	/**
	 * Constructor
	 */
	public NotifyTextBubble(SerializableFunction<Vector2> position, String text, long duration, int xOffset, int yOffset) {
		this.position = position;
		this.text = text;
		this.duration = duration;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
	}


	@Override
	public void acknowledge() {
		UserInterface.addTextBubble(
			text,
			position,
			duration,
			xOffset,
			yOffset
		);
	}


	@Override
	public int forClient() {
		return -1;
	}


	@Override
	public void prepare() {
	}
}