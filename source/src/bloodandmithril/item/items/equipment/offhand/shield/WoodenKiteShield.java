package bloodandmithril.item.items.equipment.offhand.shield;

import java.util.HashMap;
import java.util.Map;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.proficiency.proficiencies.Carpentry;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.offhand.Shield;
import bloodandmithril.item.items.material.IngotItem;
import bloodandmithril.item.items.material.PlankItem;
import bloodandmithril.item.material.metal.Iron;
import bloodandmithril.item.material.wood.StandardWood;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Maps;

public class WoodenKiteShield extends Shield {
	private static final long serialVersionUID = 5112607606681273075L;

	public static TextureRegion woodenKiteShield;

	/**
	 * Constructor
	 */
	public WoodenKiteShield() {
		super(5f, 20, ItemValues.WOODENBUCKLER);
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return (firstCap ? "Wooden kite shield" : "wooden kite shield");
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return (firstCap ? "Wooden kite shields" : "wooden kite shields");
	}


	@Override
	public String getDescription() {
		return "A wooden kite shield.";
	}


	@Override
	protected boolean internalSameAs(Item other) {
		if (other instanceof WoodenKiteShield) {
			return true;
		}

		return false;
	}


	@Override
	public TextureRegion getTextureRegion() {
		return woodenKiteShield;
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		return null;
	}


	@Override
	protected Item internalCopy() {
		WoodenKiteShield shield = new WoodenKiteShield();
		return shield;
	}


	@Override
	public float renderAngle() {
		return 60f;
	}


	@Override
	public float combatAngle() {
		return 80f;
	}


	@Override
	public Vector2 getGripLocation() {
		return new Vector2(43, 11);
	}


	@Override
	public boolean canBeCraftedBy(Individual individual) {
		return individual.getProficiencies().getProficiency(Carpentry.class).getLevel() >= 10;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		HashMap<Item, Integer> newHashMap = Maps.newHashMap();
		newHashMap.put(PlankItem.plank(StandardWood.class), 15);
		newHashMap.put(IngotItem.ingot(Iron.class), 1);
		return newHashMap;
	}


	@Override
	public void crafterEffects(Individual crafter, float delta) {
		crafter.getProficiencies().getProficiency(Carpentry.class).increaseExperience(delta * 10f);
	}


	@Override
	public float getCraftingDuration() {
		return 100;
	}
}
