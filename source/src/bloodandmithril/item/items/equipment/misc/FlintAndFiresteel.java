package bloodandmithril.item.items.equipment.misc;

import static bloodandmithril.character.ai.perception.Visible.getVisible;
import bloodandmithril.audio.SoundService;
import bloodandmithril.graphics.particles.ParticleService;
import bloodandmithril.item.FireLighter;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.misc.Misc;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.Domain.Depth;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**
 * Basic firestarter, not good for much else
 *
 * @author Matt
 */
public class FlintAndFiresteel extends Misc implements FireLighter {
	private static final long serialVersionUID = 1209549782426000939L;

	/**
	 * Constructor
	 */
	public FlintAndFiresteel() {
		super(0.1f, 1, false, ItemValues.FLINTANDFIRESTEEL);
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return firstCap ? "Flint and firesteel" : "flint and firesteel";
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return firstCap ? "Flint and firesteel" : "flint and firesteel";
	}


	@Override
	public String getDescription() {
		return "Basic fire starting kit, strike the steel against the flint to produce sparks";
	}


	@Override
	protected boolean internalSameAs(Item other) {
		return other instanceof FlintAndFiresteel;
	}


	@Override
	public TextureRegion getTextureRegion() {
		return null;
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		return null;
	}


	@Override
	protected Item internalCopy() {
		return new FlintAndFiresteel();
	}


	@Override
	public void fireLightingEffect(Prop lightable) {
		ParticleService.parrySpark(lightable.position.cpy().add(0, 7), new Vector2(), Depth.MIDDLEGROUND, Color.WHITE, Color.WHITE, 100, true, 30, 200f);
		SoundService.play(SoundService.flint, lightable.position, true, getVisible(lightable));
	}


	@Override
	public boolean canLightFire() {
		return true;
	}
}