package bloodandmithril.item.items.equipment.weapon.onehandedsword;

import static bloodandmithril.item.items.material.Ingot.ingot;

import java.util.HashMap;
import java.util.Map;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.OneHandedSword;
import bloodandmithril.item.material.metal.Steel;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

@Copyright("Matthew Peck 2014")
public class Broadsword extends OneHandedSword<Steel> {
	private static final long serialVersionUID = 9190548689838579213L;

	public Broadsword() {
		super(3f, 10, 100, Steel.class);
	}


	@Override
	protected String weaponGetSingular(boolean firstCap) {
		return "Broadsword";
	}


	@Override
	protected String weaponGetPlural(boolean firstCap) {
		return "Broadswords";
	}


	@Override
	public String getDescription() {
		return "A medium military sword, forged from Steel";
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		return null;
	}


	@Override
	protected Item internalCopy() {
		return new Broadsword();
	}


	@Override
	protected boolean internalSameAs(Item other) {
		return other instanceof Broadsword;
	}


	@Override
	public boolean canBeCraftedBy(Individual individual) {
		return individual.getSkills().getSmithing().getLevel() >= 10;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		HashMap<Item, Integer> newHashMap = Maps.newHashMap();
		newHashMap.put(ingot(Steel.class), 7);
		return newHashMap;
	}


	@Override
	public float getCraftingDuration() {
		return 30;
	}
}