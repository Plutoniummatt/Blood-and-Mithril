package bloodandmithril.item.equipment;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static java.lang.Math.abs;
import static java.lang.Math.max;

import java.util.Map;

import bloodandmithril.character.Individual;
import bloodandmithril.character.conditions.Bleeding;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.item.Item;
import bloodandmithril.item.material.metal.SteelIngot;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.tile.Tile;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Maps;

public class Broadsword extends OneHandedWeapon implements Craftable {
	private static final long serialVersionUID = -8932319773500235186L;

	public static TextureRegion texture;
	private float angle = Util.getRandom().nextFloat() * 360f;
	private float angularVelocity = (Util.getRandom().nextFloat() - 0.5f) * 40f;

	/**
	 * Constructor
	 */
	public Broadsword(long value) {
		super(10, true, value);
	}


	@Override
	public String getSingular(boolean firstCap) {
		return "Broad sword";
	}


	@Override
	public String getPlural(boolean firstCap) {
		return "Broad swords";
	}


	@Override
	public String getDescription() {
		return "Broadswords are heavy military swords, contrasting with rapier, the light sword worn with civilian dress. Since the blade of the rapier had become narrow and thrust-oriented, the heavier blades became known as Broadsword";
	}


	@Override
	public boolean sameAs(Item other) {
		if (other instanceof Broadsword) {
			return true;
		} else {
			return false;
		}
	}


	@Override
	public void render(Vector2 position, float angle, boolean flipX) {
		BloodAndMithrilClient.spriteBatch.draw(
			Domain.individualTexture,
			position.x - (flipX ? texture.getRegionWidth() - 13 : 13),
			position.y - 7,
			flipX ? texture.getRegionWidth() - 13 : 13,
			7,
			texture.getRegionWidth(),
			texture.getRegionHeight(),
			1f,
			1f,
			angle,
			417,
			621,
			52,
			11,
			flipX,
			false
		);
	}


	@Override
	public void affect(Individual victim) {
		victim.damage(Util.getRandom().nextFloat() * 5f);
		victim.addCondition(new Bleeding(0.03f));
	}


	@Override
	public boolean canBeCraftedBy(Individual individual) {
		return individual.getSkills().getSmithing() >= 10;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		Map<Item, Integer> map = Maps.newHashMap();

		map.put(new SteelIngot(), 7);

		return map;
	}


	@Override
	public float getCraftingDuration() {
		return 15f;
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return texture;
	}


	@Override
	public void update(float delta) {
		if (getId() == null) {
			return;
		}

		Vector2 previousPosition = getPosition().cpy();
		Vector2 previousVelocity = getVelocity().cpy();

		getPosition().add(getVelocity().cpy().mul(delta));

		float gravity = Domain.getWorld(getWorldId()).getGravity();
		if (abs((getVelocity().y - gravity * delta) * delta) < TILE_SIZE/2) {
			getVelocity().y = getVelocity().y - delta * gravity;
		} else {
			getVelocity().y = getVelocity().y * 0.8f;
		}

		Tile tileUnder = Domain.getWorld(getWorldId()).getTopography().getTile(getPosition().x, getPosition().y, true);
		if (tileUnder.isPassable()) {
			angle = angle + angularVelocity;
		}

		if (tileUnder.isPlatformTile || !tileUnder.isPassable()) {
			Vector2 trial = getPosition().cpy();
			trial.y += -previousVelocity.y*delta;

			if (Domain.getWorld(getWorldId()).getTopography().getTile(trial.x, trial.y, true).isPassable()) {
				if (previousVelocity.y <= 0f) {

					int i = (int)angle % 360;
					if (i < 0) {
						i = i + 360;
					}
					boolean pointingUp = i > 0 && i < 180;
					if (pointingUp) {
						if (abs(getVelocity().y) > 400f) {
							angularVelocity = (Util.getRandom().nextFloat() - 0.5f) * 40f;
						} else {
							angularVelocity = max(angularVelocity * 0.6f, 5f);
						}
						setPosition(previousPosition);
						getVelocity().y = -previousVelocity.y * 0.7f;
						getVelocity().x = previousVelocity.x * 0.3f;
					} else {
						angularVelocity = 0f;
						getVelocity().x = getVelocity().x * 0.3f;
						getVelocity().y = 0f;
						getPosition().y = Domain.getWorld(getWorldId()).getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(getPosition(), true).y;
					}
				} else {
					setPosition(previousPosition);
					getVelocity().y = -previousVelocity.y;
				}
			} else {
				getVelocity().x = 0f;
				setPosition(previousPosition);
			}
		}
	}


	@Override
	protected float getRenderAngle() {
		return angle;
	};


	@Override
	protected Vector2 getRenderCentreOffset() {
		return new Vector2(getTextureRegion().getRegionWidth() * (2f / 4f), getTextureRegion().getRegionHeight() / 2);
	}
}