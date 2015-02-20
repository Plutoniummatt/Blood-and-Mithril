package bloodandmithril.item.items.equipment.weapon.onehandedsword;

import static bloodandmithril.item.items.material.Ingot.ingot;

import java.util.HashMap;
import java.util.Map;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.OneHandedSword;
import bloodandmithril.item.material.metal.Iron;
import bloodandmithril.item.material.metal.Steel;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

@Copyright("Matthew Peck 2014")
public class Machette extends OneHandedSword<Iron> {
	private static final long serialVersionUID = 7225208685150727973L;


	public Machette() {
		super(2.5f, 10, 100, Iron.class);
	}


	@Override
	protected String weaponGetSingular(boolean firstCap) {
		return "Machette";
	}


	@Override
	protected String weaponGetPlural(boolean firstCap) {
		return "Machettes";
	}


	@Override
	public String getDescription() {
		return "A machette, good for chopping down plants, or other things, that get in your way.";
	}


	@Override
	protected boolean internalSameAs(Item other) {
		return other instanceof Machette;
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		return null;
	}


	@Override
	protected Item internalCopy() {
		return new Machette();
	}


	@Override
	public boolean canBeCraftedBy(Individual individual) {
		return individual.getSkills().getSmithing().getLevel() >= 8;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		HashMap<Item, Integer> newHashMap = Maps.newHashMap();
		newHashMap.put(ingot(Steel.class), 7);
		return newHashMap;
	}


	@Override
	public float getCraftingDuration() {
		return 15;
	}
}