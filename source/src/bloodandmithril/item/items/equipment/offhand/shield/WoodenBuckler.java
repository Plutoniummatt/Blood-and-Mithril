package bloodandmithril.item.items.equipment.offhand.shield;

import java.util.HashMap;
import java.util.Map;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.proficiency.proficiencies.Carpentry;
import bloodandmithril.core.Copyright;
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

/**
 * A simple wooden buckler
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class WoodenBuckler extends Shield {
	private static final long serialVersionUID = 5112607606681273075L;

	public static TextureRegion woodenBuckler;

	/**
	 * Constructor
	 */
	public WoodenBuckler() {
		super(5f, 20, ItemValues.WOODENBUCKLER);
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return firstCap ? "Wooden buckler" : "wooden buckler";
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return firstCap ? "Wooden bucklers" : "wooden bucklers";
	}


	@Override
	public String getDescription() {
		return "A wooden buckler";
	}


	@Override
	protected boolean internalSameAs(Item other) {
		if (other instanceof WoodenBuckler) {
			return true;
		}

		return false;
	}


	@Override
	public TextureRegion getTextureRegion() {
		return woodenBuckler;
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		return null;
	}


	@Override
	protected Item internalCopy() {
		WoodenBuckler shield = new WoodenBuckler();
		return shield;
	}


	@Override
	public float renderAngle() {
		return -40f;
	}


	@Override
	public float combatAngle() {
		return 50f;
	}


	@Override
	public Vector2 getGripLocation() {
		return new Vector2(26, 21);
	}


	@Override
	public boolean canBeCraftedBy(Individual individual) {
		return true;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		HashMap<Item, Integer> newHashMap = Maps.newHashMap();
		newHashMap.put(PlankItem.plank(StandardWood.class), 5);
		newHashMap.put(IngotItem.ingot(Iron.class), 1);
		return newHashMap;
	}


	@Override
	public void crafterEffects(Individual crafter, float delta) {
		crafter.getProficiencies().getProficiency(Carpentry.class).increaseExperience(delta * 10f);
	}


	@Override
	public float getCraftingDuration() {
		return 50;
	}


	@Override
	public float getBlockChance() {
		return 0.25f;
	}
}