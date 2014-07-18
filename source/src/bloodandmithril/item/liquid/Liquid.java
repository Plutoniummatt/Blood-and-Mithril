package bloodandmithril.item.liquid;

import java.io.Serializable;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;

import com.badlogic.gdx.graphics.Color;

/**
 * A class representing a liquid
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class Liquid implements Serializable {
	private static final long serialVersionUID = -317605951640204479L;

	protected Liquid() {}

	public abstract void drink(float amount, Individual affected);

	public abstract String getDescription();

	public abstract Color getColor();
}