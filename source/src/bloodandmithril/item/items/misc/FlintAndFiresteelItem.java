package bloodandmithril.item.items.misc;

import static bloodandmithril.character.ai.perception.Visible.getVisible;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import bloodandmithril.audio.SoundService;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.graphics.particles.ParticleService;
import bloodandmithril.item.FireLighter;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.prop.Prop;

/**
 * Basic firestarter, not good for much else
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class FlintAndFiresteelItem extends MiscItem implements FireLighter {
	private static final long serialVersionUID = 1209549782426000939L;

	/**
	 * Constructor
	 */
	public FlintAndFiresteelItem() {
		super(0.1f, 1, false, ItemValues.FLINTANDFIRESTEEL);
	}


	@Override
	protected String internalGetSingular(final boolean firstCap) {
		return firstCap ? "Flint and firesteel" : "flint and firesteel";
	}


	@Override
	protected String internalGetPlural(final boolean firstCap) {
		return firstCap ? "Flint and firesteel" : "flint and firesteel";
	}


	@Override
	public String getDescription() {
		return "Basic fire starting kit, strike the steel against the flint to produce sparks";
	}


	@Override
	protected boolean internalSameAs(final Item other) {
		return other instanceof FlintAndFiresteelItem;
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
		return new FlintAndFiresteelItem();
	}


	@Override
	public void fireLightingEffect(final Prop lightable) {
		ParticleService.parrySpark(lightable.position.cpy().add(0, 7), new Vector2(), Depth.MIDDLEGROUND, Color.WHITE, Color.WHITE, 100, true, 30, 200f);
		SoundService.play(SoundService.flint, lightable.position, true, getVisible(lightable));
	}


	@Override
	public boolean canLightFire() {
		return true;
	}
}