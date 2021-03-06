package bloodandmithril.prop.construction.craftingstation;

import static bloodandmithril.item.items.material.IngotItem.ingot;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;
import java.util.TreeMap;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.core.UpdatedBy;
import bloodandmithril.graphics.RenderPropWith;
import bloodandmithril.graphics.Textures;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.item.items.container.GlassBottleItem;
import bloodandmithril.item.items.material.BrickItem;
import bloodandmithril.item.items.material.GlassItem;
import bloodandmithril.item.items.material.RockItem;
import bloodandmithril.item.material.metal.Iron;
import bloodandmithril.item.material.metal.Steel;
import bloodandmithril.item.material.mineral.SandStone;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.renderservice.ConstructionRenderingService;
import bloodandmithril.prop.updateservice.FurnaceUpdateService;

/**
 * A Furnace
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@Name(name = "Furnace")
@UpdatedBy(FurnaceUpdateService.class)
@RenderPropWith(ConstructionRenderingService.class)
public class Furnace extends CraftingStation implements Container {
	private static final long serialVersionUID = 7693386784097531328L;

	/** {@link TextureRegion} of the {@link Furnace} */
	public static TextureRegion FURNACE, FURNACE_BURNING;
	public static TextureRegion FURNACE1, FURNACE2, FURNACE3, FURNACE4, FURNACE5;

	private static final Map<Item, Integer> craftables = Maps.newHashMap();
	private static final TreeMap<Float, TextureRegion> inProgressTextures = Maps.newTreeMap();

	static {
		craftables.put(new GlassItem(), 1);
		craftables.put(new GlassBottleItem(newHashMap()), 3);
		craftables.put(ingot(Iron.class), 1);
		craftables.put(ingot(Steel.class), 1);

		if (ClientServerInterface.isClient()) {
			Furnace.FURNACE1 = new TextureRegion(Textures.GAME_WORLD_TEXTURE, 1, 1, 95, 56);
			Furnace.FURNACE2 = new TextureRegion(Textures.GAME_WORLD_TEXTURE, 1, 58, 95, 56);
			Furnace.FURNACE3 = new TextureRegion(Textures.GAME_WORLD_TEXTURE, 1, 115, 95, 56);
			Furnace.FURNACE4 = new TextureRegion(Textures.GAME_WORLD_TEXTURE, 1, 172, 95, 56);
			Furnace.FURNACE5 = new TextureRegion(Textures.GAME_WORLD_TEXTURE, 1, 229, 95, 56);

			inProgressTextures.put(0f/5f, FURNACE1);
			inProgressTextures.put(1f/5f, FURNACE2);
			inProgressTextures.put(2f/5f, FURNACE3);
			inProgressTextures.put(3f/5f, FURNACE4);
			inProgressTextures.put(4f/5f, FURNACE5);
		}
	}

	/**
	 * Constructor
	 */
	public Furnace(final float x, final float y) {
		super(x, y, 95, 56, 0.1f);
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		final Map<Item, Integer> requiredItems = newHashMap();
		requiredItems.put(RockItem.rock(SandStone.class), 5);
		requiredItems.put(BrickItem.brick(SandStone.class), 5);
		return requiredItems;
	}


	@Override
	public String getCustomMessage() {
		return "Furnace is not ignited";
	}


	@Override
	public TextureRegion getTextureRegion() {
		if (getConstructionProgress() == 0f) {
			return FURNACE;
		} else if (getConstructionProgress() >= 1f) {
			if (isOccupied()) {
				return FURNACE_BURNING;
			} else {
				return FURNACE;
			}
		} else {
			return inProgressTextures.floorEntry(getConstructionProgress()).getValue();
		}
	}


	@Override
	public String getDescription() {
		return "A furnace, able to achieve temperatures hot enough to melt most metals";
	}


	@Override
	public String getAction() {
		return "Craft";
	}


	@Override
	public Map<Item, Integer> getCraftables() {
		return craftables;
	}


	@Override
	protected int getCraftingSound() {
		return -1;
	}


	@Override
	public boolean requiresConstruction() {
		return true;
	}


	@Override
	public boolean canBeUsedAsFireSource() {
		return false;
	}


	@Override
	public boolean canDeconstruct() {
		return !isOccupied();
	}


	@Override
	public void affectIndividual(final Individual individual, final float delta) {
	}
}