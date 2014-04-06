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
}