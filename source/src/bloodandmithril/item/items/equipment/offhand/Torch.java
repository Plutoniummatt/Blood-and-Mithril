package bloodandmithril.item.items.equipment.offhand;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Maps;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.proficiency.proficiencies.Carpentry;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.graphics.particles.ParticleService;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.FireLighter;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.Equipper;
import bloodandmithril.item.items.material.StickItem;
import bloodandmithril.item.material.wood.StandardWood;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;

/**
 * Offhand torch for lighting
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class Torch extends OffhandEquipment implements FireLighter, Craftable {
	private static final long serialVersionUID = 5112607606681273075L;

	public static TextureRegion torch;
	private float durationRemaining;
	private Integer workingId;
	private boolean burning;

	/**
	 * Constructor
	 */
	public Torch() {
		super(1f, 2, ItemValues.TORCH);
		this.durationRemaining = 1000f;
	}


	@Override
	protected String internalGetSingular(final boolean firstCap) {
		return (firstCap ? "Torch" : "torch") + " (" + String.format("%.2f", durationRemaining) + ")";
	}


	@Override
	protected String internalGetPlural(final boolean firstCap) {
		return (firstCap ? "Torches" : "torches") + " (" + String.format("%.2f", durationRemaining) + ")";
	}


	@Override
	public String getDescription() {
		return "A torch, used for lighting. Magically disappears when it burns out.";
	}


	@Override
	protected boolean internalSameAs(final Item other) {
		if (other instanceof Torch) {
			return ((Torch) other).workingId == workingId && ((Torch) other).durationRemaining == durationRemaining && burning == ((Torch) other).burning;
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
		final Torch torch = new Torch();
		torch.durationRemaining = durationRemaining;
		torch.burning = burning;
		return torch;
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
	public ItemCategory getType() {
		return ItemCategory.OFFHAND;
	}


	@Override
	public Vector2 getGripLocation() {
		return new Vector2(12, 2);
	}


	@Override
	public void particleEffects(final Vector2 position, final float angle, final boolean flipX) {
		if (burning) {
			final Vector2 emission = position.cpy().add(new Vector2(flipX ? - 27 : 27, 0).rotate(angle));

			final float size1 = Util.getRandom().nextFloat();
			final float size2 = Util.getRandom().nextFloat();
			ParticleService.randomVelocityDiminishing(emission, 3f, 15f, Colors.FIRE_START, Colors.FIRE_START, size1 * 3f, size1 * 8f + 10f, MovementMode.EMBER, Util.getRandom().nextInt(800), Depth.FOREGROUND, false, Colors.FIRE_END);
			ParticleService.randomVelocityDiminishing(emission, 3f, 15f, Colors.FIRE_START, Colors.FIRE_START, size2 * 3f, size2 * 2f + 6f, MovementMode.EMBER, Util.getRandom().nextInt(800), Depth.MIDDLEGROUND, false, Colors.FIRE_END);
			ParticleService.randomVelocityDiminishing(emission, 3f, 10f, Colors.LIGHT_SMOKE, Colors.LIGHT_SMOKE, 8f, 0f, MovementMode.EMBER, Util.getRandom().nextInt(3000), Depth.FOREGROUND, false, null);
		}
	}


	@Override
	public void update(final Equipper equipper, final float delta) {
		if (burning) {
			if (durationRemaining <= 0f) {
				equipper.unequip(this);
				equipper.takeItem(this);
				burning = false;
				Wiring.injector().getInstance(UserInterface.class).refreshRefreshableWindows();
			} else {
				durationRemaining -= delta;
			}
		}
	}


	@Override
	public void onUnequip(final Equipper equipper) {
		burning = false;
	}


	public boolean burning() {
		return burning;
	}


	@Override
	public void onEquip(final Equipper equipper) {
		if (workingId == null) {
			this.workingId = Wiring.injector().getInstance(ParameterPersistenceService.class).getParameters().getNextItemId();
		}

		if (equipper instanceof Individual) {
			final FireLighter fireLighter = ((Individual) equipper).getFireLighter();
			if (fireLighter != null) {
				burning = true;
				return;
			}

			final World world = Domain.getWorld(((Individual) equipper).getWorldId());
			for (final int propId : world.getPositionalIndexMap().getNearbyEntityIds(Prop.class, ((Individual) equipper).getState().position)) {
				final Prop prop = world.props().getProp(propId);
				if (prop.canBeUsedAsFireSource() && ((Individual) equipper).getInteractionBox().overlapsWith(new Box(prop.position.cpy().add(0, prop.height/2), prop.width, prop.height))) {
					burning = true;
					return;
				}
			}

			for (final int individualId : world.getPositionalIndexMap().getNearbyEntityIds(Individual.class, ((Individual) equipper).getState().position)) {
				final Individual nearbyIndividual = Domain.getIndividual(individualId);
				if (nearbyIndividual.canBeUsedAsFireSource()) {
					burning = true;
					return;
				}
			}
		}
	}


	@Override
	public void fireLightingEffect(final Prop lightable) {
	}


	@Override
	public boolean canLightFire() {
		return burning;
	}


	@Override
	public boolean canBeCraftedBy(final Individual individual) {
		return true;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		final HashMap<Item, Integer> materials = Maps.newHashMap();
		materials.put(StickItem.stick(StandardWood.class), 1);
		return materials;
	}


	@Override
	public void crafterEffects(final Individual crafter, final float delta) {
		crafter.getProficiencies().getProficiency(Carpentry.class).increaseExperience(delta * 2f);
	}


	@Override
	public float getCraftingDuration() {
		return 10f;
	}
}