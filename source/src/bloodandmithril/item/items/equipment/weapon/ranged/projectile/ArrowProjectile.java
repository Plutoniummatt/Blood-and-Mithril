package bloodandmithril.item.items.equipment.weapon.ranged.projectile;

import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Maps;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.IndividualStateService;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.RenderProjectileWith;
import bloodandmithril.graphics.particles.ParticleService;
import bloodandmithril.graphics.renderers.ArrowRenderer;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;
import bloodandmithril.item.items.material.ArrowHeadItem;
import bloodandmithril.item.items.material.StickItem;
import bloodandmithril.item.material.Material;
import bloodandmithril.item.material.metal.Metal;
import bloodandmithril.item.material.wood.StandardWood;
import bloodandmithril.ui.FloatingTextService;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile;

/**
 * A vanilla arrow
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
@RenderProjectileWith(ArrowRenderer.class)
public class ArrowProjectile<T extends Metal> extends Projectile {
	private static final long serialVersionUID = -4403381751507847926L;
	public Class<T> arrowTipMaterial;

	public static TextureRegion textureRegion;

	/**
	 * Constructor
	 */
	public ArrowProjectile(final Class<T> metal, final Vector2 position, final Vector2 velocity) {
		super(position, velocity, new Vector2());
		this.arrowTipMaterial = metal;
	}


	@Override
	public float getTerminalVelocity() {
		return 2000f;
	}


	@Override
	public void collision(final Vector2 previousPosition) {
		try {
			Tile tile = Domain.getWorld(getWorldId()).getTopography().getTile(position, true);

			final Vector2 testPosition = position.cpy();
			final Vector2 velocityCopy = velocity.cpy();
			while (tile.isPlatformTile || !tile.isPassable()) {
				testPosition.sub(velocityCopy.nor());
				tile = Domain.getWorld(getWorldId()).getTopography().getTile(testPosition, true);
			}

			setPosition(testPosition);
			stuck = true;
		} catch (final NoTileFoundException e) {}
	}


	@Override
	public void hit(final Individual victim) {
		final float damage = velocity.len() / getTerminalVelocity() * (5f + 5f * Util.getRandom().nextFloat()) * Metal.getMaterial(arrowTipMaterial).getCombatMultiplier();
		Wiring.injector().getInstance(IndividualStateService.class).damage(victim, damage);
		Wiring.injector().getInstance(FloatingTextService.class).addFloatingTextToIndividual(victim, String.format("%.2f", damage), Color.RED);
		ParticleService.bloodSplat(victim.getEmissionPosition(), new Vector2());
	}


	@Override
	public boolean penetrating() {
		return Util.roll(0.2f);
	}


	@Override
	public int getHitSound(final Individual individual) {
		return SoundService.stab;
	}


	public static class ArrowItem<T extends Metal> extends ProjectileItem implements Craftable {
		private static final long serialVersionUID = 1815115944595474845L;
		protected Class<T> metal;

		/**
		 * Constructor
		 */
		protected ArrowItem(final Class<T> metal) {
			super(0.05f, 1, false, Material.getMaterial(metal).getIngot().getValue() / 25 + ItemValues.WOODSTICK);
			this.metal = metal;
		}


		/**
		 * Static instance getter
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public static ArrowItem arrowItem(final Class<? extends Metal> metal) {
			return new ArrowItem(metal);
		}


		@Override
		protected String internalGetSingular(final boolean firstCap) {
			return Metal.getMaterial(metal).getName() + " Arrow";
		}


		@Override
		protected String internalGetPlural(final boolean firstCap) {
			return Metal.getMaterial(metal).getName() + " Arrows";
		}


		@Override
		public String getDescription() {
			return "An arrow, tipped with " + Metal.getMaterial(metal).getName();
		}


		@Override
		@SuppressWarnings("rawtypes")
		protected boolean internalSameAs(final Item other) {
			if (other.getClass().equals(ArrowItem.class)) {
				return ((ArrowItem) other).metal.equals(metal);
			}
			return false;
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
			return new ArrowItem<>(metal);
		}


		@Override
		public Projectile getProjectile() {
			return new ArrowProjectile<>(metal, null, null);
		}


		@Override
		public boolean canBeCraftedBy(final Individual individual) {
			return true;
		}


		@Override
		public Map<Item, Integer> getRequiredMaterials() {
			final Map<Item, Integer> requiredMaterials = Maps.newHashMap();
			requiredMaterials.put(StickItem.stick(StandardWood.class), 1);
			requiredMaterials.put(ArrowHeadItem.arrowHead(metal), 1);
			return requiredMaterials;
		}


		@Override
		public float getCraftingDuration() {
			return 25f;
		}


		@Override
		public void crafterEffects(final Individual crafter, final float delta) {
		}
	}


	@Override
	public void targetHitKinematics() {
		if (Util.roll(0.2f)) {
			Domain.getWorld(getWorldId()).projectiles().removeProjectile(getId());
		} else {
			velocity.scl(0.05f);
			velocity.x = -velocity.x;
		}
	}


	@Override
	public void preFireDecorate(final Individual individual) {
	}


	@Override
	public void particleEffects(final float delta) {
	}
}