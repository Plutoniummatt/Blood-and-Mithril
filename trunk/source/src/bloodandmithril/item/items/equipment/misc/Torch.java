package bloodandmithril.item.items.equipment.misc;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.graphics.particles.ParticleService;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain.Depth;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**
 * Offhand torch for lighting
 * 
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class Torch extends OffhandEquipment {

	public static TextureRegion torch;
	private static final long serialVersionUID = 5112607606681273075L;

	/**
	 * Constructor
	 */
	public Torch() {
		super(1f, 2, ItemValues.TORCH);
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return firstCap ? "Torch" : "torch";
	}

	
	@Override
	protected String internalGetPlural(boolean firstCap) {
		return firstCap ? "Torches" : "torches";
	}

	
	@Override
	public String getDescription() {
		return "A torch, used for lighting";
	}
	

	@Override
	protected boolean internalSameAs(Item other) {
		return other instanceof Torch;
	}

	
	@Override
	public TextureRegion getTextureRegion() {
		return torch;
	}

	
	@Override
	public TextureRegion getIconTextureRegion() {
		return null;
	}
	
	
	@Override
	protected Item internalCopy() {
		return new Torch();
	}

	
	@Override
	public Category getType() {
		return Category.MISC;
	}


	@Override
	public Vector2 getGripLocation() {
		return new Vector2(12, 2);
	}
	
	
	@Override
	public void particleEffects(Vector2 position, float angle, boolean flipX) {
		Vector2 emission = position.cpy().add(new Vector2(flipX ? - 27 : 27, 0).rotate(angle));
		
		ParticleService.randomVelocityDiminishing(emission, 3f, 15f, Color.WHITE, Color.ORANGE, Util.getRandom().nextFloat() * 3f, 12f, MovementMode.EMBER, Util.getRandom().nextInt(800), Depth.FOREGOUND, false, Color.RED);
		ParticleService.randomVelocityDiminishing(emission, 3f, 15f, Color.WHITE, Color.ORANGE, Util.getRandom().nextFloat() * 3f, 12f, MovementMode.EMBER, Util.getRandom().nextInt(800), Depth.FOREGOUND, false, Color.RED);
		ParticleService.randomVelocityDiminishing(emission, 3f, 10f, Colors.LIGHT_SMOKE, Colors.LIGHT_SMOKE, 8f, 0f, MovementMode.EMBER, Util.getRandom().nextInt(3000), Depth.FOREGOUND, false, null);
	}
}
