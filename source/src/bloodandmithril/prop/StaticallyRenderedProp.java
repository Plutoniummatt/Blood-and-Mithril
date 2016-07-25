package bloodandmithril.prop;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import bloodandmithril.core.Copyright;

/**
 * Indicates that a prop is statically rendered, hence must return a {@link TextureRegion} to render
 * 
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public interface StaticallyRenderedProp {

	/**
	 * Returns the {@link TextureRegion} to render
	 */
	public TextureRegion getTextureRegion();
}