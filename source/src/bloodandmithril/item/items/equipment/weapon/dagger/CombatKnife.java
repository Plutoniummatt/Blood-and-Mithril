package bloodandmithril.item.items.equipment.weapon.dagger;

import static bloodandmithril.item.items.material.Ingot.ingot;

import java.util.HashMap;
import java.util.Map;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.Dagger;
import bloodandmithril.item.material.metal.Steel;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

@Copyright("Matthew Peck 2014")
public class CombatKnife extends Dagger<Steel> {
	private static final long serialVersionUID = 7540318665466689612L;

	public CombatKnife() {
		super(0.6f, 3, 100, Steel.class);
	}


	@Override
	protected String weaponGetSingular(boolean firstCap) {
		return "Combat knife";
	}


	@Override
	protected String weaponGetPlural(boolean firstCap) {
		return "Combat knives";
	}


	@Override
	public String getDescription() {
		return "A lightweight combat knife forged from Steel.";
	}


	@Override
	protected boolean internalSameAs(Item other) {
		return other instanceof CombatKnife;
	}


	@Override
	protected Item internalCopy() {
		return new CombatKnife();
	}


	@Override
	public boolean canBeCraftedBy(Individual individual) {
		return individual.getSkills().getSmithing().getLevel() >= 5;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		HashMap<Item, Integer> newHashMap = Maps.newHashMap();
		newHashMap.put(ingot(Steel.class), 3);
		return newHashMap;
	}


	@Override
	public float getCraftingDuration() {
		return 20;
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		return null;
	}
}