package bloodandmithril.item.items.material;

import java.util.Map;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.proficiency.proficiencies.Carpentry;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.material.Material;
import bloodandmithril.item.material.wood.Wood;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A plank made from a {@link Wood}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class PlankItem extends bloodandmithril.item.items.material.MaterialItem implements Craftable {
	private static final long serialVersionUID = 8519886397429197864L;

	public static TextureRegion PLANK;
	public static TextureRegion PLANKICON;
	private Class<? extends Wood> wood;

	/**
	 * Constructor
	 */
	private PlankItem(Class<? extends Wood> wood) {
		super(1f, 5, false);
		this.wood = wood;
		setValue(Material.getMaterial(wood).getPlankValue());
		bounces();
	}


	/**
	 * Static instance getter
	 */
	public static PlankItem plank(Class<? extends Wood> wood) {
		return new PlankItem(wood);
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return Material.getMaterial(wood).getName() + " Plank";
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return Material.getMaterial(wood).getName() + " Planks";
	}


	@Override
	public String getDescription() {
		return "A wooden plank, made from " + Material.getMaterial(wood).getName() + ".";
	}


	@Override
	protected boolean internalSameAs(Item other) {
		if (other instanceof PlankItem) {
			return wood.equals(((PlankItem) other).wood);
		}

		return false;
	}


	@Override
	public TextureRegion getTextureRegion() {
		return Material.getMaterial(wood).getPlankTextureRegion();
	}


	@Override
	protected Item internalCopy() {
		return plank(wood);
	}


	@Override
	public boolean canBeCraftedBy(Individual individual) {
		return individual.getProficiencies().getProficiency(Carpentry.class).getLevel() >= Material.getMaterial(wood).getPlankCraftingLevel();
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		return Material.getMaterial(wood).getRequiredMaterialsToCraftPlank();
	}


	@Override
	public float getCraftingDuration() {
		return Material.getMaterial(wood).getPlankCraftingDuration();
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		return PLANKICON;
	}


	@Override
	public void crafterEffects(Individual crafter, float delta) {
		crafter.getProficiencies().getProficiency(Carpentry.class).increaseExperience(delta * 5f);
	}
}