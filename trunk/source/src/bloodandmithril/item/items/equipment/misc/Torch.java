package bloodandmithril.item.items.equipment.misc;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.graphics.particles.ParticleService;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.Equipper;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain.Depth;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**
 * Offhand torch for lighting
 * 
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class Torch extends OffhandEquipment {
	private static final long serialVersionUID = 5112607606681273075L;

	public static TextureRegion torch;
	private float durationRemaining;
	private Integer workingId;

	/**
	 * Constructor
	 */
	public Torch(float durationRemaining) {
		super(1f, 2, ItemValues.TORCH);
		this.durationRemaining = durationRemaining;
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
		return "A torch, used for lighting.";
	}
	

	@Override
	protected boolean internalSameAs(Item other) {
		if (other instanceof Torch) {
			return ((Torch) other).workingId == workingId && ((Torch) other).durationRemaining == durationRemaining;
		}
		
		return false;
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
		return new Torch(durationRemaining);
	}
	
	
	@Override
	public float renderAngle() {
		return 55f;
	}
	
	
	@Override
	public float combatAngle() {
		return 90f;
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
		
		float size1 = Util.getRandom().nextFloat();
		float size2 = Util.getRandom().nextFloat();
		ParticleService.randomVelocityDiminishing(emission, 3f, 15f, Colors.FIRE_START, Colors.FIRE_START, size1 * 3f, size1 * 8f + 10f, MovementMode.EMBER, Util.getRandom().nextInt(800), Depth.FOREGOUND, false, Colors.FIRE_END);
		ParticleService.randomVelocityDiminishing(emission, 3f, 15f, Colors.FIRE_START, Colors.FIRE_START, size2 * 3f, size2 * 2f + 6f, MovementMode.EMBER, Util.getRandom().nextInt(800), Depth.MIDDLEGROUND, false, Colors.FIRE_END);
		ParticleService.randomVelocityDiminishing(emission, 3f, 10f, Colors.LIGHT_SMOKE, Colors.LIGHT_SMOKE, 8f, 0f, MovementMode.EMBER, Util.getRandom().nextInt(3000), Depth.FOREGOUND, false, null);
	}
	
	
	@Override
	public void update(Equipper equipper, float delta) {
		if (durationRemaining <= 0f) {
			equipper.unequip(this);
			equipper.takeItem(this);
			UserInterface.refreshRefreshableWindows();
		} else {
			durationRemaining -= delta;
		}
	}
	
	
	@Override
	public void onUnequip() {
	}


	@Override
	public void onEquip() {
		if (workingId == null) {
			this.workingId = ParameterPersistenceService.getParameters().getNextItemId();
		}
	}
}