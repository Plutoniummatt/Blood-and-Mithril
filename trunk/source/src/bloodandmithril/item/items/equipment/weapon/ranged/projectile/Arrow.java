package bloodandmithril.item.items.equipment.weapon.ranged.projectile;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;

import java.util.Map;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.particles.ParticleService;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;
import bloodandmithril.item.items.material.ArrowHead;
import bloodandmithril.item.items.material.Stick;
import bloodandmithril.item.material.Material;
import bloodandmithril.item.material.metal.Metal;
import bloodandmithril.item.material.wood.StandardWood;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Maps;

/**
 * A vanilla arrow
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class Arrow<T extends Metal> extends Projectile {
	private static final long serialVersionUID = -4403381751507847926L;
	public Class<T> arrowTipMaterial;

	public static TextureRegion textureRegion;
	private float angle;
	protected boolean stuck = false;

	/**
	 * Constructor
	 */
	public Arrow(Class<T> metal, Vector2 position, Vector2 velocity) {
		super(position, velocity, new Vector2());
		this.arrowTipMaterial = metal;
	}


	@Override
	public void update(float delta) {
		if (!stuck) {
			angle = velocity.angle();
			super.update(delta);
		}
	}


	@Override
	public void render() {
		spriteBatch.draw(
			textureRegion,
			position.x - 25,
			position.y - 1.5f,
			25,
			1.5f,
			textureRegion.getRegionWidth(),
			textureRegion.getRegionHeight(),
			1f,
			1f,
			angle
		);
	}


	@Override
	protected float getTerminalVelocity() {
		return 2000f;
	}


	@Override
	protected void collision(Vector2 previousPosition) {
		try {
			Tile tile = Domain.getWorld(getWorldId()).getTopography().getTile(position, true);

			Vector2 testPosition = position.cpy();
			Vector2 velocityCopy = velocity.cpy();
			while (tile.isPlatformTile || !tile.isPassable()) {
				testPosition.sub(velocityCopy.nor());
				tile = Domain.getWorld(getWorldId()).getTopography().getTile(testPosition, true);
			}

			setPosition(testPosition);
			stuck = true;
		} catch (NoTileFoundException e) {}
	}


	@Override
	public void hit(Individual victim) {
		float damage = velocity.len() / getTerminalVelocity() * (5f + 5f * Util.getRandom().nextFloat()) * Metal.getMaterial(arrowTipMaterial).getCombatMultiplier();
		victim.damage(damage);
		victim.addFloatingText(String.format("%.2f", damage), Color.RED);
		ParticleService.bloodSplat(victim.getEmissionPosition(), new Vector2());
	}


	@Override
	protected boolean penetrating() {
		return Util.roll(0.2f);
	}


	@Override
	protected int getHitSound(Individual individual) {
		return SoundService.stab;
	}


	public static class ArrowItem<T extends Metal> extends ProjectileItem implements Craftable {
		private static final long serialVersionUID = 1815115944595474845L;
		protected Class<T> metal;

		/**
		 * Constructor
		 */
		protected ArrowItem(Class<T> metal) {
			super(0.05f, 1, false, Material.getMaterial(metal).getIngot().getValue() / 25 + ItemValues.WOODSTICK);
			this.metal = metal;
		}


		/**
		 * Static instance getter
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public static ArrowItem arrowItem(Class<? extends Metal> metal) {
			return new ArrowItem(metal);
		}


		@Override
		protected String internalGetSingular(boolean firstCap) {
			return Metal.getMaterial(metal).getName() + " Arrow";
		}


		@Override
		protected String internalGetPlural(boolean firstCap) {
			return Metal.getMaterial(metal).getName() + " Arrows";
		}


		@Override
		public String getDescription() {
			return "An arrow, tipped with " + Metal.getMaterial(metal).getName();
		}


		@Override
		@SuppressWarnings("rawtypes")
		protected boolean internalSameAs(Item other) {
			if (other.getClass().equals(ArrowItem.class)) {
				return ((ArrowItem) other).metal.equals(metal);
			}
			return false;
		}


		@Override
		public TextureRegion getTextureRegion() {
			return Arrow.textureRegion;
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
			return new Arrow<>(metal, null, null);
		}


		@Override
		public boolean canBeCraftedBy(Individual individual) {
			return true;
		}


		@Override
		public Map<Item, Integer> getRequiredMaterials() {
			Map<Item, Integer> requiredMaterials = Maps.newHashMap();
			requiredMaterials.put(Stick.stick(StandardWood.class), 1);
			requiredMaterials.put(ArrowHead.arrowHead(metal), 1);
			return requiredMaterials;
		}


		@Override
		public float getCraftingDuration() {
			return 25f;
		}
	}


	@Override
	protected void targetHitKinematics() {
		if (Util.roll(0.2f)) {
			Domain.getWorld(getWorldId()).projectiles().removeProjectile(getId());
		} else {
			velocity.scl(0.05f);
			velocity.x = -velocity.x;
		}
	}


	@Override
	public void preFireDecorate(Individual individual) {
	}
}