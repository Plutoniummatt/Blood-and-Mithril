package spritestar.item.consumable;

import spritestar.Fortress;
import spritestar.character.Individual;
import spritestar.item.Item;
import spritestar.ui.components.window.MessageWindow;
import spritestar.ui.components.window.Window;

import com.badlogic.gdx.graphics.Color;

/**
 * A Carrot
 *
 * @author Matt
 */
public class Carrot extends Consumable {
	private static final long serialVersionUID = 3714624810622084079L;


	/**
	 * Constructor
	 */
	public Carrot() {
		super(0.1f, false, 1);
	}


	@Override
	public String getSingular(boolean firstCap) {
		return firstCap ? "Carrot" : "carrot";
	}


	@Override
	public String getPlural(boolean firstCap) {
		return firstCap ? "Carrots" : "carrots";
	}


	@Override
	public boolean consume(Individual consumer) {
		consumer.id.nickName = "Carrot Eater";
		return true;
	}


	@Override
	public Window getInfoWindow() {
		return new MessageWindow(
			"The carrot is a root vegetable, usually orange in color. It has a crisp texture when fresh.",
			Color.ORANGE,
			Fortress.getMouseScreenX(),
			Fortress.getMouseScreenY(),
			350,
			200,
			"Carrot",
			true,
			100,
			100
		);
	}


	@Override
	public boolean sameAs(Item other) {
		if (other instanceof Carrot) {
			return true;
		}
		return false;
	}
}
