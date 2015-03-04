package bloodandmithril.prop.construction.craftingstation;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.item.items.material.Ingot.ingot;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.graphics.particles.ParticleService;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.item.items.container.GlassBottle;
import bloodandmithril.item.items.material.Bricks;
import bloodandmithril.item.items.material.Glass;
import bloodandmithril.item.items.material.Rock;
import bloodandmithril.item.material.Material;
import bloodandmithril.item.material.metal.Iron;
import bloodandmithril.item.material.metal.Steel;
import bloodandmithril.item.material.mineral.Mineral;
import bloodandmithril.item.material.mineral.SandStone;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain.Depth;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

/**
 * A Furnace
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Furnace extends CraftingStation implements Container {
	private static final long serialVersionUID = 7693386784097531328L;

	/** {@link TextureRegion} of the {@link Furnace} */
	public static TextureRegion FURNACE, FURNACE_BURNING;

	/** The {@link Mineral} this {@link Furnace} is made from */
	private Class<? extends Mineral> material;

	private static final Map<Item, Integer> craftables = Maps.newHashMap();

	static {
		craftables.put(new Glass(), 1);
		craftables.put(new GlassBottle(newHashMap()), 3);
		craftables.put(ingot(Iron.class), 1);
		craftables.put(ingot(Steel.class), 1);
	}

	/**
	 * Constructor
	 */
	public Furnace(Class<? extends Mineral> material, float x, float y) {
		super(x, y, 95, 56, 0.1f);
		this.material = material;
	}


	@Override
	protected void internalRender(float constructionProgress) {
		if (isOccupied()) {
			spriteBatch.draw(FURNACE_BURNING, position.x - width / 2, position.y);
		} else {
			spriteBatch.draw(FURNACE, position.x - width / 2, position.y);
		}
	}


	@Override
	public synchronized void update(float delta) {
		super.update(delta);

		if (isOccupied()) {
			if (BloodAndMithrilClient.isOnScreen(position, 50f)) {
				ParticleService.randomVelocityDiminishing(position.cpy().add(0, height - 38), 6f, 30f, Color.ORANGE, Color.ORANGE, 2f, 8f, MovementMode.EMBER, Util.getRandom().nextInt(600), Depth.MIDDLEGROUND, false, Color.RED);
				ParticleService.randomVelocityDiminishing(position.cpy().add(0, height - 38), 6f, 30f, Color.ORANGE, Color.ORANGE, 1f, 6f, MovementMode.EMBER, Util.getRandom().nextInt(1000), Depth.MIDDLEGROUND, false, Color.RED);
				ParticleService.randomVelocityDiminishing(position.cpy().add(0, height - 38), 30f, 10f, Colors.LIGHT_SMOKE, Colors.LIGHT_SMOKE, 10f, 0f, MovementMode.EMBER, Util.getRandom().nextInt(3000), Depth.BACKGROUND, false, null);
			}
		}
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		Map<Item, Integer> requiredItems = newHashMap();
		requiredItems.put(Rock.rock(SandStone.class), 5);
		requiredItems.put(Bricks.bricks(SandStone.class), 5);
		return requiredItems;
	}


	@Override
	public String getCustomMessage() {
		return "Furnace is not ignited";
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return null;
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
	public void preRender() {
		if (getConstructionProgress() == 0f) {
			Shaders.filter.setUniformf("color", Colors.modulateAlpha(Material.getMaterial(material).getColor(), 0.90f));
		} else {
			Shaders.filter.setUniformf("color", Material.getMaterial(material).getColor());
		}
	}


	@Override
	protected int getCraftingSound() {
		return -1;
	}


	@Override
	protected String internalGetTitle() {
		return "Furnace";
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
}