package bloodandmithril.item.items.equipment.weapon.ranged;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import bloodandmithril.audio.SoundService;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.TwoHandedProjectileWeapon;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile.ProjectileItem;
import bloodandmithril.item.items.equipment.weapon.ranged.projectile.ArrowProjectile;
import bloodandmithril.item.items.equipment.weapon.ranged.projectile.ArrowProjectile.ArrowItem;
import bloodandmithril.item.material.wood.Wood;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.util.datastructure.WrapperForTwo;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**
 * A long bow, fires {@link ArrowProjectile}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class LongBow<T extends Wood> extends TwoHandedProjectileWeapon<T> {
	private static final long serialVersionUID = -2594506184136140101L;
	private Item currentAmmo;
	
	public static TextureRegion texture;
	
	static {
		if (ClientServerInterface.isClient()) {
			texture = new TextureRegion(Domain.individualTexture, 0, 947, 94, 18);
		}
	}

	/**
	 * Constructor
	 */
	public LongBow(Class<T> material) {
		super(1f, 2, true, ItemValues.LONGBOW, material);
	}


	@Override
	public boolean stab() {
		return false;
	}


	@Override
	public Box getActionFrameHitBox(Individual individual) {
		return new Box(
			new Vector2(
				individual.getHitBox().position.x + (individual.getCurrentAction().left() ? - individual.getHitBox().width * (3f/4f) : individual.getHitBox().width  * (3f/4f)),
				individual.getHitBox().position.y
			),
			individual.getHitBox().width,
			individual.getHitBox().height
		);
	}


	@Override
	public int getHitSound() {
		return SoundService.stab;
	}


	@Override
	public int getBlockSound() {
		return SoundService.broadSwordBlock;
	}


	@Override
	public float getDisarmChance() {
		return 0;
	}


	@Override
	public float getParryChance() {
		return 0.2f;
	}


	@Override
	public float getParryChanceIgnored() {
		return 0;
	}


	@Override
	public int getAttackNumber(Individual attacker) {
		return 2;
	}


	@Override
	protected String weaponGetSingular(boolean firstCap) {
		return Wood.getMaterial(getMaterial()).getName() + " Longbow" + (currentAmmo == null ? " (No ammo)" : " (" + currentAmmo.getSingular(true) + ")");
	}


	@Override
	protected String weaponGetPlural(boolean firstCap) {
		return Wood.getMaterial(getMaterial()).getName() + " Longbows" + (currentAmmo == null ? " (No ammo)" : " (" + currentAmmo.getSingular(true) + ")");
	}


	@Override
	public float getBaseAttackPeriod() {
		return 3f;
	}


	@Override
	public float getBaseMinDamage() {
		return 1.5f;
	}


	@Override
	public float getBaseMaxDamage() {
		return 2.5f;
	}


	@Override
	public float getCritDamageMultiplier() {
		return 1.2f;
	}


	@Override
	public float getKnockbackStrength() {
		return 650;
	}


	@Override
	public WrapperForTwo<Animation, Vector2> getAttackAnimationEffects(Individual individual) {
		return null;
	}


	@Override
	public void specialEffect(Individual individual) {
	}


	@Override
	public float getBaseCritChance() {
		return 0.1f;
	}


	@Override
	public void render(Vector2 position, float angle, boolean flipX) {
		TextureRegion texture = getTextureRegion();

		spriteBatch.draw(
			Domain.individualTexture,
			position.x - (flipX ? texture.getRegionWidth() - 47 : 47),
			position.y - 3,
			flipX ? texture.getRegionWidth() - 47 : 47,
			9,
			texture.getRegionWidth(),
			texture.getRegionHeight(),
			1f,
			1f,
			flipX ? 20 : -20f,
			texture.getRegionX(),
			texture.getRegionY(),
			texture.getRegionWidth(),
			texture.getRegionHeight(),
			flipX,
			false
		);
	}


	@Override
	public String getDescription() {
		return "A longbow is a type of bow that has extended range.";
	}


	@Override
	@SuppressWarnings("rawtypes")
	protected boolean internalSameAs(Item other) {
		if (other instanceof LongBow) {
			return getMaterial().equals(((LongBow) other).getMaterial());
		}
		return false;
	}


	@Override
	public TextureRegion getTextureRegion() {
		return texture;
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		return null;
	}


	@Override
	protected Item internalCopy() {
		return new LongBow<>(getMaterial());
	}


	@Override
	public Category getType() {
		return Category.BOW;
	}


	@Override
	@SuppressWarnings("rawtypes")
	public Projectile fire(Vector2 origin, Vector2 direction) {
		if (currentAmmo != null) {
			ArrowProjectile arrow = (ArrowProjectile) ((ProjectileItem) currentAmmo).getProjectile();
			arrow.setPosition(origin);
			arrow.setVelocity(direction.cpy().scl(2000f));
			return arrow;
		}

		return null;
	}


	@Override
	public boolean canFire(Item item) {
		return item instanceof ArrowItem;
	}


	@Override
	public void setAmmo(Item item) {
		this.currentAmmo = item;
	}


	@Override
	public Item getAmmo() {
		return currentAmmo;
	}


	@Override
	public void particleEffects(Vector2 position, float angle, boolean flipX) {
	}
}