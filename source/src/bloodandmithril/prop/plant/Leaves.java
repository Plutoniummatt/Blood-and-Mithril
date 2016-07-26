package bloodandmithril.prop.plant;

import java.io.Serializable;

import bloodandmithril.core.Copyright;

/**
 * Leaves
 * 
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class Leaves implements Serializable {
	private static final long serialVersionUID = -6686824361055155280L;
	
	public final int textureId;

	/**
	 * Constructor
	 */
	public Leaves(int textureId) {
		this.textureId = textureId;
	}
}