package bloodandmithril.networking.requests;

import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;

import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Response;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.TextBubble;
import bloodandmithril.util.SerializableFunction;

/**
 * {@link Response} to notify clients to add {@link TextBubble}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class NotifyTextBubble implements Response {
	private static final long serialVersionUID = 3718843397631863398L;

	@Inject private transient UserInterface userInterface;

	private SerializableFunction<Vector2> position;
	private int xOffset;
	private int yOffset;
	private String text;
	private long duration;


	/**
	 * Constructor
	 */
	public NotifyTextBubble(final SerializableFunction<Vector2> position, final String text, final long duration, final int xOffset, final int yOffset) {
		this.position = position;
		this.text = text;
		this.duration = duration;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
	}


	@Override
	public void acknowledge() {
		userInterface.addTextBubble(
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