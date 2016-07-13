package bloodandmithril.ui;

import java.io.Serializable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

import bloodandmithril.core.Copyright;
import bloodandmithril.util.SerializableColor;

/**
 * Floating text that is rendered at UI layer
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class FloatingText implements Serializable {
	private static final long serialVersionUID = -2300891549168870979L;

	public final String text;
	public final SerializableColor color;
	public final Vector2 worldPosition;
	public float maxLife = 1f, life = 1f;
	public boolean ui;

	private FloatingText(final String text, final SerializableColor color, final Vector2 worldPosition, final boolean ui) {
		this.text = text;
		this.color = color;
		this.worldPosition = worldPosition;
		this.ui = ui;
	}

	public static FloatingText floatingText(final String text, final Color color, final Vector2 worldPosition, final boolean ui) {
		return new FloatingText(text, new SerializableColor(color), worldPosition, ui);
	}


	public static FloatingText floatingText(final String text, final Color color, final Vector2 worldPosition, final float life, final boolean ui) {
		final FloatingText floatingText = new FloatingText(text, new SerializableColor(color), worldPosition, ui);
		floatingText.maxLife = life;
		floatingText.life = life;
		return floatingText;
	}
}