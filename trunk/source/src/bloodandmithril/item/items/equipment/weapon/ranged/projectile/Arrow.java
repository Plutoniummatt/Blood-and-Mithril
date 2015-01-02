package bloodandmithril.item.items.equipment.weapon.ranged.projectile;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.graphics.particles.ParticleService;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;
import bloodandmithril.item.material.metal.Metal;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.tile.Tile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class Arrow<T extends Metal> extends Projectile {
	private static final long serialVersionUID = -4403381751507847926L;
	public Class<T> arrowTipMaterial;

	public static TextureRegion textureRegion;
	private float angle;
	private boolean stuck = false;
	
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
		Tile tile = Domain.getWorld(getWorldId()).getTopography().getTile(position, true);
		
		Vector2 testPosition = position.cpy();
		Vector2 velocityCopy = velocity.cpy();
		while (tile.isPlatformTile || !tile.isPassable()) {
			testPosition.sub(velocityCopy.nor());
			tile = Domain.getWorld(getWorldId()).getTopography().getTile(testPosition, true);
		}
		
		setPosition(testPosition);
		stuck = true;
	}


	@Override
	public void hit(Individual victim) {
		victim.damage(10f);
		victim.addFloatingText("-10.0", Color.RED);
		ParticleService.bloodSplat(victim.getEmissionPosition(), new Vector2());
	}


	@Override
	protected boolean penetrating() {
		return Util.roll(0.2f);
	}
}