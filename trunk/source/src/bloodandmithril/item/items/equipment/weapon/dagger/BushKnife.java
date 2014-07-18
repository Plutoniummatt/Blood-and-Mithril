package bloodandmithril.item.items.equipment.weapon.dagger;

import static bloodandmithril.item.items.material.Ingot.ingot;

import java.util.HashMap;
import java.util.Map;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.Dagger;
import bloodandmithril.item.material.metal.Iron;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

@Copyright("Matthew Peck 2014")
public class BushKnife extends Dagger<Iron> {
	private static final long serialVersionUID = 7641873863821117364L;

	public BushKnife() {
		super(0.5f, 50, Iron.class);
	}


	@Override
	protected String weaponGetSingular(boolean firstCap) {
		return "Bush knife";
	}


	@Override
	protected String weaponGetPlural(boolean firstCap) {
		return "Bush knives";
	}


	@Override
	public String getDescription() {
		return "An all-purpose survival knife, made from Iron.";
	}


	@Override
	protected boolean internalSameAs(Item other) {
		return other instanceof BushKnife;
	}


	@Override
	protected Item internalCopy() {
		return new BushKnife();
	}


	@Override
	public boolean canBeCraftedBy(Individual individual) {
		return individual.getSkills().getSmithing() >= 3;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		HashMap<Item, Integer> newHashMap = Maps.newHashMap();
		newHashMap.put(ingot(Iron.class), 3);
		return newHashMap;
	}


	@Override
	public float getCraftingDuration() {
		return 10;
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		return null;
	}
}