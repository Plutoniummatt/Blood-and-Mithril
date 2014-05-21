package bloodandmithril.item.items.material;

import bloodandmithril.item.items.Item;
import bloodandmithril.item.material.Material;
import bloodandmithril.item.material.wood.Wood;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A log made from a {@link Wood}
 *
 * @author Matt
 */
public class Log extends Item {
	private static final long serialVersionUID = 8519886397429197864L;

	private Class<? extends Wood> wood;

	/**
	 * Constructor
	 */
	private Log(Class<? extends Wood> wood) {
		super(5f, false);
		this.wood = wood;
		setValue(Material.getMaterial(wood).getLogValue());
	}


	/**
	 * Static instance getter
	 */
	public static Log log(Class<? extends Wood> wood) {
		return new Log(wood);
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return Material.getMaterial(wood).getName() + " Log";
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return Material.getMaterial(wood).getName() + " Logs";
	}


	@Override
	public String getDescription() {
		return "A wooden log, made from " + Material.getMaterial(wood).getName() + ".";
	}


	@Override
	protected boolean internalSameAs(Item other) {
		if (other instanceof Log) {
			return wood.equals(((Log) other).wood);
		}

		return false;
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return Material.getMaterial(wood).getLogTextureRegion();
	}


	@Override
	protected Item internalCopy() {
		return log(wood);
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		// TODO Auto-generated method stub
		return null;
	}
}