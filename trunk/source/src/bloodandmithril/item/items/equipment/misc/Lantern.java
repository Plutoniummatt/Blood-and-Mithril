package bloodandmithril.item.items.equipment.misc;

import static bloodandmithril.networking.ClientServerInterface.isServer;

import java.util.Set;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.particles.DiminishingTracerParticle;
import bloodandmithril.graphics.particles.Particle;
import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.Equipper;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;
import bloodandmithril.world.Domain.Depth;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Sets;

/**
 * Offhand lantern for lighting
 * 
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class Lantern extends OffhandEquipment {
	private static final long serialVersionUID = -8857992190500608270L;
	
	public static TextureRegion lantern;

	private Set<Long> particleIds = Sets.newHashSet();
	private float fuelRemaining;
	private Integer workingId;
	private float refreshTimer = 0.5f;
	
	/**
	 * Constructor
	 */
	public Lantern(float fuelRemaining) {
		super(1f, 5, ItemValues.LANTERN);
		this.fuelRemaining = fuelRemaining;
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return (firstCap ? "Lantern" : "lantern") + " (" + String.format("%.2f", fuelRemaining) + ")";
	}

	
	@Override
	protected String internalGetPlural(boolean firstCap) {
		return (firstCap ? "Lanterns" : "lanterns") + String.format("%.2f", fuelRemaining) + ")";
	}

	
	@Override
	public String getDescription() {
		return "A lantern, used for lighting. Uses oil as fuel.";
	}
	

	@Override
	protected boolean internalSameAs(Item other) {
		if (other instanceof Lantern) {
			return (((Lantern) other).fuelRemaining == fuelRemaining && ((Lantern) other).workingId == workingId) || (fuelRemaining == 0f && ((Lantern) other).fuelRemaining == 0f);
		}
		return other instanceof Lantern;
	}

	
	@Override
	public TextureRegion getTextureRegion() {
		return lantern;
	}

	
	@Override
	public TextureRegion getIconTextureRegion() {
		return null;
	}
	
	
	@Override
	protected Item internalCopy() {
		return new Lantern(fuelRemaining);
	}
	
	
	@Override
	public float renderAngle() {
		return 0f;
	}
	
	
	@Override
	public float combatAngle() {
		return 0f;
	}

	
	@Override
	public Category getType() {
		return Category.MISC;
	}


	@Override
	public Vector2 getGripLocation() {
		return new Vector2(6, 25);
	}
	
	
	@Override
	public void particleEffects(Vector2 position, float angle, boolean flipX) {
		if (fuelRemaining > 0f) {
			if (particleIds.isEmpty()) {
				if (isServer()) {
					for (int i = 0; i < 20; i++) {
						Particle particle = new DiminishingTracerParticle(
							position.cpy().add(0f, -19f),
							new Vector2(0, 0),
							Color.WHITE,
							Colors.FIRE_START,
							1f,
							getWorldId(),
							8f,
							MovementMode.WEIGHTLESS,
							Depth.FOREGOUND,
							Long.MAX_VALUE
						);
						particle.doNotUpdate();
						Domain.getWorld(getWorldId()).getServerParticles().put(particle.particleId, particle);
						particleIds.add(particle.particleId);
					}
					for (int i = 0; i < 10; i++) {
						Particle particle = new DiminishingTracerParticle(
							position.cpy().add(0f, -19f),
							new Vector2(0, 0),
							Color.WHITE,
							Colors.FIRE_START,
							1f,
							getWorldId(),
							3f,
							MovementMode.WEIGHTLESS,
							Depth.MIDDLEGROUND,
							Long.MAX_VALUE
						);
						particle.doNotUpdate();
						Domain.getWorld(getWorldId()).getServerParticles().put(particle.particleId, particle);
						particleIds.add(particle.particleId);
					}
				}
			} else {
				for (long id : particleIds) {
					DiminishingTracerParticle particle = (DiminishingTracerParticle) Domain.getWorld(getWorldId()).getServerParticles().get(id);
					if (particle != null) {
						particle.position = position.cpy().add(0f, -19f);
						particle.prevPosition = position.cpy().add(0f, -19f);
					}
				}
			}
		}
	}
	
	
	@Override
	public void update(Equipper equipper, float delta) {
		if (refreshTimer <= 0f) {
			UserInterface.refreshRefreshableWindows();
			refreshTimer = 0.5f;
		} else {
			refreshTimer -= delta;
		}
		
		if (fuelRemaining > 0f) {
			fuelRemaining -= delta / 4000f;
		} else {
			for (long id : particleIds) {
				if (ClientServerInterface.isServer()) {
					Domain.getWorld(getWorldId()).getServerParticles().remove(id);
				}
			}			
			particleIds.clear();
			fuelRemaining = 0f;
		}
	}
	
	
	public void addFuel(float amount) {
		this.fuelRemaining += amount;
	}
	
	
	@Override
	public void onUnequip(Equipper equipper) {
		for (long id : particleIds) {
			if (ClientServerInterface.isServer()) {
				Domain.getWorld(getWorldId()).getServerParticles().remove(id);
			}
		}
		particleIds.clear();
	}


	@Override
	public void onEquip(Equipper equipper) {
		if (workingId == null) {
			this.workingId = ParameterPersistenceService.getParameters().getNextItemId();
		}
	}
}