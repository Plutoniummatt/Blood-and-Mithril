package bloodandmithril.item.items.equipment.weapon.ranged.projectile;

import static bloodandmithril.core.BloodAndMithrilClient.isOnScreen;

import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import bloodandmithril.character.conditions.Burning;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.graphics.particles.ParticleService;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;
import bloodandmithril.item.items.material.RockItem;
import bloodandmithril.item.material.metal.Metal;
import bloodandmithril.item.material.mineral.Coal;
import bloodandmithril.prop.Prop;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;

/**
 * A flaming arrow, must be near a fire source in order to be lit
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class FireArrowProjectile<T extends Metal> extends ArrowProjectile<T> {
	private static final long serialVersionUID = -4345906143614035570L;
	private float burnDuration;
	private boolean lit;

	/**
	 * Constructor
	 */
	public FireArrowProjectile(Class<T> metal, Vector2 position, Vector2 velocity, float burnDuration, boolean lit) {
		super(metal, position, velocity);
		this.burnDuration = burnDuration;
	}


	@Override
	public void update(float delta) {
		if (lit) {
			if (burnDuration > 0f) {
				burnDuration -= delta;
			} else {
				lit = false;
			}
			if (isOnScreen(position, 50f)) {
				ParticleService.randomVelocityDiminishing(position, 0f, 20f, Color.ORANGE, Color.ORANGE, Util.getRandom().nextFloat() * 4f, 6f, MovementMode.EMBER, Util.getRandom().nextInt(600), Depth.FOREGROUND, false, Color.RED);
				ParticleService.randomVelocityDiminishing(position, 0f, 10f, Colors.LIGHT_SMOKE, Colors.LIGHT_SMOKE, 4f, 0f, MovementMode.EMBER, Util.getRandom().nextInt(3000), Depth.FOREGROUND, false, null);
			}
		}

		super.update(delta);
	}


	@Override
	public void hit(Individual victim) {
		super.hit(victim);

		if (Util.roll(0.35f) && lit) {
			victim.addCondition(new Burning(10f));
		}
	}


	@Override
	public void preFireDecorate(Individual individual) {
		World world = Domain.getWorld(individual.getWorldId());

		for (int propId : world.getPositionalIndexMap().getNearbyEntityIds(Prop.class, individual.getState().position)) {
			Prop prop = world.props().getProp(propId);
			if (prop.canBeUsedAsFireSource() && individual.getInteractionBox().overlapsWith(new Box(prop.position.cpy().add(0, prop.height/2), prop.width, prop.height))) {
				lit = true;
				return;
			}
		}

		for (int individualId : world.getPositionalIndexMap().getNearbyEntityIds(Individual.class, individual.getState().position)) {
			Individual nearbyIndividual = Domain.getIndividual(individualId);
			if (nearbyIndividual.canBeUsedAsFireSource() && nearbyIndividual.getInteractionBox().isWithinBox(individual.getState().position)) {
				lit = true;
				return;
			}
		}
	}


	/**
	 * {@link Item} representation of a {@link FireArrowProjectile}
	 *
	 * @author Matt
	 */
	public static class FireArrowItem<T extends Metal> extends ArrowItem<T> {
		private static final long serialVersionUID = 9027137493687956507L;

		public FireArrowItem(Class<T> metal) {
			super(metal);
			this.setValue(getValue() + ItemValues.COAL);
		}

		@Override
		protected String internalGetSingular(boolean firstCap) {
			return "Ignitable " + super.internalGetSingular(firstCap);
		}


		@Override
		protected String internalGetPlural(boolean firstCap) {
			return "Ignitable " + super.internalGetPlural(firstCap);
		}


		@Override
		public String getDescription() {
			return super.getDescription() + ", this one can be lit.  In order to light it, stand next to a fire source when firing.";
		}


		@Override
		@SuppressWarnings("rawtypes")
		protected boolean internalSameAs(Item other) {
			if (other instanceof FireArrowItem) {
				return metal.equals(((FireArrowItem) other).metal);
			}
			return false;
		}


		@Override
		public Map<Item, Integer> getRequiredMaterials() {
			Map<Item, Integer> requiredMaterials = super.getRequiredMaterials();
			requiredMaterials.put(RockItem.rock(Coal.class), 1);
			return requiredMaterials;
		}


		@Override
		public TextureRegion getTextureRegion() {
			return ArrowProjectile.textureRegion;
		}


		@Override
		public TextureRegion getIconTextureRegion() {
			return null;
		}


		@Override
		protected Item internalCopy() {
			return new FireArrowItem<>(metal);
		}


		@Override
		public Projectile getProjectile() {
			return new FireArrowProjectile<>(metal, null, null, 10f, false);
		}
	}
}