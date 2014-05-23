package bloodandmithril.item.items.material;

import java.util.Map;

import bloodandmithril.character.individuals.Individual;
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
public class Plank extends bloodandmithril.item.items.material.Material implements Craftable {
	private static final long serialVersionUID = 8519886397429197864L;

	private Class<? extends Wood> wood;

	/**
	 * Constructor
	 */
	private Plank(Class<? extends Wood> wood) {
		super(5f, false);
		this.wood = wood;
		setValue(Material.getMaterial(wood).getPlankValue());
	}


	/**
	 * Static instance getter
	 */
	public static Plank plank(Class<? extends Wood> wood) {
		return new Plank(wood);
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
		if (other instanceof Plank) {
			return wood.equals(((Plank) other).wood);
		}

		return false;
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return Material.getMaterial(wood).getPlankTextureRegion();
	}


	@Override
	protected Item internalCopy() {
		return plank(wood);
	}


	@Override
	public boolean canBeCraftedBy(Individual individual) {
		return individual.getSkills().getCarpentry() >= Material.getMaterial(wood).getPlankCraftingLevel();
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
		// TODO Auto-generated method stub
		return null;
	}
}