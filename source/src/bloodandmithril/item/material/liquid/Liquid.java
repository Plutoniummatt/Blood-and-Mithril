package bloodandmithril.item.material.liquid;

import java.io.Serializable;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.character.Individual;

/**
 * A class representing a liquid
 *
 * @author Matt
 */
public abstract class Liquid implements Serializable {
	private static final long serialVersionUID = -317605951640204479L;
	
	protected Liquid() {}

	public abstract void drink(float amount, Individual affected);

	public abstract String getDescription();

	public abstract Color getColor();

	/** Empty */
	public static class Empty extends Liquid {
		private static final long serialVersionUID = 6431065925729672809L;
		
		public Empty() {
			super();
		}
		
		@Override
		public void drink(float amount, Individual affected) {
		}
		@Override
		public String getDescription() {
			return "Empty";
		}

		@Override
		public Color getColor() {
			return null;
		}
	}
}