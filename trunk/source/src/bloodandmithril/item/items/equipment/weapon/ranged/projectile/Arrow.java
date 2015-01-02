package bloodandmithril.item.items.equipment.weapon.ranged.projectile;

import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;
import bloodandmithril.item.material.metal.Metal;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;

public class Arrow<T extends Metal> extends Projectile {
	private static final long serialVersionUID = -4403381751507847926L;
	public Class<T> arrowTipMaterial;


	/**
	 * Constructor
	 */
	public Arrow(Class<T> metal, Vector2 position, Vector2 velocity) {
		super(position, velocity, new Vector2());
		this.arrowTipMaterial = metal;
	}


	@Override
	public void render() {
		Domain.shapeRenderer.begin(ShapeType.Line);
		Domain.shapeRenderer.setColor(Color.RED);
		Vector2 position = getPosition();
		Vector2 behind = getPosition().cpy().sub(getVelocity().cpy().nor().mul(50f));
		Domain.shapeRenderer.line(position.x, position.y, behind.x, behind.y);
		Domain.shapeRenderer.end();
	}


	@Override
	protected float getTerminalVelocity() {
		return 2000f;
	}
}